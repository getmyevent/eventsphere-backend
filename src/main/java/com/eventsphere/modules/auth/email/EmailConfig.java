package com.eventsphere.modules.auth.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class EmailConfig {

    private static final Logger log = LoggerFactory.getLogger(EmailConfig.class);

    @Value("${eventsphere.email.provider:mock}")
    private String emailProvider;

    @Value("${eventsphere.email.from:EventSphere <onboarding@resend.dev>}")
    private String fromEmail;

    @Value("${eventsphere.email.resend.api-key:}")
    private String resendApiKey;

    @Bean
    public EmailService emailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        String provider = emailProvider.trim().toLowerCase();
        log.info("Configuring EmailService provider: {}", provider);

        return switch (provider) {
            case "resend" -> new ResendEmailService(resendApiKey, fromEmail);
            case "smtp" -> {
                JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
                if (mailSender == null) {
                    log.warn("SMTP requested but JavaMailSender bean not configured. Falling back to MockEmailService");
                    yield new MockEmailService();
                }
                yield new SmtpEmailService(mailSender, fromEmail);
            }
            default -> new MockEmailService();
        };
    }
}
