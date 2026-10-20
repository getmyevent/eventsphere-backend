package com.eventsphere.modules.auth.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class SmtpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;
    private final String fromEmail;

    public SmtpEmailService(JavaMailSender mailSender, String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail != null && !fromEmail.isBlank() ? fromEmail : "login@eventsphere.com";
    }

    @Override
    public void sendOtp(String recipientEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(recipientEmail);
            message.setSubject("Your EventSphere Login OTP: " + otp);
            message.setText("Your EventSphere 6-digit login verification code is: " + otp + "\n\n"
                    + "This code expires in 5 minutes. If you did not request this code, please ignore this email.");

            mailSender.send(message);
            log.info("OTP email successfully dispatched via SMTP to {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed to send OTP email via SMTP to {}: {}", recipientEmail, e.getMessage());
        }
    }
}
