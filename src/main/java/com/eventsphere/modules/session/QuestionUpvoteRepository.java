package com.eventsphere.modules.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionUpvoteRepository extends JpaRepository<QuestionUpvoteEntity, QuestionUpvoteId> {

    boolean existsByIdQuestionIdAndIdAttendeeId(String questionId, String attendeeId);

    Optional<QuestionUpvoteEntity> findByIdQuestionIdAndIdAttendeeId(String questionId, String attendeeId);

    void deleteByIdQuestionIdAndIdAttendeeId(String questionId, String attendeeId);

    List<QuestionUpvoteEntity> findByIdAttendeeId(String attendeeId);
}
