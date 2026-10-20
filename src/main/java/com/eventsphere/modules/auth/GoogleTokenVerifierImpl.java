package com.eventsphere.modules.auth;

import com.eventsphere.common.error.BusinessException;
import com.eventsphere.modules.auth.dto.GooglePayload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class GoogleTokenVerifierImpl implements GoogleTokenVerifier {

    private static final Logger log = LoggerFactory.getLogger(GoogleTokenVerifierImpl.class);

    private final GoogleIdTokenVerifier verifier;
    private final String clientId;

    public GoogleTokenVerifierImpl(@Value("${eventsphere.auth.google.client-id:eventsphere-dev-client-id}") String clientId) {
        this.clientId = clientId;
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    @Override
    public GooglePayload verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                log.warn("Invalid Google ID token signature or token expired");
                throw new BusinessException("Invalid or expired Google ID token", HttpStatus.UNAUTHORIZED);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                log.warn("Google account email not verified: {}", payload.getEmail());
                throw new BusinessException("Google email address is not verified", HttpStatus.UNAUTHORIZED);
            }

            return new GooglePayload(
                    payload.getEmail(),
                    payload.getEmailVerified(),
                    (String) payload.get("name"),
                    (String) payload.get("picture"),
                    payload.getSubject()
            );
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google token verification failed: {}", e.getMessage());
            throw new BusinessException("Google ID token verification failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }
}
