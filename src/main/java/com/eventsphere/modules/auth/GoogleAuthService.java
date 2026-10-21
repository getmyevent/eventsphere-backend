package com.eventsphere.modules.auth;

import com.eventsphere.common.error.DelegateNotFoundException;
import com.eventsphere.modules.attendee.AttendeeEntity;
import com.eventsphere.modules.attendee.AttendeeRepository;
import com.eventsphere.modules.auth.dto.AccessResponse;
import com.eventsphere.modules.auth.dto.GoogleAuthRequest;
import com.eventsphere.modules.auth.dto.GooglePayload;
import com.eventsphere.modules.auth.dto.UserSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class GoogleAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthService.class);

    private final GoogleTokenVerifier googleTokenVerifier;
    private final AttendeeRepository attendeeRepository;
    private final EmailOtpService emailOtpService;

    public GoogleAuthService(
            GoogleTokenVerifier googleTokenVerifier,
            AttendeeRepository attendeeRepository,
            EmailOtpService emailOtpService
    ) {
        this.googleTokenVerifier = googleTokenVerifier;
        this.attendeeRepository = attendeeRepository;
        this.emailOtpService = emailOtpService;
    }

    public AccessResponse authenticate(GoogleAuthRequest request) {
        // 1. Cryptographic ID token verification
        GooglePayload payload = googleTokenVerifier.verify(request.getCredential());

        // 2. Normalize email
        String normalizedEmail = payload.email().trim().toLowerCase(Locale.ROOT);

        // 3. Database match by indexed email
        Optional<AttendeeEntity> attendeeOpt = attendeeRepository.findFirstByEmailIgnoreCase(normalizedEmail);
        if (attendeeOpt.isEmpty()) {
            String candidateName = (request.getName() != null && !request.getName().isBlank())
                    ? request.getName().trim()
                    : (payload.name() != null && !payload.name().isBlank() ? payload.name().trim() : null);

            if (candidateName != null && !candidateName.isBlank()) {
                String eventId = (request.getEventId() != null && !request.getEventId().isBlank())
                        ? request.getEventId()
                        : "MPSS26";
                log.info("Auto-registering Google authenticated delegate: email={}, name={}, eventId={}",
                        normalizedEmail, candidateName, eventId);
                return emailOtpService.registerDelegate(eventId, normalizedEmail, candidateName);
            }
            log.info("Google authentication for unregistered email={}. Requiring profile creation.", normalizedEmail);
            AccessResponse response = new AccessResponse();
            response.setNewUser(true);
            UserSummaryDto userSummary = new UserSummaryDto();
            userSummary.setEmail(normalizedEmail);
            userSummary.setName(payload.name());
            userSummary.setEventId(request.getEventId() != null ? request.getEventId() : "MPSS26");
            response.setUser(userSummary);
            return response;
        }

        AttendeeEntity attendee = attendeeOpt.get();
        log.info("Google authentication successful for delegateId={} email={}", attendee.getId(), normalizedEmail);

        // 4. Issue standard EventSphere session JWT and profile
        return emailOtpService.buildAccessResponse(attendee);
    }
}
