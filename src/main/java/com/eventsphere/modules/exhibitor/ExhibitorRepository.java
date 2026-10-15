package com.eventsphere.modules.exhibitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExhibitorRepository extends JpaRepository<ExhibitorEntity, String> {

    List<ExhibitorEntity> findAllByEventId(String eventId);

    long countByEventId(String eventId);

    long countByEventIdAndStatus(String eventId, String status);
}
