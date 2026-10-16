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

    public PollService(
            PollRepository pollRepository,
            PollOptionRepository pollOptionRepository,
            PollVoteRepository pollVoteRepository
    ) {
        this.pollRepository = pollRepository;
        this.pollOptionRepository = pollOptionRepository;
        this.pollVoteRepository = pollVoteRepository;
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
        return mapToDto(updatedPoll, attendeeId);
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
        return mapToDto(updatedPoll, attendeeId);
    }

    private PollDto mapToDto(PollEntity poll, String currentAttendeeId) {
        Optional<PollVoteEntity> userVote = (currentAttendeeId != null)
                ? pollVoteRepository.findByIdPollIdAndIdAttendeeId(poll.getId(), currentAttendeeId)
                : Optional.empty();

        boolean hasVoted = userVote.isPresent();
        String votedOptionId = userVote.map(PollVoteEntity::getOptionId).orElse(null);

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
