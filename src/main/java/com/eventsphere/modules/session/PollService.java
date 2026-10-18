package com.eventsphere.modules.session;

import com.eventsphere.common.error.BusinessException;
import com.eventsphere.common.error.ResourceNotFoundException;
import com.eventsphere.modules.session.dto.PollDto;
import com.eventsphere.modules.session.dto.PollOptionDto;
import com.eventsphere.modules.session.dto.VotePollRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PollService {

    private static final Logger log = LoggerFactory.getLogger(PollService.class);

    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final SessionRepository sessionRepository;
    private final PollStreamService pollStreamService;

    public PollService(
            PollRepository pollRepository,
            PollOptionRepository pollOptionRepository,
            PollVoteRepository pollVoteRepository,
            SessionRepository sessionRepository,
            PollStreamService pollStreamService
    ) {
        this.pollRepository = pollRepository;
        this.pollOptionRepository = pollOptionRepository;
        this.pollVoteRepository = pollVoteRepository;
        this.sessionRepository = sessionRepository;
        this.pollStreamService = pollStreamService;
    }

    @Transactional(readOnly = true)
    public List<PollDto> getPollsForSession(String sessionId, String currentAttendeeId) {
        List<PollEntity> polls = pollRepository.findAllBySessionIdOrderByCreatedAtDesc(sessionId);
        return polls.stream()
                .map(p -> mapToDto(p, currentAttendeeId))
                .toList();
    }

    @Transactional(readOnly = true)
    public PollDto getPollById(String pollId, String currentAttendeeId) {
        PollEntity poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found: " + pollId));
        return mapToDto(poll, currentAttendeeId);
    }

    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter subscribeToPoll(String pollId, String currentAttendeeId) {
        PollDto snapshot = getPollById(pollId, currentAttendeeId);
        return pollStreamService.subscribe(pollId, snapshot);
    }

    @Transactional
    public PollDto vote(String pollId, VotePollRequest request, String attendeeId) {
        PollEntity poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found: " + pollId));

        if ("closed".equalsIgnoreCase(poll.getStatus())) {
            throw new BusinessException("Poll is closed for voting");
        }

        // Architectural invariant: option_id must strictly belong to the specified poll
        PollOptionEntity option = pollOptionRepository.findByPollIdAndId(pollId, request.optionId())
                .orElseThrow(() -> new BusinessException("Option '" + request.optionId() + "' does not belong to poll '" + pollId + "'"));

        // Single vote per attendee per poll
        Optional<PollVoteEntity> existingVote = pollVoteRepository.findByIdPollIdAndIdAttendeeId(pollId, attendeeId);
        if (existingVote.isPresent()) {
            throw new BusinessException("You have already voted in this poll");
        }

        // Record vote atomically
        try {
            pollVoteRepository.saveAndFlush(new PollVoteEntity(pollId, attendeeId, option.getId()));
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            throw new BusinessException("You have already voted in this poll");
        }

        // Update vote tallies atomically via DB UPDATE queries
        pollOptionRepository.incrementVotes(option.getId());
        pollRepository.incrementTotalVotes(pollId);

        log.info("Attendee {} voted for option {} in poll {}", attendeeId, option.getId(), pollId);

        PollEntity updatedPoll = pollRepository.findById(pollId).orElse(poll);
        PollDto voterResponse = mapToDto(updatedPoll, true, option.getId());

        // Instant fanout to all connected SSE clients with updated counts
        PollDto publicBroadcast = mapToDto(updatedPoll, false, null);
        pollStreamService.broadcast(pollId, publicBroadcast);

        return voterResponse;
    }

    @Transactional
    public PollDto resetVote(String pollId, String attendeeId) {
        PollEntity poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found: " + pollId));

        Optional<PollVoteEntity> existingVote = pollVoteRepository.findByIdPollIdAndIdAttendeeId(pollId, attendeeId);
        if (existingVote.isEmpty()) {
            return mapToDto(poll, attendeeId);
        }

        PollVoteEntity vote = existingVote.get();
        pollOptionRepository.decrementVotes(vote.getOptionId());
        pollVoteRepository.delete(vote);
        pollVoteRepository.flush();

        pollRepository.decrementTotalVotes(pollId);

        log.info("Attendee {} reset vote in poll {}", attendeeId, pollId);

        PollEntity updatedPoll = pollRepository.findById(pollId).orElse(poll);
        PollDto response = mapToDto(updatedPoll, false, null);

        // Instant fanout to all connected SSE clients with updated counts
        pollStreamService.broadcast(pollId, response);

        return response;
    }

    @Transactional
    public PollDto createPoll(com.eventsphere.modules.session.dto.CreatePollRequest request) {
        if (!sessionRepository.existsById(request.sessionId())) {
            throw new ResourceNotFoundException("Session not found: " + request.sessionId());
        }

        String pollId = "poll_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        PollEntity poll = new PollEntity();
        poll.setId(pollId);
        poll.setSessionId(request.sessionId());
        poll.setQuestion(request.question().trim());
        poll.setStatus("open");
        poll.setTotalVotes(0);

        List<PollOptionEntity> optionEntities = new java.util.ArrayList<>();
        for (int i = 0; i < request.options().size(); i++) {
            PollOptionEntity opt = new PollOptionEntity();
            opt.setId("opt_" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 10));
            opt.setPoll(poll);
            opt.setText(request.options().get(i).trim());
            opt.setVotes(0);
            opt.setSortOrder(i + 1);
            optionEntities.add(opt);
        }
        poll.setOptions(optionEntities);

        PollEntity saved = pollRepository.save(poll);
        log.info("Organizer created new live poll {} for session {}", pollId, request.sessionId());

        PollDto dto = mapToDto(saved, false, null);
        pollStreamService.broadcast(pollId, dto);
        return dto;
    }

    @Transactional
    public PollDto updatePollStatus(String pollId, String status) {
        PollEntity poll = pollRepository.findById(pollId)
                .orElseThrow(() -> new ResourceNotFoundException("Poll not found: " + pollId));

        String normalizedStatus = status.trim().toLowerCase();
        if (!"open".equals(normalizedStatus) && !"closed".equals(normalizedStatus)) {
            throw new BusinessException("Poll status must be either 'open' or 'closed'");
        }

        poll.setStatus(normalizedStatus);
        PollEntity saved = pollRepository.save(poll);
        log.info("Organizer updated poll {} status to {}", pollId, normalizedStatus);

        PollDto dto = mapToDto(saved, false, null);
        pollStreamService.broadcast(pollId, dto);
        return dto;
    }

    @Transactional(readOnly = true)
    public List<PollDto> getAllPolls(String currentAttendeeId) {
        return pollRepository.findAll().stream()
                .map(p -> mapToDto(p, currentAttendeeId))
                .toList();
    }

    private PollDto mapToDto(PollEntity poll, String currentAttendeeId) {
        Optional<PollVoteEntity> userVote = (currentAttendeeId != null)
                ? pollVoteRepository.findByIdPollIdAndIdAttendeeId(poll.getId(), currentAttendeeId)
                : Optional.empty();

        return mapToDto(poll, userVote.isPresent(), userVote.map(PollVoteEntity::getOptionId).orElse(null));
    }

    private PollDto mapToDto(PollEntity poll, boolean hasVoted, String votedOptionId) {
        List<PollOptionEntity> options = pollOptionRepository.findAllByPollIdOrderBySortOrderAsc(poll.getId());

        List<PollOptionDto> optionDtos = options.stream()
                .map(o -> new PollOptionDto(o.getId(), o.getText(), o.getVotes(), o.getSortOrder()))
                .toList();

        return new PollDto(
                poll.getId(),
                poll.getSessionId(),
                poll.getQuestion(),
                poll.getStatus(),
                poll.getTotalVotes(),
                optionDtos,
                hasVoted,
                votedOptionId
        );
    }
}
