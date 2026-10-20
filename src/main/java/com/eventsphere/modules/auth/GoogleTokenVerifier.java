package com.eventsphere.modules.auth;

import com.eventsphere.modules.auth.dto.GooglePayload;

public interface GoogleTokenVerifier {

    GooglePayload verify(String idTokenString);
}
