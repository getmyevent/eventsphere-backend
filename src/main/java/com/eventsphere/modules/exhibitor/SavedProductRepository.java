package com.eventsphere.modules.exhibitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedProductRepository extends JpaRepository<SavedProductEntity, SavedProductId> {

    List<SavedProductEntity> findAllByAttendeeId(String attendeeId);

    boolean existsByAttendeeIdAndTargetProductId(String attendeeId, String targetProductId);

    void deleteByAttendeeIdAndTargetProductId(String attendeeId, String targetProductId);

    long countByAttendeeId(String attendeeId);
}
