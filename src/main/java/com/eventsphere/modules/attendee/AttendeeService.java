package com.eventsphere.modules.attendee;

import com.eventsphere.common.error.BusinessException;
import com.eventsphere.common.error.ResourceNotFoundException;
import com.eventsphere.modules.attendee.dto.*;
import com.eventsphere.modules.attendee.model.AttendeeCategory;
import com.eventsphere.modules.attendee.model.PrivacyPreset;
import com.eventsphere.modules.connection.ConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendeeService {

    private static final Logger log = LoggerFactory.getLogger(AttendeeService.class);

    private final AttendeeRepository attendeeRepository;
    private final PrivacyConsentRepository privacyConsentRepository;
    private final SavedPersonRepository savedPersonRepository;
    private final ConnectionService connectionService;

    public AttendeeService(
            AttendeeRepository attendeeRepository,
            PrivacyConsentRepository privacyConsentRepository,
            SavedPersonRepository savedPersonRepository,
            ConnectionService connectionService
    ) {
        this.attendeeRepository = attendeeRepository;
        this.privacyConsentRepository = privacyConsentRepository;
        this.savedPersonRepository = savedPersonRepository;
        this.connectionService = connectionService;
    }

    @Transactional(readOnly = true)
    public List<AttendeeProfileDto> getDirectory(
            String eventId,
            String viewerId,
            String query,
            String category,
            boolean savedOnly,
            int page,
            int size
    ) {
        int sanitizedPage = Math.max(page, 0);
        int sanitizedSize = (size > 0 && size <= 100) ? size : 30;
        Pageable pageable = PageRequest.of(sanitizedPage, sanitizedSize);

        String searchQuery = (query != null && !query.isBlank()) ? query.trim() : null;

        List<AttendeeEntity> attendees;
        if (category != null && !category.isBlank()) {
            AttendeeCategory cat = AttendeeCategory.fromValue(category);
            attendees = attendeeRepository.searchCandidatesByCategories(eventId, searchQuery, List.of(cat), pageable);
        } else {
            // Include all attendees except EXHIBITOR_STAFF or show all
            attendees = attendeeRepository.searchCandidates(eventId, searchQuery, AttendeeCategory.EXHIBITOR_STAFF, pageable);
        }

        Set<String> savedPersonIds = savedPersonRepository.findAllByAttendeeId(viewerId).stream()
                .map(SavedPersonEntity::getTargetPersonId)
                .collect(Collectors.toSet());

        return attendees.stream()
                .filter(a -> !a.getId().equals(viewerId)) // Filter out self
                .filter(a -> !savedOnly || savedPersonIds.contains(a.getId()))
                .map(a -> {
                    ConnectionService.ConnectionStatusInfo connInfo =
                            connectionService.getConnectionStatusInfo(eventId, viewerId, a.getId());
                    boolean isSaved = savedPersonIds.contains(a.getId());
                    return toProfileDto(a, viewerId, connInfo, isSaved);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AttendeeProfileDto getAttendeeProfile(String eventId, String viewerId, String targetId) {
        AttendeeEntity target = attendeeRepository.findByIdAndEventId(targetId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendee", targetId));

        ConnectionService.ConnectionStatusInfo connInfo =
                connectionService.getConnectionStatusInfo(eventId, viewerId, targetId);
        boolean isSaved = savedPersonRepository.existsByAttendeeIdAndTargetPersonId(viewerId, targetId);

        return toProfileDto(target, viewerId, connInfo, isSaved);
    }

    public AttendeeProfileDto updateMyProfile(String eventId, String viewerId, UpdateProfileRequest request) {
        AttendeeEntity attendee = attendeeRepository.findByIdAndEventId(viewerId, eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendee", viewerId));

        if (request.getName() != null && !request.getName().isBlank()) attendee.setName(request.getName().trim());
        if (request.getDesignation() != null) attendee.setDesignation(request.getDesignation().trim());
        if (request.getCompany() != null) attendee.setCompany(request.getCompany().trim());
        if (request.getBio() != null) attendee.setBio(request.getBio().trim());
        if (request.getAvatar() != null) attendee.setAvatarUrl(request.getAvatar().trim());
        if (request.getLinkedin() != null) attendee.setLinkedinUrl(request.getLinkedin().trim());
        if (request.getTwitter() != null) attendee.setTwitterUrl(request.getTwitter().trim());

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            attendee.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getMobile() != null && !request.getMobile().isBlank()) {
            attendee.setMobile(request.getMobile().trim());
            String digits = request.getMobile().replaceAll("\\D", "");
            if (digits.startsWith("91") && digits.length() > 10) digits = digits.substring(2);
            if (digits.startsWith("0")) digits = digits.substring(1);
            attendee.setNormalizedMobile(digits);
        }

        AttendeeEntity saved = attendeeRepository.save(attendee);
        log.info("Updated profile for attendee {}", viewerId);

        ConnectionService.ConnectionStatusInfo connInfo = new ConnectionService.ConnectionStatusInfo("none", null);
        return toProfileDto(saved, viewerId, connInfo, false);
    }

    public PrivacyConsentDto updateMyPrivacy(String viewerId, UpdatePrivacyRequest request) {
        AttendeeEntity attendee = attendeeRepository.findById(viewerId)
                .orElseThrow(() -> new ResourceNotFoundException("Attendee", viewerId));

        PrivacyConsentEntity privacy = attendee.getPrivacyConsent();
        if (privacy == null) {
            privacy = new PrivacyConsentEntity();
            privacy.setAttendee(attendee);
            privacy.setAttendeeId(viewerId);
        }

        if (request.getPreset() != null) privacy.setPreset(request.getPreset());
        if (request.getShareEmail() != null) privacy.setShareEmail(request.getShareEmail());
        if (request.getSharePhone() != null) privacy.setSharePhone(request.getSharePhone());
        if (request.getShareLinkedIn() != null) privacy.setShareLinkedIn(request.getShareLinkedIn());
        if (request.getShareTwitter() != null) privacy.setShareTwitter(request.getShareTwitter());

        PrivacyConsentEntity saved = privacyConsentRepository.save(privacy);
        log.info("Updated privacy consent for attendee {}: preset={}", viewerId, saved.getPreset());

        return new PrivacyConsentDto(
                saved.getPreset(),
                saved.isShareEmail(),
                saved.isSharePhone(),
                saved.isShareLinkedIn(),
                saved.isShareTwitter()
        );
    }

    public void savePerson(String viewerId, String targetPersonId) {
        if (viewerId.equals(targetPersonId)) {
            throw new BusinessException("Cannot save your own profile");
        }

        if (!attendeeRepository.existsById(targetPersonId)) {
            throw new ResourceNotFoundException("Target attendee not found: " + targetPersonId);
        }

        if (!savedPersonRepository.existsByAttendeeIdAndTargetPersonId(viewerId, targetPersonId)) {
            savedPersonRepository.save(new SavedPersonEntity(viewerId, targetPersonId));
            log.info("Attendee {} saved person {}", viewerId, targetPersonId);
        }
    }

    public void unsavePerson(String viewerId, String targetPersonId) {
        savedPersonRepository.deleteByAttendeeIdAndTargetPersonId(viewerId, targetPersonId);
        log.info("Attendee {} unsaved person {}", viewerId, targetPersonId);
    }

    @Transactional(readOnly = true)
    public List<AttendeeProfileDto> getSavedPeople(String eventId, String viewerId) {
        List<SavedPersonEntity> saved = savedPersonRepository.findAllByAttendeeId(viewerId);
        Set<String> targetIds = saved.stream().map(SavedPersonEntity::getTargetPersonId).collect(Collectors.toSet());

        List<AttendeeEntity> targets = attendeeRepository.findAllById(targetIds);

        return targets.stream().map(target -> {
            ConnectionService.ConnectionStatusInfo connInfo =
                    connectionService.getConnectionStatusInfo(eventId, viewerId, target.getId());
            return toProfileDto(target, viewerId, connInfo, true);
        }).collect(Collectors.toList());
    }

    // Privacy Masking Enforcement
    private AttendeeProfileDto toProfileDto(
            AttendeeEntity target,
            String viewerId,
            ConnectionService.ConnectionStatusInfo connInfo,
            boolean isSaved
    ) {
        boolean isSelf = target.getId().equals(viewerId);
        boolean isConnected = "connected".equalsIgnoreCase(connInfo.getStatus());

        PrivacyConsentEntity privacy = target.getPrivacyConsent();
        PrivacyPreset preset = (privacy != null) ? privacy.getPreset() : PrivacyPreset.CONNECTIONS_ONLY;

        boolean canShareEmail;
        boolean canSharePhone;
        boolean canShareLinkedIn;
        boolean canShareTwitter;

        if (isSelf) {
            canShareEmail = true;
            canSharePhone = true;
            canShareLinkedIn = true;
            canShareTwitter = true;
        } else if (preset == PrivacyPreset.CONNECTIONS_ONLY) {
            if (isConnected) {
                canShareEmail = (privacy == null) || privacy.isShareEmail();
                canSharePhone = (privacy == null) || privacy.isSharePhone();
                canShareLinkedIn = (privacy == null) || privacy.isShareLinkedIn();
                canShareTwitter = (privacy != null) && privacy.isShareTwitter();
            } else {
                canShareEmail = false;
                canSharePhone = false;
                canShareLinkedIn = false;
                canShareTwitter = false;
            }
        } else { // OPEN
            canShareEmail = (privacy == null) || privacy.isShareEmail();
            canSharePhone = (privacy == null) || privacy.isSharePhone();
            canShareLinkedIn = (privacy == null) || privacy.isShareLinkedIn();
            canShareTwitter = (privacy != null) && privacy.isShareTwitter();
        }

        String email = canShareEmail ? target.getEmail() : "contact@eventsphere.internal";
        String mobile = canSharePhone ? target.getMobile() : "+91 98765 00000";
        String linkedin = canShareLinkedIn ? target.getLinkedinUrl() : null;
        String twitter = canShareTwitter ? target.getTwitterUrl() : null;

        PrivacyConsentDto privacyDto = null;
        if (isSelf && privacy != null) {
            privacyDto = new PrivacyConsentDto(
                    privacy.getPreset(),
                    privacy.isShareEmail(),
                    privacy.isSharePhone(),
                    privacy.isShareLinkedIn(),
                    privacy.isShareTwitter()
            );
        }

        return new AttendeeProfileDto(
                target.getId(),
                target.getName(),
                target.getAvatarUrl(),
                target.getDesignation(),
                target.getCompany(),
                target.getCategory().name().toLowerCase(),
                target.getTicketType(),
                target.getBio(),
                email,
                mobile,
                linkedin,
                twitter,
                target.getRelevanceScore(),
                target.isCheckedIn(),
                target.getRegistrationId(),
                privacyDto,
                connInfo.getStatus(),
                connInfo.getConnectionId(),
                isSaved,
                List.of(), // skills
                List.of()  // interests
        );
    }
}
