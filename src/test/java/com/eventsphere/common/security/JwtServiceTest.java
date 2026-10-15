package com.eventsphere.common.security;

import com.eventsphere.modules.attendee.model.AttendeeCategory;
import com.eventsphere.modules.attendee.model.UserRole;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private final String secret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(secret, expirationMs);
    }

    @Test
    @DisplayName("Should generate valid token with claims and parse back correctly")
    void shouldGenerateAndParseToken() {
        EventSphereUserPrincipal principal = new EventSphereUserPrincipal(
                "att-123",
                "MPSS26",
                "Jane Doe",
                "jane@example.com",
                UserRole.ROLE_EXHIBITOR,
                AttendeeCategory.EXHIBITOR_STAFF,
                "exh-alpha"
        );

        String token = jwtService.generateToken(principal);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();

        EventSphereUserPrincipal parsed = jwtService.parseUserPrincipal(token);
        assertThat(parsed.getAttendeeId()).isEqualTo("att-123");
        assertThat(parsed.getEventId()).isEqualTo("MPSS26");
        assertThat(parsed.getName()).isEqualTo("Jane Doe");
        assertThat(parsed.getEmail()).isEqualTo("jane@example.com");
        assertThat(parsed.getRole()).isEqualTo(UserRole.ROLE_EXHIBITOR);
        assertThat(parsed.getCategory()).isEqualTo(AttendeeCategory.EXHIBITOR_STAFF);
        assertThat(parsed.getExhibitorId()).isEqualTo("exh-alpha");
    }

    @Test
    @DisplayName("Should reject tampered token")
    void shouldRejectTamperedToken() {
        EventSphereUserPrincipal principal = new EventSphereUserPrincipal(
                "att-123",
                "MPSS26",
                "Jane Doe",
                "jane@example.com",
                UserRole.ROLE_DELEGATE,
                AttendeeCategory.DELEGATE,
                null
        );

        String token = jwtService.generateToken(principal);
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";

        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }

    @Test
    @DisplayName("Should reject expired token")
    void shouldRejectExpiredToken() {
        // Service with negative expiration ms so token is expired immediately
        JwtService expiredService = new JwtService(secret, -1000);

        EventSphereUserPrincipal principal = new EventSphereUserPrincipal(
                "att-123",
                "MPSS26",
                "Jane Doe",
                "jane@example.com",
                UserRole.ROLE_DELEGATE,
                AttendeeCategory.DELEGATE,
                null
        );

        String token = expiredService.generateToken(principal);
        assertThat(jwtService.isTokenValid(token)).isFalse();
    }
}
