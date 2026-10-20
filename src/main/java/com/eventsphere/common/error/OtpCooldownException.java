package com.eventsphere.common.error;

import org.springframework.http.HttpStatus;

public class OtpCooldownException extends BusinessException {

    public OtpCooldownException(String message) {
        super(message, HttpStatus.TOO_MANY_REQUESTS);
    }
}
