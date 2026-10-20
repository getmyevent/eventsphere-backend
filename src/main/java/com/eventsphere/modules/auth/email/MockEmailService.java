package com.eventsphere.modules.auth.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(MockEmailService.class);

    @Override
    public void sendOtp(String recipientEmail, String otp) {
        log.info("[MOCK EMAIL DISPATCH] Delivering OTP [{}] to recipient: {}", otp, recipientEmail);
    }
}
