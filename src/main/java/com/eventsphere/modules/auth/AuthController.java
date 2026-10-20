package com.eventsphere.modules.auth;

import com.eventsphere.common.security.EventSphereUserPrincipal;
import com.eventsphere.modules.auth.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/auth", "/api/v1/auth"})
public class AuthController {

    private final AuthService authService;
    private final GoogleAuthService googleAuthService;
    private final EmailOtpService emailOtpService;

    public AuthController(
            AuthService authService,
            GoogleAuthService googleAuthService,
            EmailOtpService emailOtpService
    ) {
        this.authService = authService;
        this.googleAuthService = googleAuthService;
        this.emailOtpService = emailOtpService;
    }

    @PostMapping("/google")
    public ResponseEntity<AccessResponse> googleAuth(@Valid @RequestBody GoogleAuthRequest request) {
        AccessResponse response = googleAuthService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping({"/email/request-otp", "/email/send-otp"})
    public ResponseEntity<Map<String, Object>> requestOtp(@Valid @RequestBody RequestOtpRequest request) {
        emailOtpService.requestOtp(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "OTP sent successfully to " + request.getEmail()
        ));
    }

    @PostMapping("/email/verify-otp")
    public ResponseEntity<AccessResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AccessResponse response = emailOtpService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/access")
    public ResponseEntity<AccessResponse> access(
            @Valid @RequestBody AccessRequest request,
            HttpServletRequest servletRequest
    ) {
        String clientIp = extractClientIp(servletRequest);
        AccessResponse response = authService.authenticate(request, clientIp);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserSummaryDto> getCurrentUser(
            @AuthenticationPrincipal EventSphereUserPrincipal principal
    ) {
        UserSummaryDto userSummary = authService.getCurrentUserSummary(principal);
        return ResponseEntity.ok(userSummary);
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
