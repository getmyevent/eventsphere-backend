package com.eventsphere.common.security;

import com.eventsphere.modules.attendee.model.AttendeeCategory;
import com.eventsphere.modules.attendee.model.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final String secret;
    private final long expirationMs;

    public JwtService(
            @Value("${eventsphere.jwt.secret}") String secret,
            @Value("${eventsphere.jwt.expiration-ms:259200000}") long expirationMs
    ) {
        if (secret == null || secret.trim().length() < 32) {
            throw new IllegalArgumentException("JWT secret must be configured and at least 256 bits (32 bytes) long");
        }
        this.secret = secret.trim();
        this.expirationMs = expirationMs;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(EventSphereUserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(expirationMs);

        var builder = Jwts.builder()
                .subject(principal.getAttendeeId())
                .claim("eventId", principal.getEventId())
                .claim("name", principal.getName())
                .claim("email", principal.getEmail())
                .claim("role", principal.getRole().name())
                .claim("category", principal.getCategory().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey(), Jwts.SIG.HS256);

        if (principal.getExhibitorId() != null) {
            builder.claim("exhibitorId", principal.getExhibitorId());
        }

        return builder.compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration() != null && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public EventSphereUserPrincipal parseUserPrincipal(String token) {
        Claims claims = extractAllClaims(token);
        String attendeeId = claims.getSubject();
        String eventId = claims.get("eventId", String.class);
        String name = claims.get("name", String.class);
        String email = claims.get("email", String.class);
        String roleStr = claims.get("role", String.class);
        String categoryStr = claims.get("category", String.class);
        String exhibitorId = claims.get("exhibitorId", String.class);

        UserRole role = UserRole.valueOf(roleStr);
        AttendeeCategory category = AttendeeCategory.valueOf(categoryStr);

        return new EventSphereUserPrincipal(
                attendeeId,
                eventId,
                name,
                email,
                role,
                category,
                exhibitorId
        );
    }

    public long getExpirationSeconds() {
        return expirationMs / 1000;
    }
}
