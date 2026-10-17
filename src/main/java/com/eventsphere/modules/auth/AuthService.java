package com.eventsphere.modules.auth;

import com.eventsphere.common.error.ResourceNotFoundException;
import com.eventsphere.common.security.EventSphereUserPrincipal;
import com.eventsphere.common.security.JwtService;
import com.eventsphere.common.security.RateLimiterService;
import com.eventsphere.common.util.MobileNormalizer;
import com.eventsphere.modules.attendee.AttendeeEntity;
import com.eventsphere.modules.attendee.AttendeeRepository;
import com.eventsphere.modules.attendee.model.AttendeeCategory;
import com.eventsphere.modules.attendee.model.UserRole;
import com.eventsphere.modules.auth.dto.AccessRequest;
import com.eventsphere.modules.auth.dto.AccessResponse;
import com.eventsphere.modules.auth.dto.UserSummaryDto;
import com.eventsphere.modules.exhibitor.ExhibitorEntity;
import com.eventsphere.modules.exhibitor.ExhibitorRepository;
import com.eventsphere.modules.exhibitor.ExhibitorStaffEntity;
import com.eventsphere.modules.exhibitor.ExhibitorStaffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AttendeeRepository attendeeRepository;
    private final ExhibitorStaffRepository exhibitorStaffRepository;
    private final ExhibitorRepository exhibitorRepository;
    private final JwtService jwtService;
    private final RateLimiterService rateLimiterService;

    public AuthService(
            AttendeeRepository attendeeRepository,
            ExhibitorStaffRepository exhibitorStaffRepository,
            ExhibitorRepository exhibitorRepository,
            JwtService jwtService,
            RateLimiterService rateLimiterService
    ) {
        this.attendeeRepository = attendeeRepository;
        this.exhibitorStaffRepository = exhibitorStaffRepository;
        this.exhibitorRepository = exhibitorRepository;
        this.jwtService = jwtService;
        this.rateLimiterService = rateLimiterService;
    }

    public AccessResponse authenticate(AccessRequest request, String clientIp) {
        long tTotal0 = System.nanoTime();

        // 1. Rate limiting check (in-memory)
        long tRateLimit0 = System.nanoTime();
        rateLimiterService.checkRateLimit(clientIp, request.getMobile());
        long rateLimitMs = (System.nanoTime() - tRateLimit0) / 1_000_000;

        // 2. Normalize mobile
        String normalizedMobile = normalizeMobile(request.getMobile());
        if (normalizedMobile.isBlank()) {
            throw new ResourceNotFoundException(
                    "No registration found for mobile number '" + request.getMobile() + "' in event '" + request.getEventId() + "'"
            );
        }

        // 3. Find attendee by event and normalized mobile (isolated indexed DB lookup)
        long tDbLookup0 = System.nanoTime();
        AttendeeEntity attendee = findAttendeeByMobile(request.getEventId(), normalizedMobile)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No registration found for mobile number '" + request.getMobile() + "' in event '" + request.getEventId() + "'"
                ));
        long dbLookupMs = (System.nanoTime() - tDbLookup0) / 1_000_000;

        // 4. Resolve Exhibitor Scope if role is EXHIBITOR
        long tExhibitor0 = System.nanoTime();
        String exhibitorId = null;
        String boothNumber = null;

        if (attendee.getAuthRole() == UserRole.ROLE_EXHIBITOR) {
            Optional<ExhibitorStaffEntity> staffOpt = exhibitorStaffRepository.findFirstByIdAttendeeId(attendee.getId());
            if (staffOpt.isPresent()) {
                exhibitorId = staffOpt.get().getId().getExhibitorId();
                Optional<ExhibitorEntity> exhibitorOpt = exhibitorRepository.findById(exhibitorId);
                if (exhibitorOpt.isPresent()) {
                    boothNumber = exhibitorOpt.get().getBooth();
                }
            }
        }
        long exhibitorMs = (System.nanoTime() - tExhibitor0) / 1_000_000;

        // 5. Generate Principal & JWT
        long tJwt0 = System.nanoTime();
        EventSphereUserPrincipal principal = new EventSphereUserPrincipal(
                attendee.getId(),
                attendee.getEventId(),
                attendee.getName(),
                attendee.getEmail(),
                attendee.getAuthRole(),
                attendee.getCategory(),
                exhibitorId
        );

        String token = jwtService.generateToken(principal);
        long jwtMs = (System.nanoTime() - tJwt0) / 1_000_000;

        // 6. Build response
        UserSummaryDto userDto = new UserSummaryDto(
                attendee.getId(),
                attendee.getName(),
                attendee.getCategory().getValue(),
                attendee.getAuthRole().name(),
                exhibitorId,
                attendee.getEventId(),
                attendee.getCompany(),
                attendee.getDesignation(),
                attendee.getEmail(),
                attendee.getMobile(),
                attendee.getAvatarUrl(),
                attendee.getTicketType(),
                attendee.isCheckedIn(),
                boothNumber
        );

        long totalMs = (System.nanoTime() - tTotal0) / 1_000_000;
        log.info("AUTH_TRACE eventId={} maskedMobile={} totalMs={} rateLimitMs={} dbLookupMs={} exhibitorMs={} jwtMs={}",
                request.getEventId(), maskMobile(normalizedMobile), totalMs, rateLimitMs, dbLookupMs, exhibitorMs, jwtMs);

        return new AccessResponse(token, "Bearer", jwtService.getExpirationSeconds(), userDto);
    }

    @Transactional(readOnly = true)
    public UserSummaryDto getCurrentUserSummary(EventSphereUserPrincipal principal) {
        AttendeeEntity attendee = attendeeRepository.findById(principal.getAttendeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendee not found: " + principal.getAttendeeId()));

        String boothNumber = null;
        if (principal.getExhibitorId() != null) {
            Optional<ExhibitorEntity> exhibitorOpt = exhibitorRepository.findById(principal.getExhibitorId());
            if (exhibitorOpt.isPresent()) {
                boothNumber = exhibitorOpt.get().getBooth();
            }
        }

        return new UserSummaryDto(
                attendee.getId(),
                attendee.getName(),
                attendee.getCategory().getValue(),
                attendee.getAuthRole().name(),
                principal.getExhibitorId(),
                attendee.getEventId(),
                attendee.getCompany(),
                attendee.getDesignation(),
                attendee.getEmail(),
                attendee.getMobile(),
                attendee.getAvatarUrl(),
                attendee.getTicketType(),
                attendee.isCheckedIn(),
                boothNumber
        );
    }

    private Optional<AttendeeEntity> findAttendeeByMobile(String eventId, String normalizedMobile) {
        return attendeeRepository.findByEventIdAndNormalizedMobile(eventId, normalizedMobile);
    }

    private String normalizeMobile(String rawMobile) {
        return MobileNormalizer.normalize(rawMobile);
    }

    private String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 4) return "****";
        return "****" + mobile.substring(mobile.length() - 4);
    }
}
