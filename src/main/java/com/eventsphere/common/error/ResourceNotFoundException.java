package com.eventsphere.common.error;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }

    public ResourceNotFoundException(String resourceName, String identifier) {
        super("%s not found with identifier: %s".formatted(resourceName, identifier), HttpStatus.NOT_FOUND);
    }
}
