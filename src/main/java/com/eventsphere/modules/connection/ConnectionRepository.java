package com.eventsphere.modules.connection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConnectionRepository extends JpaRepository<ConnectionEntity, String> {

    Optional<ConnectionEntity> findByEventIdAndUserLowAndUserHigh(String eventId, String userLow, String userHigh);

    @Query("SELECT c FROM ConnectionEntity c WHERE c.eventId = :eventId AND (c.senderId = :attendeeId OR c.receiverId = :attendeeId)")
    List<ConnectionEntity> findAllByAttendee(@Param("eventId") String eventId, @Param("attendeeId") String attendeeId);

    long countByEventIdAndStatus(String eventId, String status);
}
