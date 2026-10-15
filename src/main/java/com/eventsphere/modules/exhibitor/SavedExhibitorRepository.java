package com.eventsphere.modules.exhibitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedExhibitorRepository extends JpaRepository<SavedExhibitorEntity, SavedExhibitorId> {

    List<SavedExhibitorEntity> findAllByAttendeeId(String attendeeId);

    boolean existsByAttendeeIdAndTargetExhibitorId(String attendeeId, String targetExhibitorId);

    void deleteByAttendeeIdAndTargetExhibitorId(String attendeeId, String targetExhibitorId);

    long countByAttendeeId(String attendeeId);
}
