package com.eventsphere.modules.attendee;

import com.eventsphere.modules.attendee.model.AttendeeCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendeeRepository extends JpaRepository<AttendeeEntity, String> {

    Optional<AttendeeEntity> findByEventIdAndNormalizedMobile(String eventId, String normalizedMobile);

    Optional<AttendeeEntity> findByEventIdAndRegistrationId(String eventId, String registrationId);

    Optional<AttendeeEntity> findByIdAndEventId(String id, String eventId);

    List<AttendeeEntity> findAllByEventId(String eventId);

    long countByEventId(String eventId);

    long countByEventIdAndCheckedInTrue(String eventId);

    long countByEventIdAndCategory(String eventId, AttendeeCategory category);

    boolean existsByEventIdAndNormalizedMobile(String eventId, String normalizedMobile);

    @Query("""
        SELECT a FROM AttendeeEntity a
        LEFT JOIN FETCH a.privacyConsent
        WHERE a.eventId = :eventId
          AND a.category != :excludedCategory
          AND (:query IS NULL OR :query = '' OR (
              LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
              LOWER(a.company) LIKE LOWER(CONCAT('%', :query, '%')) OR
              LOWER(a.designation) LIKE LOWER(CONCAT('%', :query, '%')) OR
              LOWER(a.email) LIKE LOWER(CONCAT('%', :query, '%'))
          ))
        ORDER BY a.name ASC
    """)
    List<AttendeeEntity> searchCandidates(
        @Param("eventId") String eventId,
        @Param("query") String query,
        @Param("excludedCategory") AttendeeCategory excludedCategory,
        Pageable pageable
    );

    @Query("""
        SELECT a FROM AttendeeEntity a
        LEFT JOIN FETCH a.privacyConsent
        WHERE a.eventId = :eventId
          AND a.category IN :categories
          AND (:query IS NULL OR :query = '' OR (
              LOWER(a.name) LIKE LOWER(CONCAT('%', :query, '%')) OR
              LOWER(a.company) LIKE LOWER(CONCAT('%', :query, '%')) OR
              LOWER(a.designation) LIKE LOWER(CONCAT('%', :query, '%')) OR
              LOWER(a.email) LIKE LOWER(CONCAT('%', :query, '%'))
          ))
        ORDER BY a.name ASC
    """)
    List<AttendeeEntity> searchCandidatesByCategories(
        @Param("eventId") String eventId,
        @Param("query") String query,
        @Param("categories") List<AttendeeCategory> categories,
        Pageable pageable
    );
}
