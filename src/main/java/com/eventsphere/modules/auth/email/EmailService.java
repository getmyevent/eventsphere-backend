package com.eventsphere.modules.auth.email;

public interface EmailService {

    void sendOtp(String recipientEmail, String otp);
}
