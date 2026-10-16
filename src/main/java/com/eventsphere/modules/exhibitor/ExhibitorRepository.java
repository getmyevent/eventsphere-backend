package com.eventsphere.modules.exhibitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExhibitorRepository extends JpaRepository<ExhibitorEntity, String> {

    List<ExhibitorEntity> findAllByEventId(String eventId);

    long countByEventId(String eventId);

    long countByEventIdAndStatus(String eventId, String status);

    @org.springframework.data.jpa.repository.Query(
            "SELECT new com.eventsphere.modules.organizer.dto.OrganizerExhibitorDto(e.id, e.company, e.booth, e.category, e.logoUrl, COUNT(l.id)) " +
            "FROM ExhibitorEntity e LEFT JOIN LeadEntity l ON e.id = l.exhibitorId " +
            "WHERE e.eventId = :eventId " +
            "GROUP BY e.id, e.company, e.booth, e.category, e.logoUrl " +
            "ORDER BY e.company ASC"
    )
    List<com.eventsphere.modules.organizer.dto.OrganizerExhibitorDto> findExhibitorsWithLeadCount(@org.springframework.data.repository.query.Param("eventId") String eventId);
}
