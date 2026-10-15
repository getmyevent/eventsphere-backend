package com.eventsphere.modules.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, String> {

    List<SessionEntity> findAllByEventIdOrderByDayNumberAsc(String eventId);

    List<SessionEntity> findByEventIdAndDayNumberOrderByTimeRangeAsc(String eventId, int dayNumber);

    long countByEventId(String eventId);

    long countByEventIdAndStatus(String eventId, String status);
}
