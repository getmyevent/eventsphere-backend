package com.eventsphere.modules.attendee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedPersonRepository extends JpaRepository<SavedPersonEntity, SavedPersonId> {

    List<SavedPersonEntity> findAllByAttendeeId(String attendeeId);

    boolean existsByAttendeeIdAndTargetPersonId(String attendeeId, String targetPersonId);

    void deleteByAttendeeIdAndTargetPersonId(String attendeeId, String targetPersonId);

    long countByAttendeeId(String attendeeId);
}
