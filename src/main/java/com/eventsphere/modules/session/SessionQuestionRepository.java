package com.eventsphere.modules.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionQuestionRepository extends JpaRepository<SessionQuestionEntity, String> {

    List<SessionQuestionEntity> findBySessionIdOrderByUpvotesDescCreatedAtAsc(String sessionId);

    List<SessionQuestionEntity> findBySessionIdOrderByCreatedAtDesc(String sessionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SessionQuestionEntity q SET q.upvotes = q.upvotes + 1 WHERE q.id = :id")
    int incrementUpvotes(@Param("id") String id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SessionQuestionEntity q SET q.upvotes = CASE WHEN q.upvotes > 0 THEN q.upvotes - 1 ELSE 0 END WHERE q.id = :id")
    int decrementUpvotes(@Param("id") String id);

    @Query("SELECT q.upvotes FROM SessionQuestionEntity q WHERE q.id = :id")
    Integer getUpvotesCount(@Param("id") String id);
}
