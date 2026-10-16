package com.eventsphere.modules.session;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<SessionEntity, String> {

    List<SessionEntity> findAllByEventIdOrderByDayNumberAsc(String eventId);

    List<SessionEntity> findByEventIdAndDayNumberOrderByTimeRangeAsc(String eventId, int dayNumber);

    long countByEventId(String eventId);

    long countByEventIdAndStatus(String eventId, String status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE SessionEntity s SET s.questionsCount = s.questionsCount + 1 WHERE s.id = :id")
    int incrementQuestionsCount(@Param("id") String id);
}
