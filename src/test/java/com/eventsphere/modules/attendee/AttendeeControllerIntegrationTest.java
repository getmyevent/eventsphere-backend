package com.eventsphere.modules.attendee;

import com.eventsphere.common.security.RateLimiterService;
import com.eventsphere.modules.attendee.dto.UpdatePrivacyRequest;
import com.eventsphere.modules.attendee.dto.UpdateProfileRequest;
import com.eventsphere.modules.attendee.model.AttendeeCategory;
import com.eventsphere.modules.attendee.model.PrivacyPreset;
import com.eventsphere.modules.attendee.model.UserRole;
import com.eventsphere.modules.auth.dto.AccessRequest;
import com.eventsphere.modules.connection.ConnectionEntity;
import com.eventsphere.modules.connection.ConnectionRepository;
import com.eventsphere.modules.event.EventEntity;
import com.eventsphere.modules.event.EventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AttendeeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private PrivacyConsentRepository privacyConsentRepository;

    @Autowired
    private SavedPersonRepository savedPersonRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    @Autowired
    private RateLimiterService rateLimiterService;

    private static final String EVENT_ID = "MPSS26";

    private String userAToken;
    private String userBToken;
    private String userCToken;

    @BeforeEach
    void setUp() throws Exception {
        rateLimiterService.reset();

        // 1. Seed Event
        EventEntity event = new EventEntity(
                EVENT_ID,
                "MP Semiconductor Summit 2026",
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                "23–24 Oct 2026",
                "Indore",
                "Brilliant Convention Centre",
                "UPCOMING"
        );
        eventRepository.save(event);

        // 2. User A: Delegate (Viewer)
        AttendeeEntity userA = new AttendeeEntity();
        userA.setId("att-user-a");
        userA.setEventId(EVENT_ID);
        userA.setRegistrationId("#REG-A");
        userA.setName("Aarav Mehta");
        userA.setEmail("aarav@ti.com");
        userA.setMobile("+91 98111 11111");
        userA.setNormalizedMobile("9811111111");
        userA.setCategory(AttendeeCategory.DELEGATE);
        userA.setAuthRole(UserRole.ROLE_DELEGATE);
        userA.setCompany("Texas Instruments");
        userA.setDesignation("Staff Architect");
        userA.setTicketType("Delegate");
        PrivacyConsentEntity privA = new PrivacyConsentEntity("att-user-a", PrivacyPreset.CONNECTIONS_ONLY, true, true, true, false);
        privA.setAttendee(userA);
        userA.setPrivacyConsent(privA);
        attendeeRepository.save(userA);

        // 3. User B: VIP (Connections Only privacy)
        AttendeeEntity userB = new AttendeeEntity();
        userB.setId("att-user-b");
        userB.setEventId(EVENT_ID);
        userB.setRegistrationId("#REG-B");
        userB.setName("Priya Sharma");
        userB.setEmail("priya@qualcomm.com");
        userB.setMobile("+91 98222 22222");
        userB.setNormalizedMobile("9822222222");
        userB.setCategory(AttendeeCategory.VIP);
        userB.setAuthRole(UserRole.ROLE_DELEGATE);
        userB.setCompany("Qualcomm");
        userB.setDesignation("Senior Director");
        userB.setTicketType("VIP");
        userB.setLinkedinUrl("https://linkedin.com/in/priyasharma");
        PrivacyConsentEntity privB = new PrivacyConsentEntity("att-user-b", PrivacyPreset.CONNECTIONS_ONLY, true, true, true, false);
        privB.setAttendee(userB);
        userB.setPrivacyConsent(privB);
        attendeeRepository.save(userB);

        // 4. User C: Speaker (Open privacy)
        AttendeeEntity userC = new AttendeeEntity();
        userC.setId("att-user-c");
        userC.setEventId(EVENT_ID);
        userC.setRegistrationId("#REG-C");
        userC.setName("Dr. Rajesh Kumar");
        userC.setEmail("rajesh@iitb.ac.in");
        userC.setMobile("+91 98333 33333");
        userC.setNormalizedMobile("9833333333");
        userC.setCategory(AttendeeCategory.SPEAKER);
        userC.setAuthRole(UserRole.ROLE_DELEGATE);
        userC.setCompany("IIT Bombay");
        userC.setDesignation("Professor of Microelectronics");
        userC.setTicketType("Speaker");
        userC.setLinkedinUrl("https://linkedin.com/in/drrajesh");
        PrivacyConsentEntity privC = new PrivacyConsentEntity("att-user-c", PrivacyPreset.OPEN, true, true, true, true);
        privC.setAttendee(userC);
        userC.setPrivacyConsent(privC);
        attendeeRepository.save(userC);

        // Obtain tokens
        userAToken = obtainToken("9811111111");
        userBToken = obtainToken("9822222222");
        userCToken = obtainToken("9833333333");
    }

    @Test
    @DisplayName("Directory search returns attendees excluding self")
    void testDirectoryBrowsingExcludesSelf() throws Exception {
        mockMvc.perform(get("/api/v1/attendees")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // User B and User C (A excluded)
                .andExpect(jsonPath("$[*].id", not(hasItem("att-user-a"))));
    }

    @Test
    @DisplayName("Privacy enforcement: Contacts masked when preset is CONNECTIONS_ONLY and not connected")
    void testPrivacyMaskedForNonConnectedAttendee() throws Exception {
        // User A views User B (Connections Only, not connected)
        mockMvc.perform(get("/api/v1/attendees/att-user-b")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("att-user-b")))
                .andExpect(jsonPath("$.name", is("Priya Sharma")))
                .andExpect(jsonPath("$.email", is("contact@eventsphere.internal")))
                .andExpect(jsonPath("$.mobile", is("+91 98765 00000")))
                .andExpect(jsonPath("$.linkedin").doesNotExist())
                .andExpect(jsonPath("$.connectionStatus", is("none")));
    }

    @Test
    @DisplayName("Privacy enforcement: Contacts revealed when connection status is 'connected'")
    void testPrivacyRevealedWhenConnected() throws Exception {
        // Establish connection between User A and User B
        ConnectionEntity conn = new ConnectionEntity(
                "conn-ab",
                EVENT_ID,
                "att-user-a",
                "att-user-b",
                "connected",
                "direct"
        );
        connectionRepository.save(conn);

        // User A views User B after connecting
        mockMvc.perform(get("/api/v1/attendees/att-user-b")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("priya@qualcomm.com")))
                .andExpect(jsonPath("$.mobile", is("+91 98222 22222")))
                .andExpect(jsonPath("$.linkedin", is("https://linkedin.com/in/priyasharma")))
                .andExpect(jsonPath("$.connectionStatus", is("connected")));
    }

    @Test
    @DisplayName("Privacy enforcement: Contacts revealed when preset is OPEN even without connection")
    void testPrivacyRevealedWhenPresetIsOpen() throws Exception {
        // User A views User C (Open privacy preset, no connection)
        mockMvc.perform(get("/api/v1/attendees/att-user-c")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("rajesh@iitb.ac.in")))
                .andExpect(jsonPath("$.mobile", is("+91 98333 33333")))
                .andExpect(jsonPath("$.linkedin", is("https://linkedin.com/in/drrajesh")))
                .andExpect(jsonPath("$.connectionStatus", is("none")));
    }

    @Test
    @DisplayName("GET /api/v1/attendees/me returns own full profile and privacy settings")
    void testGetMyProfile() throws Exception {
        mockMvc.perform(get("/api/v1/attendees/me")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("att-user-a")))
                .andExpect(jsonPath("$.name", is("Aarav Mehta")))
                .andExpect(jsonPath("$.email", is("aarav@ti.com")))
                .andExpect(jsonPath("$.mobile", is("+91 98111 11111")))
                .andExpect(jsonPath("$.privacy.preset", is("Connections Only")))
                .andExpect(jsonPath("$.privacy.shareEmail", is(true)));
    }

    @Test
    @DisplayName("PUT /api/v1/attendees/me updates profile details")
    void testUpdateMyProfile() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "Aarav Mehta, Ph.D.",
                "Principal Architect",
                "Texas Instruments R&D",
                "Specializing in high-speed analog and mixed-signal design.",
                "/images/avatars/aarav.png",
                "https://linkedin.com/in/aaravmehta-phd",
                "https://x.com/aaravmehta",
                "aarav.phd@ti.com",
                "+91 98111 99999"
        );

        mockMvc.perform(put("/api/v1/attendees/me")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Aarav Mehta, Ph.D.")))
                .andExpect(jsonPath("$.designation", is("Principal Architect")))
                .andExpect(jsonPath("$.company", is("Texas Instruments R&D")))
                .andExpect(jsonPath("$.email", is("aarav.phd@ti.com")))
                .andExpect(jsonPath("$.mobile", is("+91 98111 99999")));

        // Verify persisted in DB
        AttendeeEntity updated = attendeeRepository.findById("att-user-a").orElseThrow();
        assertThat(updated.getName()).isEqualTo("Aarav Mehta, Ph.D.");
        assertThat(updated.getNormalizedMobile()).isEqualTo("9811199999");
    }

    @Test
    @DisplayName("PUT /api/v1/attendees/me/privacy updates privacy preset and flags")
    void testUpdateMyPrivacy() throws Exception {
        UpdatePrivacyRequest request = new UpdatePrivacyRequest(
                PrivacyPreset.OPEN,
                true,
                false, // Hide phone
                true,
                true
        );

        mockMvc.perform(put("/api/v1/attendees/me/privacy")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preset", is("Open")))
                .andExpect(jsonPath("$.shareEmail", is(true)))
                .andExpect(jsonPath("$.sharePhone", is(false)))
                .andExpect(jsonPath("$.shareTwitter", is(true)));

        // Verify in DB
        PrivacyConsentEntity consent = privacyConsentRepository.findByAttendeeId("att-user-a").orElseThrow();
        assertThat(consent.getPreset()).isEqualTo(PrivacyPreset.OPEN);
        assertThat(consent.isSharePhone()).isFalse();
    }

    @Test
    @DisplayName("Save and Unsave person bookmarks work cleanly")
    void testSaveAndUnsavePerson() throws Exception {
        // 1. Save User B
        mockMvc.perform(post("/api/v1/attendees/att-user-b/save")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved", is(true)));

        // Verify in DB
        assertThat(savedPersonRepository.existsByAttendeeIdAndTargetPersonId("att-user-a", "att-user-b")).isTrue();

        // 2. Get saved people
        mockMvc.perform(get("/api/v1/attendees/saved")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("att-user-b")))
                .andExpect(jsonPath("$[0].isSaved", is(true)));

        // 3. Unsave User B
        mockMvc.perform(delete("/api/v1/attendees/att-user-b/save")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saved", is(false)));

        // Verify in DB
        assertThat(savedPersonRepository.existsByAttendeeIdAndTargetPersonId("att-user-a", "att-user-b")).isFalse();
    }

    private String obtainToken(String mobile) throws Exception {
        AccessRequest request = new AccessRequest(EVENT_ID, mobile);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String json = result.getResponse().getContentAsString();
        return objectMapper.readTree(json).get("token").asText();
    }
}
