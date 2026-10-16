package com.eventsphere.modules.organizer;

import com.eventsphere.common.error.BusinessException;
import com.eventsphere.common.error.ResourceNotFoundException;
import com.eventsphere.common.util.MobileNormalizer;
import com.eventsphere.modules.attendee.AttendeeEntity;
import com.eventsphere.modules.attendee.AttendeeRepository;
import com.eventsphere.modules.attendee.PrivacyConsentEntity;
import com.eventsphere.modules.attendee.PrivacyConsentRepository;
import com.eventsphere.modules.attendee.dto.AttendeeProfileDto;
import com.eventsphere.modules.attendee.dto.PrivacyConsentDto;
import com.eventsphere.modules.attendee.model.AttendeeCategory;
import com.eventsphere.modules.attendee.model.PrivacyPreset;
import com.eventsphere.modules.attendee.model.UserRole;
import com.eventsphere.modules.connection.ConnectionRepository;
import com.eventsphere.modules.exhibitor.ExhibitorEntity;
import com.eventsphere.modules.exhibitor.ExhibitorRepository;
import com.eventsphere.modules.exhibitor.LeadRepository;
import com.eventsphere.modules.organizer.dto.*;
import com.eventsphere.modules.session.SessionEntity;
import com.eventsphere.modules.session.SessionRepository;
import com.eventsphere.modules.session.dto.SessionDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrganizerService {

    private static final Logger log = LoggerFactory.getLogger(OrganizerService.class);

    private final AttendeeRepository attendeeRepository;
    private final PrivacyConsentRepository privacyConsentRepository;
    private final ExhibitorRepository exhibitorRepository;
    private final LeadRepository leadRepository;
    private final ConnectionRepository connectionRepository;
    private final SessionRepository sessionRepository;
    private final ObjectMapper objectMapper;

    public OrganizerService(
            AttendeeRepository attendeeRepository,
            PrivacyConsentRepository privacyConsentRepository,
            ExhibitorRepository exhibitorRepository,
            LeadRepository leadRepository,
            ConnectionRepository connectionRepository,
            SessionRepository sessionRepository,
            ObjectMapper objectMapper
    ) {
        this.attendeeRepository = attendeeRepository;
        this.privacyConsentRepository = privacyConsentRepository;
        this.exhibitorRepository = exhibitorRepository;
        this.leadRepository = leadRepository;
        this.connectionRepository = connectionRepository;
        this.sessionRepository = sessionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public OrganizerMetricsDto getMetrics(String eventId) {
        long totalRegistered = attendeeRepository.countByEventId(eventId);
        long totalCheckedIn = attendeeRepository.countByEventIdAndCheckedInTrue(eventId);
        double checkInRate = totalRegistered > 0
                ? Math.round((totalCheckedIn * 1000.0) / totalRegistered) / 10.0
                : 0.0;

        long totalExhibitors = exhibitorRepository.countByEventId(eventId);
        long activeExhibitors = exhibitorRepository.countByEventIdAndStatus(eventId, "Active");
        long totalSpeakers = attendeeRepository.countByEventIdAndCategory(eventId, AttendeeCategory.SPEAKER);

        long totalSessions = sessionRepository.countByEventId(eventId);
        long liveSessions = sessionRepository.countByEventIdAndStatus(eventId, "live");
        long totalConnections = connectionRepository.countByEventIdAndStatus(eventId, "connected");
        long totalLeads = leadRepository.countByEventId(eventId);

        List<ExhibitorEntity> exhibitors = exhibitorRepository.findAllByEventId(eventId);
        List<Object[]> leadCountsGrouped = leadRepository.countLeadsGroupedByExhibitor(eventId);
        Map<String, Long> leadCountMap = leadCountsGrouped.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue(),
                        (existing, replacement) -> existing
                ));

        List<TopExhibitorDto> topExhibitors = exhibitors.stream()
                .map(e -> {
                    long leads = leadCountMap.getOrDefault(e.getId(), 0L);
                    return new TopExhibitorDto(e.getId(), e.getCompany(), e.getBooth(), e.getCategory(), leads);
                })
                .sorted((a, b) -> Long.compare(b.leads(), a.leads()))
                .limit(5)
                .toList();

        return new OrganizerMetricsDto(
                totalRegistered,
                totalCheckedIn,
                checkInRate,
                totalExhibitors,
                activeExhibitors,
                totalSpeakers,
                totalSessions,
                liveSessions,
                totalConnections,
                totalLeads,
                topExhibitors
        );
    }

    @Transactional
    public CheckInResponseDto toggleCheckIn(String eventId, String attendeeId) {
        AttendeeEntity attendee = attendeeRepository.findByIdAndEventId(attendeeId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendee not found: " + attendeeId));

        boolean nextCheckedIn = !attendee.isCheckedIn();
        attendee.setCheckedIn(nextCheckedIn);
        attendee.setCheckedInAt(nextCheckedIn ? Instant.now() : null);
        attendeeRepository.save(attendee);

        String message = nextCheckedIn ? "Attendee checked in successfully" : "Attendee check-in cancelled";
        String timeStr = attendee.getCheckedInAt() != null
                ? DateTimeFormatter.ISO_INSTANT.format(attendee.getCheckedInAt())
                : null;

        log.info("Organizer toggled check-in for attendee {} in event {}: checkedIn={}", attendeeId, eventId, nextCheckedIn);

        return new CheckInResponseDto(attendee.getId(), attendee.getName(), nextCheckedIn, timeStr, message);
    }

    @Transactional
    public AttendeeProfileDto registerWalkIn(String eventId, WalkInRegistrationRequest request) {
        String normalized = MobileNormalizer.normalize(request.mobile());
        if (normalized.length() != 10) {
            throw new BusinessException("Mobile number must be a valid 10-digit number");
        }

        if (attendeeRepository.existsByEventIdAndNormalizedMobile(eventId, normalized)) {
            throw new BusinessException("Mobile number '" + request.mobile() + "' is already registered for this event");
        }

        String attendeeId = "att-walkin-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String regId = "#WLK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();

        AttendeeCategory category = AttendeeCategory.DELEGATE;
        if (request.category() != null && !request.category().isBlank()) {
            try {
                category = AttendeeCategory.valueOf(request.category().toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                category = AttendeeCategory.DELEGATE;
            }
        }

        AttendeeEntity attendee = new AttendeeEntity();
        attendee.setId(attendeeId);
        attendee.setEventId(eventId);
        attendee.setRegistrationId(regId);
        attendee.setName(request.name().trim());
        attendee.setEmail(request.email().trim().toLowerCase());
        attendee.setMobile(request.mobile().trim());
        attendee.setNormalizedMobile(normalized);
        attendee.setCompany(request.company().trim());
        attendee.setDesignation(request.designation().trim());
        attendee.setTicketType(request.ticketType() != null && !request.ticketType().isBlank()
                ? request.ticketType()
                : "Full Access Delegate Pass");
        attendee.setCategory(category);
        attendee.setAuthRole(UserRole.ROLE_DELEGATE);
        attendee.setCheckedIn(true);
        attendee.setCheckedInAt(Instant.now());

        PrivacyConsentEntity priv = new PrivacyConsentEntity(attendeeId, PrivacyPreset.CONNECTIONS_ONLY, true, true, true, false);
        priv.setAttendee(attendee);
        attendee.setPrivacyConsent(priv);

        attendeeRepository.save(attendee);
        log.info("Registered on-site walk-in attendee {} ({}) in event {}", attendee.getName(), attendeeId, eventId);

        return mapToAttendeeDto(attendee);
    }

    public ImportResultDto importAttendees(String eventId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("CSV file cannot be empty");
        }

        int processed = 0;
        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isBlank()) continue;

                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                processed++;
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                if (parts.length < 5) {
                    skipped++;
                    errors.add("Row " + processed + ": insufficient columns (minimum 5 required: name, email, mobile, company, designation)");
                    continue;
                }

                String name = cleanField(parts[0]);
                String email = cleanField(parts[1]).toLowerCase();
                String rawMobile = cleanField(parts[2]);
                String company = cleanField(parts[3]);
                String designation = cleanField(parts[4]);
                String ticketType = parts.length > 5 ? cleanField(parts[5]) : "Delegate Pass";

                String normalized = MobileNormalizer.normalize(rawMobile);
                if (normalized.length() != 10) {
                    skipped++;
                    errors.add("Row " + processed + ": invalid mobile '" + rawMobile + "'");
                    continue;
                }

                if (attendeeRepository.existsByEventIdAndNormalizedMobile(eventId, normalized)) {
                    skipped++;
                    errors.add("Row " + processed + ": mobile '" + rawMobile + "' already registered (duplicate)");
                    continue;
                }

                AttendeeCategory category = AttendeeCategory.DELEGATE;
                if (parts.length > 6 && !cleanField(parts[6]).isBlank()) {
                    try {
                        category = AttendeeCategory.fromValue(cleanField(parts[6]));
                    } catch (Exception ex) {
                        category = AttendeeCategory.DELEGATE;
                    }
                }

                String attendeeId = "att-imp-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
                String regId = (parts.length > 7 && !cleanField(parts[7]).isBlank())
                        ? cleanField(parts[7])
                        : String.format("#IMP-%05d-%s", processed, UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase());

                AttendeeEntity attendee = new AttendeeEntity();
                attendee.setId(attendeeId);
                attendee.setEventId(eventId);
                attendee.setRegistrationId(regId);
                attendee.setName(name);
                attendee.setEmail(email);
                attendee.setMobile(rawMobile);
                attendee.setNormalizedMobile(normalized);
                attendee.setCompany(company);
                attendee.setDesignation(designation);
                attendee.setTicketType(ticketType.isBlank() ? "Delegate Pass" : ticketType);
                attendee.setCategory(category);
                attendee.setAuthRole(UserRole.ROLE_DELEGATE);
                attendee.setCheckedIn(false);

                PrivacyConsentEntity priv = new PrivacyConsentEntity(attendeeId, PrivacyPreset.CONNECTIONS_ONLY, true, true, true, false);
                priv.setAttendee(attendee);
                attendee.setPrivacyConsent(priv);

                try {
                    attendeeRepository.save(attendee);
                    imported++;
                } catch (Exception rowEx) {
                    skipped++;
                    errors.add("Row " + processed + " (" + name + "): save failed - " + rowEx.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse import CSV", e);
            throw new BusinessException("Failed to parse CSV file: " + e.getMessage());
        }

        log.info("Imported {} attendees for event {} (processed={}, skipped={})", imported, eventId, processed, skipped);
        return new ImportResultDto(processed, imported, skipped, errors);
    }

    @Transactional
    public AttendeeProfileDto updateAttendee(String eventId, String attendeeId, com.eventsphere.modules.attendee.dto.UpdateProfileRequest request) {
        AttendeeEntity attendee = attendeeRepository.findByIdAndEventId(attendeeId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendee", attendeeId));

        if (request.getName() != null && !request.getName().isBlank()) attendee.setName(request.getName().trim());
        if (request.getDesignation() != null) attendee.setDesignation(request.getDesignation().trim());
        if (request.getCompany() != null) attendee.setCompany(request.getCompany().trim());
        if (request.getBio() != null) attendee.setBio(request.getBio().trim());
        if (request.getAvatar() != null) attendee.setAvatarUrl(request.getAvatar().trim());
        if (request.getLinkedin() != null) attendee.setLinkedinUrl(request.getLinkedin().trim());
        if (request.getTwitter() != null) attendee.setTwitterUrl(request.getTwitter().trim());

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            attendee.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getMobile() != null && !request.getMobile().isBlank()) {
            attendee.setMobile(request.getMobile().trim());
            String digits = request.getMobile().replaceAll("\\D", "");
            if (digits.startsWith("91") && digits.length() > 10) digits = digits.substring(2);
            if (digits.startsWith("0")) digits = digits.substring(1);
            attendee.setNormalizedMobile(digits);
        }
        if (request.getSkills() != null) {
            attendee.setSkillsList(request.getSkills());
        }
        if (request.getInterests() != null) {
            attendee.setInterestsList(request.getInterests());
        }
        if (request.getTicketType() != null && !request.getTicketType().isBlank()) {
            attendee.setTicketType(request.getTicketType().trim());
        }
        if (request.getCategory() != null && !request.getCategory().isBlank()) {
            attendee.setCategory(AttendeeCategory.fromValue(request.getCategory()));
        }

        AttendeeEntity saved = attendeeRepository.save(attendee);
        log.info("Organizer updated attendee record for {}", attendeeId);

        return mapToAttendeeDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AttendeeProfileDto> getAttendees(
            String eventId,
            String query,
            String category,
            Boolean checkedInOnly,
            int page,
            int size
    ) {
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = (size > 0 && size <= 100) ? size : 30;
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(sanitizedPage, sanitizedSize);

        String q = (query != null && !query.isBlank()) ? query.trim() : null;
        AttendeeCategory cat = (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category.trim()))
                ? AttendeeCategory.fromValue(category.trim())
                : null;

        List<AttendeeEntity> list = attendeeRepository.searchAttendeesForOrganizer(eventId, q, cat, checkedInOnly, pageable);
        return list.stream().map(this::mapToAttendeeDto).toList();
    }

    @Transactional(readOnly = true)
    public long countAttendees(
            String eventId,
            String query,
            String category,
            Boolean checkedInOnly
    ) {
        String q = (query != null && !query.isBlank()) ? query.trim() : null;
        AttendeeCategory cat = (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category.trim()))
                ? AttendeeCategory.fromValue(category.trim())
                : null;

        return attendeeRepository.countAttendeesForOrganizer(eventId, q, cat, checkedInOnly);
    }

    @Transactional(readOnly = true)
    public List<OrganizerExhibitorDto> getExhibitors(String eventId) {
        return exhibitorRepository.findExhibitorsWithLeadCount(eventId);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "sessions", allEntries = true)
    public SessionDto createSession(String eventId, CreateSessionRequest request) {
        String sessionId = "sess_" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        SessionEntity session = new SessionEntity();
        session.setId(sessionId);
        session.setEventId(eventId);
        session.setTitle(request.title().trim());
        session.setTrack(request.track() != null ? request.track().trim() : "General");
        session.setDayNumber(request.day() > 0 ? request.day() : 1);
        session.setTimeRange(request.timeRange() != null ? request.timeRange().trim() : "10:00 AM – 11:00 AM");
        session.setRoom(request.room() != null ? request.room().trim() : "Main Auditorium");
        session.setStatus(request.status() != null ? request.status().trim() : "upcoming");
        session.setSpeakerName(request.speakerName());
        session.setSpeakerDesignation(request.speakerDesignation());
        session.setSpeakerCompany(request.speakerCompany());
        session.setDescription(request.description());
        session.setTakeawaysJson(writeJson(request.takeaways() != null ? request.takeaways() : List.of()));
        session.setAttendanceCount(0);
        session.setQuestionsCount(0);

        sessionRepository.save(session);
        log.info("Created session {} ({}) in event {}", session.getTitle(), sessionId, eventId);

        return mapToSessionDto(session);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "sessions", allEntries = true)
    public SessionDto updateSession(String sessionId, UpdateSessionRequest request) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        if (request.title() != null && !request.title().isBlank()) session.setTitle(request.title().trim());
        if (request.track() != null) session.setTrack(request.track().trim());
        if (request.day() != null && request.day() > 0) session.setDayNumber(request.day());
        if (request.timeRange() != null) session.setTimeRange(request.timeRange().trim());
        if (request.room() != null) session.setRoom(request.room().trim());
        if (request.status() != null && !request.status().isBlank()) session.setStatus(request.status().trim());
        if (request.speakerName() != null) session.setSpeakerName(request.speakerName());
        if (request.speakerDesignation() != null) session.setSpeakerDesignation(request.speakerDesignation());
        if (request.speakerCompany() != null) session.setSpeakerCompany(request.speakerCompany());
        if (request.description() != null) session.setDescription(request.description());
        if (request.takeaways() != null) session.setTakeawaysJson(writeJson(request.takeaways()));

        sessionRepository.save(session);
        log.info("Updated session {} ({})", session.getTitle(), sessionId);

        return mapToSessionDto(session);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "sessions", allEntries = true)
    public SessionDto updateSessionStatus(String sessionId, String status) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        String validStatus = status.trim().toLowerCase();
        if (!List.of("live", "upcoming", "completed").contains(validStatus)) {
            throw new BusinessException("Invalid session status '" + status + "'. Allowed: live, upcoming, completed");
        }

        session.setStatus(validStatus);
        sessionRepository.save(session);
        log.info("Updated session {} status to {}", sessionId, validStatus);

        return mapToSessionDto(session);
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "sessions", allEntries = true)
    public void deleteSession(String sessionId) {
        if (!sessionRepository.existsById(sessionId)) {
            throw new ResourceNotFoundException("Session not found: " + sessionId);
        }
        sessionRepository.deleteById(sessionId);
        log.info("Deleted session {}", sessionId);
    }

    private String cleanField(String val) {
        if (val == null) return "";
        String s = val.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length() - 1).replace("\"\"", "\"");
        }
        return s.trim();
    }

    private String writeJson(Object obj) {
        if (obj == null) return "[]";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<String> readStringList(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private AttendeeProfileDto mapToAttendeeDto(AttendeeEntity a) {
        PrivacyConsentEntity p = a.getPrivacyConsent();
        PrivacyConsentDto privDto = p != null
                ? new PrivacyConsentDto(p.getPreset(), p.isShareEmail(), p.isSharePhone(), p.isShareLinkedIn(), p.isShareTwitter())
                : new PrivacyConsentDto(PrivacyPreset.CONNECTIONS_ONLY, true, true, true, false);

        return new AttendeeProfileDto(
                a.getId(),
                a.getName(),
                a.getAvatarUrl(),
                a.getDesignation(),
                a.getCompany(),
                a.getCategory().name().toLowerCase(),
                a.getTicketType(),
                a.getBio(),
                a.getEmail(),
                a.getMobile(),
                a.getLinkedinUrl(),
                a.getTwitterUrl(),
                a.getRelevanceScore(),
                a.isCheckedIn(),
                a.getRegistrationId(),
                privDto,
                "none",
                null,
                false,
                a.getSkillsList(),
                a.getInterestsList()
        );
    }

    private SessionDto mapToSessionDto(SessionEntity s) {
        List<String> speakerIds = readStringList(s.getSpeakerIdsJson());
        String speakerId = speakerIds.isEmpty() ? null : speakerIds.get(0);
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
                speakerIds,
                s.getDescription(),
                readStringList(s.getTakeawaysJson()),
                s.getStatus(),
                s.getAttendanceCount(),
                s.getQuestionsCount(),
                false
        );
    }
}
