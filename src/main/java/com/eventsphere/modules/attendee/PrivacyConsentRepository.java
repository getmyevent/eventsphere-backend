package com.eventsphere.modules.attendee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrivacyConsentRepository extends JpaRepository<PrivacyConsentEntity, String> {

    Optional<PrivacyConsentEntity> findByAttendeeId(String attendeeId);
}
