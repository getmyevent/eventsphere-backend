package com.eventsphere.common.error;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/test");
        request.addHeader("X-Correlation-Id", "test-corr-id-123");
    }

    @Test
    void handlesBusinessException() {
        BusinessException ex = new BusinessException("Duplicate submission detected", HttpStatus.CONFLICT);
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().status());
        assertEquals("Duplicate submission detected", response.getBody().message());
        assertEquals("test-corr-id-123", response.getBody().correlationId());
        assertEquals("/api/v1/test", response.getBody().path());
    }

    @Test
    void handlesResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Attendee", "att-001");
        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(ex, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertTrue(response.getBody().message().contains("Attendee not found with identifier: att-001"));
    }

    @Test
    void handlesUnexpectedExceptionSafely() {
        RuntimeException ex = new RuntimeException("Database timeout unexpected detail");
        ResponseEntity<ErrorResponse> response = handler.handleUnexpected(ex, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        // Verify internal details/stack traces are never exposed to clients
        assertFalse(response.getBody().message().contains("Database timeout"));
        assertTrue(response.getBody().message().contains("Please contact event support"));
        assertEquals("test-corr-id-123", response.getBody().correlationId());
    }
}
