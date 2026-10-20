package com.eventsphere.common.error;

import org.springframework.http.HttpStatus;

public class DelegateNotFoundException extends BusinessException {

    public DelegateNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
