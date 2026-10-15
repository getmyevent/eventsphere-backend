package com.eventsphere.modules.auth;

import com.eventsphere.common.security.EventSphereUserPrincipal;
import com.eventsphere.modules.auth.dto.AccessRequest;
import com.eventsphere.modules.auth.dto.AccessResponse;
import com.eventsphere.modules.auth.dto.UserSummaryDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
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
