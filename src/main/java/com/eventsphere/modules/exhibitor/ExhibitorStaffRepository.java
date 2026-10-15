package com.eventsphere.modules.exhibitor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExhibitorStaffRepository extends JpaRepository<ExhibitorStaffEntity, ExhibitorStaffId> {

    Optional<ExhibitorStaffEntity> findByIdAttendeeId(String attendeeId);

    List<ExhibitorStaffEntity> findAllByIdExhibitorId(String exhibitorId);

    boolean existsByIdExhibitorIdAndIdAttendeeId(String exhibitorId, String attendeeId);
}
