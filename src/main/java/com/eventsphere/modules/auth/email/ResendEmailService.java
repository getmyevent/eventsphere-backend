package com.eventsphere.modules.auth.email;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class ResendEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    private final String apiKey;
    private final String fromEmail;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ResendEmailService(String apiKey, String fromEmail) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.fromEmail = fromEmail != null && !fromEmail.isBlank() ? fromEmail : "EventSphere <onboarding@resend.dev>";
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void sendOtp(String recipientEmail, String otp) {
        if (apiKey.isBlank()) {
            log.warn("Resend API key is not configured. Falling back to log output. OTP for {}: {}", recipientEmail, otp);
            return;
        }

        try {
            String htmlContent = """
                    <div style="font-family: sans-serif; max-width: 480px; margin: 0 auto; padding: 24px; border: 1px solid #e2e8f0; border-radius: 8px;">
                        <h2 style="color: #0f172a; margin-bottom: 16px;">EventSphere Login Verification</h2>
                        <p style="color: #475569; font-size: 15px;">Your one-time login verification code is:</p>
                        <div style="background-color: #f1f5f9; padding: 16px; border-radius: 6px; text-align: center; margin: 20px 0;">
                            <span style="font-size: 32px; font-weight: bold; letter-spacing: 6px; color: #1e293b;">%s</span>
                        </div>
                        <p style="color: #64748b; font-size: 13px;">This code is valid for 5 minutes. Do not share this code with anyone.</p>
                    </div>
                    """.formatted(otp);

            Map<String, Object> body = Map.of(
                    "from", fromEmail,
                    "to", List.of(recipientEmail),
                    "subject", "Your EventSphere Login OTP: " + otp,
                    "html", htmlContent
            );

            String requestBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("OTP email successfully dispatched via Resend to {}", recipientEmail);
            } else {
                log.error("Failed to send OTP email via Resend [status={}]: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("Exception occurred while sending email via Resend to {}: {}", recipientEmail, e.getMessage());
        }
    }
}
