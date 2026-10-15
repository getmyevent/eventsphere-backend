package com.eventsphere.modules.connection;

import com.eventsphere.common.security.RateLimiterService;
import com.eventsphere.modules.attendee.AttendeeEntity;
import com.eventsphere.modules.attendee.AttendeeRepository;
import com.eventsphere.modules.attendee.model.AttendeeCategory;
import com.eventsphere.modules.attendee.model.UserRole;
import com.eventsphere.modules.auth.dto.AccessRequest;
import com.eventsphere.modules.connection.dto.SendConnectionRequest;
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
class ConnectionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

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

        // 2. User A: Delegate
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
        attendeeRepository.save(userA);

        // 3. User B: VIP
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
        attendeeRepository.save(userB);

        // 4. User C: Speaker
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
        userC.setDesignation("Professor");
        attendeeRepository.save(userC);

        userAToken = obtainToken("9811111111");
        userBToken = obtainToken("9822222222");
        userCToken = obtainToken("9833333333");
    }

    @Test
    @DisplayName("Send connection request creates pending connection with type 'sent'")
    void testSendConnectionRequest() throws Exception {
        SendConnectionRequest request = new SendConnectionRequest("att-user-b", "recommended");

        MvcResult result = mockMvc.perform(post("/api/v1/connections")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.personId", is("att-user-b")))
                .andExpect(jsonPath("$.personName", is("Priya Sharma")))
                .andExpect(jsonPath("$.status", is("pending")))
                .andExpect(jsonPath("$.type", is("sent")))
                .andExpect(jsonPath("$.source", is("recommended")))
                .andReturn();

        String connId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        // Check from User B's perspective: should see type 'received'
        mockMvc.perform(get("/api/v1/connections")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(connId)))
                .andExpect(jsonPath("$[0].personId", is("att-user-a")))
                .andExpect(jsonPath("$[0].personName", is("Aarav Mehta")))
                .andExpect(jsonPath("$[0].type", is("received")))
                .andExpect(jsonPath("$[0].status", is("pending")));
    }

    @Test
    @DisplayName("Cannot send connection request to self")
    void testCannotConnectToSelf() throws Exception {
        SendConnectionRequest request = new SendConnectionRequest("att-user-a", "direct");

        mockMvc.perform(post("/api/v1/connections")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", containsString("Cannot connect with yourself")));
    }

    @Test
    @DisplayName("Recipient can accept connection; third-party is rejected with 403 Forbidden")
    void testAcceptConnectionRequest() throws Exception {
        // User A sends request to User B
        SendConnectionRequest req = new SendConnectionRequest("att-user-b", "direct");
        MvcResult sendRes = mockMvc.perform(post("/api/v1/connections")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String connId = objectMapper.readTree(sendRes.getResponse().getContentAsString()).get("id").asText();

        // 1. User C (third party) tries to accept -> 403 Forbidden
        mockMvc.perform(post("/api/v1/connections/" + connId + "/accept")
                        .header("Authorization", "Bearer " + userCToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));

        // 2. User B (recipient) accepts -> 200 OK, status = connected
        mockMvc.perform(post("/api/v1/connections/" + connId + "/accept")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(connId)))
                .andExpect(jsonPath("$.status", is("connected")));

        // Verify persisted in DB
        ConnectionEntity updated = connectionRepository.findById(connId).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo("connected");
    }

    @Test
    @DisplayName("Mutual intent auto-accept: sending request when incoming pending exists auto-connects")
    void testMutualIntentAutoAccept() throws Exception {
        // Step 1: User A sends request to User B
        SendConnectionRequest reqA = new SendConnectionRequest("att-user-b", "direct");
        mockMvc.perform(post("/api/v1/connections")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqA)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("pending")));

        // Step 2: User B sends request back to User A -> Mutual intent auto-accepts!
        SendConnectionRequest reqB = new SendConnectionRequest("att-user-a", "direct");
        mockMvc.perform(post("/api/v1/connections")
                        .header("Authorization", "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqB)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("connected")));

        // Invariant check: Verify exactly ONE canonical row exists for (A, B) in DB!
        String userLow = "att-user-a".compareTo("att-user-b") <= 0 ? "att-user-a" : "att-user-b";
        String userHigh = "att-user-a".compareTo("att-user-b") <= 0 ? "att-user-b" : "att-user-a";
        ConnectionEntity conn = connectionRepository.findByEventIdAndUserLowAndUserHigh(EVENT_ID, userLow, userHigh).orElseThrow();
        assertThat(conn.getStatus()).isEqualTo("connected");
    }

    @Test
    @DisplayName("DELETE /api/v1/connections/{connectionId} removes connection")
    void testRemoveConnection() throws Exception {
        // Create connection
        SendConnectionRequest req = new SendConnectionRequest("att-user-b", "direct");
        MvcResult res = mockMvc.perform(post("/api/v1/connections")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        String connId = objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asText();

        // Remove connection
        mockMvc.perform(delete("/api/v1/connections/" + connId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isNoContent());

        // Verify deleted from DB
        assertThat(connectionRepository.findById(connId)).isEmpty();
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
