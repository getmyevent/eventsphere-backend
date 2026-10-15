package com.eventsphere.modules.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PollRepository extends JpaRepository<PollEntity, String> {

    Optional<PollEntity> findBySessionId(String sessionId);

    List<PollEntity> findAllBySessionIdOrderByCreatedAtDesc(String sessionId);
}
