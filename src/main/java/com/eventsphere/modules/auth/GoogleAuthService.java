package com.eventsphere.modules.auth;

import com.eventsphere.common.error.DelegateNotFoundException;
import com.eventsphere.modules.attendee.AttendeeEntity;
import com.eventsphere.modules.attendee.AttendeeRepository;
import com.eventsphere.modules.auth.dto.AccessResponse;
import com.eventsphere.modules.auth.dto.GoogleAuthRequest;
import com.eventsphere.modules.auth.dto.GooglePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;

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
        AttendeeEntity attendee = attendeeRepository.findFirstByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new DelegateNotFoundException("This Google account is not registered as an event delegate."));

        log.info("Google authentication successful for delegateId={} email={}", attendee.getId(), normalizedEmail);

        // 4. Issue standard EventSphere session JWT and profile
        return emailOtpService.buildAccessResponse(attendee);
    }
}
