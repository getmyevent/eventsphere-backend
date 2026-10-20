package com.eventsphere.modules.auth;

import com.eventsphere.common.error.BusinessException;
import com.eventsphere.common.error.DelegateNotFoundException;
import com.eventsphere.common.error.OtpCooldownException;
import com.eventsphere.common.security.JwtService;
import com.eventsphere.modules.attendee.AttendeeEntity;
import com.eventsphere.modules.attendee.AttendeeRepository;
import com.eventsphere.modules.attendee.model.AttendeeCategory;
import com.eventsphere.modules.attendee.model.UserRole;
import com.eventsphere.modules.auth.dto.AccessResponse;
import com.eventsphere.modules.auth.email.EmailService;
import com.eventsphere.modules.exhibitor.ExhibitorRepository;
import com.eventsphere.modules.exhibitor.ExhibitorStaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailOtpServiceTest {

    @Mock
    private AttendeeRepository attendeeRepository;

    @Mock
    private ExhibitorStaffRepository exhibitorStaffRepository;

    @Mock
    private ExhibitorRepository exhibitorRepository;

    @Mock
    private EmailService emailService;

    private JwtService jwtService;
    private EmailOtpService emailOtpService;
    private AttendeeEntity delegate;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970", 259200000L);

        emailOtpService = new EmailOtpService(
                attendeeRepository,
                exhibitorStaffRepository,
                exhibitorRepository,
                jwtService,
                emailService
        );

        delegate = new AttendeeEntity();
        delegate.setId("att-101");
        delegate.setEventId("MPSS26");
        delegate.setName("Test Delegate");
        delegate.setEmail("delegate@eventsphere.com");
        delegate.setAuthRole(UserRole.ROLE_DELEGATE);
        delegate.setCategory(AttendeeCategory.DELEGATE);
    }

    @Test
    @DisplayName("Request OTP dispatches email asynchronously and enters cooldown")
    void testRequestOtpSuccessAndCooldown() {
        when(attendeeRepository.findFirstByEmailIgnoreCase("delegate@eventsphere.com"))
                .thenReturn(Optional.of(delegate));

        emailOtpService.requestOtp("delegate@eventsphere.com");

        // Verify cooldown is triggered on immediate second request
        assertThatThrownBy(() -> emailOtpService.requestOtp("delegate@eventsphere.com"))
                .isInstanceOf(OtpCooldownException.class)
                .hasMessageContaining("45 seconds");
    }

    @Test
    @DisplayName("Request OTP fails with 404 DELEGATE_NOT_FOUND when email is unregistered")
    void testRequestOtpUnregisteredEmailThrows404() {
        when(attendeeRepository.findFirstByEmailIgnoreCase("unregistered@domain.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailOtpService.requestOtp("unregistered@domain.com"))
                .isInstanceOf(DelegateNotFoundException.class)
                .hasMessageContaining("This email is not registered as an event delegate.");
    }

    @Test
    @DisplayName("Verify OTP succeeds and issues valid AccessResponse")
    void testVerifyOtpSuccess() {
        when(attendeeRepository.findFirstByEmailIgnoreCase("delegate@eventsphere.com"))
                .thenReturn(Optional.of(delegate));

        emailOtpService.seedOtpForTesting("delegate@eventsphere.com", "123456");

        AccessResponse response = emailOtpService.verifyOtp("delegate@eventsphere.com", "123456");

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNotBlank();
        assertThat(response.getUser().getEmail()).isEqualTo("delegate@eventsphere.com");
    }

    @Test
    @DisplayName("Verify OTP with invalid code decrements remaining attempts")
    void testVerifyOtpInvalidCode() {
        emailOtpService.seedOtpForTesting("delegate@eventsphere.com", "123456");

        assertThatThrownBy(() -> emailOtpService.verifyOtp("delegate@eventsphere.com", "999999"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("4 attempts remaining");
    }

    @Test
    @DisplayName("Verify OTP locks out after 5 consecutive failed attempts")
    void testVerifyOtpBruteForceLockout() {
        emailOtpService.seedOtpForTesting("delegate@eventsphere.com", "123456");

        // 4 failed attempts
        for (int i = 0; i < 4; i++) {
            assertThatThrownBy(() -> emailOtpService.verifyOtp("delegate@eventsphere.com", "000000"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST);
        }

        // 5th failed attempt should trigger lockout
        assertThatThrownBy(() -> emailOtpService.verifyOtp("delegate@eventsphere.com", "000000"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.TOO_MANY_REQUESTS)
                .hasMessageContaining("Maximum verification attempts exceeded");

        // 6th attempt should be rejected as expired/invalid
        assertThatThrownBy(() -> emailOtpService.verifyOtp("delegate@eventsphere.com", "123456"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("status", HttpStatus.BAD_REQUEST)
                .hasMessageContaining("OTP is invalid or has expired");
    }
}
