package com.eventsphere.modules.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PollVoteRepository extends JpaRepository<PollVoteEntity, PollVoteId> {

    Optional<PollVoteEntity> findByIdPollIdAndIdAttendeeId(String pollId, String attendeeId);

    boolean existsByIdPollIdAndIdAttendeeId(String pollId, String attendeeId);

    void deleteByIdPollIdAndIdAttendeeId(String pollId, String attendeeId);

    List<PollVoteEntity> findByIdPollId(String pollId);
}
