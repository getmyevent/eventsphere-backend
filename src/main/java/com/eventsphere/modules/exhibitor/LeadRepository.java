package com.eventsphere.modules.exhibitor;

import com.eventsphere.modules.exhibitor.model.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRepository extends JpaRepository<LeadEntity, String> {

    List<LeadEntity> findAllByExhibitorIdOrderByCapturedAtDesc(String exhibitorId);

    List<LeadEntity> findAllByExhibitorIdAndStatusOrderByCapturedAtDesc(String exhibitorId, LeadStatus status);

    Optional<LeadEntity> findByIdAndExhibitorId(String id, String exhibitorId);

    Optional<LeadEntity> findByExhibitorIdAndAttendeeId(String exhibitorId, String attendeeId);

    long countByExhibitorId(String exhibitorId);

    long countByExhibitorIdAndStatus(String exhibitorId, LeadStatus status);

    @Query("SELECT COUNT(l) FROM LeadEntity l WHERE l.exhibitor.eventId = :eventId")
    long countByEventId(@Param("eventId") String eventId);

    @Query("""
        SELECT l FROM LeadEntity l
        JOIN FETCH l.attendee a
        LEFT JOIN FETCH a.privacyConsent
        WHERE l.exhibitorId = :exhibitorId
        ORDER BY l.capturedAt DESC
    """)
    List<LeadEntity> findAllWithAttendeeByExhibitorId(@Param("exhibitorId") String exhibitorId);

    @Query("""
        SELECT l FROM LeadEntity l
        JOIN FETCH l.attendee a
        LEFT JOIN FETCH a.privacyConsent
        WHERE l.exhibitorId = :exhibitorId AND l.status = :status
        ORDER BY l.capturedAt DESC
    """)
    List<LeadEntity> findAllWithAttendeeByExhibitorIdAndStatus(
        @Param("exhibitorId") String exhibitorId,
        @Param("status") LeadStatus status
    );

    @Query("""
        SELECT l FROM LeadEntity l
        JOIN FETCH l.attendee a
        LEFT JOIN FETCH a.privacyConsent
        WHERE l.id = :id AND l.exhibitorId = :exhibitorId
    """)
    Optional<LeadEntity> findWithAttendeeByIdAndExhibitorId(
        @Param("id") String id,
        @Param("exhibitorId") String exhibitorId
    );
}
