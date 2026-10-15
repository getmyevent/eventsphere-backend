package com.eventsphere.modules.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionBookmarkRepository extends JpaRepository<SessionBookmarkEntity, SessionBookmarkId> {

    boolean existsByIdSessionIdAndIdAttendeeId(String sessionId, String attendeeId);

    Optional<SessionBookmarkEntity> findByIdSessionIdAndIdAttendeeId(String sessionId, String attendeeId);

    List<SessionBookmarkEntity> findByIdAttendeeId(String attendeeId);

    void deleteByIdSessionIdAndIdAttendeeId(String sessionId, String attendeeId);
}
