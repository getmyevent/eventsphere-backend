package com.eventsphere.modules.session;

import com.eventsphere.common.error.ResourceNotFoundException;
import com.eventsphere.modules.attendee.AttendeeEntity;
import com.eventsphere.modules.attendee.AttendeeRepository;
import com.eventsphere.modules.session.dto.BookmarkResponse;
import com.eventsphere.modules.session.dto.SessionDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private static final Logger log = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final SessionBookmarkRepository sessionBookmarkRepository;
    private final AttendeeRepository attendeeRepository;
    private final ObjectMapper objectMapper;

    public SessionService(
            SessionRepository sessionRepository,
            SessionBookmarkRepository sessionBookmarkRepository,
            AttendeeRepository attendeeRepository,
            ObjectMapper objectMapper
    ) {
        this.sessionRepository = sessionRepository;
        this.sessionBookmarkRepository = sessionBookmarkRepository;
        this.attendeeRepository = attendeeRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<SessionDto> getSessions(
            String eventId,
            Integer day,
            String track,
            String status,
            String search,
            Boolean bookmarkedOnly,
            String attendeeId
    ) {
        List<SessionEntity> allSessions = sessionRepository.findAllByEventIdOrderByDayNumberAsc(eventId);

        Set<String> bookmarkedIds = (attendeeId != null)
                ? sessionBookmarkRepository.findByIdAttendeeId(attendeeId).stream()
                .map(b -> b.getId().getSessionId())
                .collect(Collectors.toSet())
                : Set.of();

        // Build speaker name -> ID map for event
        Map<String, String> speakerNameToId = attendeeRepository.findAllByEventId(eventId).stream()
                .filter(a -> a.getCategory().name().equalsIgnoreCase("SPEAKER") || a.getAuthRole().name().contains("SPEAKER"))
                .collect(Collectors.toMap(
                        a -> a.getName().toLowerCase().trim(),
                        AttendeeEntity::getId,
                        (existing, replacement) -> existing
                ));

        return allSessions.stream()
                .filter(s -> day == null || s.getDayNumber() == day)
                .filter(s -> track == null || track.isBlank() || s.getTrack().equalsIgnoreCase(track.trim()))
                .filter(s -> status == null || status.isBlank() || s.getStatus().equalsIgnoreCase(status.trim()))
                .filter(s -> {
                    if (search == null || search.isBlank()) return true;
                    String q = search.trim().toLowerCase();
                    boolean inTitle = s.getTitle() != null && s.getTitle().toLowerCase().contains(q);
                    boolean inDesc = s.getDescription() != null && s.getDescription().toLowerCase().contains(q);
                    boolean inSpeaker = s.getSpeakerName() != null && s.getSpeakerName().toLowerCase().contains(q);
                    boolean inTrack = s.getTrack() != null && s.getTrack().toLowerCase().contains(q);
                    return inTitle || inDesc || inSpeaker || inTrack;
                })
                .filter(s -> {
                    if (Boolean.TRUE.equals(bookmarkedOnly)) {
                        return bookmarkedIds.contains(s.getId());
                    }
                    return true;
                })
                .map(s -> mapToDto(s, bookmarkedIds.contains(s.getId()), speakerNameToId))
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionDto getSessionById(String sessionId, String attendeeId) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        boolean isBookmarked = (attendeeId != null) &&
                sessionBookmarkRepository.existsByIdSessionIdAndIdAttendeeId(sessionId, attendeeId);

        String speakerId = null;
        if (session.getSpeakerName() != null) {
            String norm = session.getSpeakerName().toLowerCase().trim();
            speakerId = attendeeRepository.findAllByEventId(session.getEventId()).stream()
                    .filter(a -> a.getName().toLowerCase().trim().equals(norm) || norm.contains(a.getName().toLowerCase().trim()))
                    .map(AttendeeEntity::getId)
                    .findFirst()
                    .orElse(null);
        }

        return mapToDto(session, isBookmarked, speakerId != null ? Map.of(session.getSpeakerName().toLowerCase().trim(), speakerId) : Map.of());
    }

    @Transactional
    public BookmarkResponse toggleBookmark(String sessionId, String attendeeId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }

        boolean exists = sessionBookmarkRepository.existsByIdSessionIdAndIdAttendeeId(sessionId, attendeeId);
        if (exists) {
            sessionBookmarkRepository.deleteByIdSessionIdAndIdAttendeeId(sessionId, attendeeId);
            log.info("Attendee {} removed bookmark on session {}", attendeeId, sessionId);
            return new BookmarkResponse(sessionId, false, "Session removed from My Schedule");
        } else {
            sessionBookmarkRepository.save(new SessionBookmarkEntity(sessionId, attendeeId));
            log.info("Attendee {} bookmarked session {}", attendeeId, sessionId);
            return new BookmarkResponse(sessionId, true, "Session added to My Schedule");
        }
    }

    private SessionDto mapToDto(SessionEntity s, boolean isBookmarked, Map<String, String> speakerNameToId) {
        List<String> takeaways = readStringList(s.getTakeawaysJson());
        String speakerId = null;
        if (s.getSpeakerName() != null) {
            String norm = s.getSpeakerName().toLowerCase().trim();
            speakerId = speakerNameToId.get(norm);
            if (speakerId == null) {
                // partial match
                speakerId = speakerNameToId.entrySet().stream()
                        .filter(e -> norm.contains(e.getKey()) || e.getKey().contains(norm))
                        .map(Map.Entry::getValue)
                        .findFirst()
                        .orElse(null);
            }
        }

        return new SessionDto(
                s.getId(),
                s.getTitle(),
                s.getTrack(),
                s.getDayNumber(),
                s.getTimeRange(),
                s.getRoom(),
                s.getSessionType(),
                s.getSpeakerName(),
                s.getSpeakerDesignation(),
                s.getSpeakerCompany(),
                speakerId,
                s.getDescription(),
                takeaways,
                s.getStatus(),
                s.getAttendanceCount(),
                s.getQuestionsCount(),
                isBookmarked
        );
    }

    private List<String> readStringList(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse JSON string list: {}", json);
            return new ArrayList<>();
        }
    }
}
