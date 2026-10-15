package com.eventsphere.modules.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionQuestionRepository extends JpaRepository<SessionQuestionEntity, String> {

    List<SessionQuestionEntity> findBySessionIdOrderByUpvotesDescCreatedAtAsc(String sessionId);

    List<SessionQuestionEntity> findBySessionIdOrderByCreatedAtDesc(String sessionId);
}
