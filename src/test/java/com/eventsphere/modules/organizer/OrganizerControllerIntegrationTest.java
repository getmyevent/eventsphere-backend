package com.eventsphere.modules.organizer;

import com.eventsphere.common.security.RateLimiterService;
import com.eventsphere.modules.attendee.AttendeeEntity;
import com.eventsphere.modules.attendee.AttendeeRepository;
import com.eventsphere.modules.attendee.PrivacyConsentEntity;
import com.eventsphere.modules.attendee.PrivacyConsentRepository;
import com.eventsphere.modules.attendee.model.AttendeeCategory;
import com.eventsphere.modules.attendee.model.PrivacyPreset;
import com.eventsphere.modules.attendee.model.UserRole;
import com.eventsphere.modules.auth.dto.AccessRequest;
import com.eventsphere.modules.event.EventEntity;
import com.eventsphere.modules.event.EventRepository;
import com.eventsphere.modules.exhibitor.ExhibitorEntity;
import com.eventsphere.modules.exhibitor.ExhibitorRepository;
import com.eventsphere.modules.exhibitor.LeadEntity;
import com.eventsphere.modules.exhibitor.LeadRepository;
import com.eventsphere.modules.exhibitor.model.LeadStatus;
import com.eventsphere.modules.organizer.dto.CreateSessionRequest;
import com.eventsphere.modules.organizer.dto.UpdateSessionStatusRequest;
import com.eventsphere.modules.organizer.dto.WalkInRegistrationRequest;
import com.eventsphere.modules.session.SessionEntity;
import com.eventsphere.modules.session.SessionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrganizerControllerIntegrationTest {

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
    private ExhibitorRepository exhibitorRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private RateLimiterService rateLimiterService;

    private String tokenOrganizer;
    private String tokenDelegate;

    @BeforeEach
    void setUp() throws Exception {
        rateLimiterService.reset();

        // 1. Setup Event
        if (!eventRepository.existsById("MPSS26")) {
            EventEntity event = new EventEntity(
                    "MPSS26",
                    "Madhya Pradesh Semiconductor Summit 2026",
                    LocalDate.of(2026, 2, 12),
                    LocalDate.of(2026, 2, 13),
                    "12–13 Feb 2026",
                    "Indore",
                    "Brilliant Convention Centre",
                    "PUBLISHED"
            );
            eventRepository.save(event);
        }

        // 2. Setup Organizer Staff
        if (!attendeeRepository.existsById("att-org-staff")) {
            AttendeeEntity organizer = new AttendeeEntity();
            organizer.setId("att-org-staff");
            organizer.setEventId("MPSS26");
            organizer.setRegistrationId("#ORG-01");
            organizer.setName("Operations Admin");
            organizer.setEmail("ops@eventsphere.com");
            organizer.setMobile("+91 98765 43212");
            organizer.setNormalizedMobile("9876543212");
            organizer.setCompany("EventSphere Operations");
            organizer.setDesignation("Operations Lead");
            organizer.setCategory(AttendeeCategory.ORGANIZER_STAFF);
            organizer.setAuthRole(UserRole.ROLE_ORGANIZER);
            organizer.setTicketType("Organizer Pass");

            PrivacyConsentEntity p1 = new PrivacyConsentEntity("att-org-staff", PrivacyPreset.OPEN, true, true, true, false);
            p1.setAttendee(organizer);
            organizer.setPrivacyConsent(p1);
            attendeeRepository.save(organizer);
        }

        // 3. Setup Delegate
        if (!attendeeRepository.existsById("att-reg-delegate")) {
            AttendeeEntity delegate = new AttendeeEntity();
            delegate.setId("att-reg-delegate");
            delegate.setEventId("MPSS26");
            delegate.setRegistrationId("#DEL-01");
            delegate.setName("Rohit Sharma");
            delegate.setEmail("rohit@tech.com");
            delegate.setMobile("+91 98765 43210");
            delegate.setNormalizedMobile("9876543210");
            delegate.setCompany("Tech Innovations");
            delegate.setDesignation("Lead Engineer");
            delegate.setCategory(AttendeeCategory.DELEGATE);
            delegate.setAuthRole(UserRole.ROLE_DELEGATE);
            delegate.setTicketType("Delegate Pass");
            delegate.setCheckedIn(false);

            PrivacyConsentEntity p2 = new PrivacyConsentEntity("att-reg-delegate", PrivacyPreset.CONNECTIONS_ONLY, true, true, true, false);
            p2.setAttendee(delegate);
            delegate.setPrivacyConsent(p2);
            attendeeRepository.save(delegate);
        }

        // 4. Setup Exhibitor and Lead
        if (!exhibitorRepository.existsById("exh-alpha")) {
            ExhibitorEntity exhibitor = new ExhibitorEntity();
            exhibitor.setId("exh-alpha");
            exhibitor.setEventId("MPSS26");
            exhibitor.setCompany("Alpha Silicon Solutions");
            exhibitor.setBooth("Booth A-01");
            exhibitor.setCategory("EDA & IP");
            exhibitor.setStatus("Active");
            exhibitorRepository.save(exhibitor);

            LeadEntity lead = new LeadEntity();
            lead.setId("lead-alpha-01");
            lead.setExhibitorId("exh-alpha");
            lead.setAttendeeId("att-reg-delegate");
            lead.setStatus(LeadStatus.INTERESTED);
            lead.setSource("Booth Scan");
            leadRepository.save(lead);
        }

        // 5. Setup Session
        if (!sessionRepository.existsById("sess-org-01")) {
            SessionEntity s = new SessionEntity();
            s.setId("sess-org-01");
            s.setEventId("MPSS26");
            s.setDayNumber(1);
            s.setTimeRange("09:00 AM – 10:00 AM");
            s.setTitle("Inauguration Ceremony");
            s.setTrack("Plenary");
            s.setRoom("Auditorium 1");
            s.setSessionType("session");
            s.setStatus("upcoming");
            sessionRepository.save(s);
        }

        tokenOrganizer = loginAndGetToken("9876543212");
        tokenDelegate = loginAndGetToken("9876543210");
    }

    private String loginAndGetToken(String mobile) throws Exception {
        AccessRequest request = new AccessRequest("MPSS26", mobile);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/access")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    @DisplayName("Should retrieve organizer overview KPIs and top exhibitors")
    void shouldGetOrganizerMetricsSuccessfully() throws Exception {
        mockMvc.perform(get("/api/v1/organizer/metrics")
                        .header("Authorization", "Bearer " + tokenOrganizer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRegistered").value(greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.totalExhibitors").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.activeExhibitors").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.topExhibitors", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$.topExhibitors[0].name").value("Alpha Silicon Solutions"))
                .andExpect(jsonPath("$.topExhibitors[0].leads").value(greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("Should toggle attendee check-in and update check-in timestamp")
    void shouldToggleAttendeeCheckInSuccessfully() throws Exception {
        // 1. Check in attendee
        mockMvc.perform(post("/api/v1/organizer/attendees/att-reg-delegate/check-in")
                        .header("Authorization", "Bearer " + tokenOrganizer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attendeeId").value("att-reg-delegate"))
                .andExpect(jsonPath("$.checkedIn").value(true))
                .andExpect(jsonPath("$.checkInTime").isNotEmpty())
                .andExpect(jsonPath("$.message").value(containsString("checked in successfully")));

        // Verify in DB
        AttendeeEntity checkedInAttendee = attendeeRepository.findById("att-reg-delegate").orElseThrow();
        assertThat(checkedInAttendee.isCheckedIn()).isTrue();
        assertThat(checkedInAttendee.getCheckedInAt()).isNotNull();

        // 2. Toggle check-out
        mockMvc.perform(post("/api/v1/organizer/attendees/att-reg-delegate/check-in")
                        .header("Authorization", "Bearer " + tokenOrganizer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedIn").value(false))
                .andExpect(jsonPath("$.checkInTime").doesNotExist())
                .andExpect(jsonPath("$.message").value(containsString("check-in cancelled")));

        // Verify in DB
        AttendeeEntity checkedOutAttendee = attendeeRepository.findById("att-reg-delegate").orElseThrow();
        assertThat(checkedOutAttendee.isCheckedIn()).isFalse();
        assertThat(checkedOutAttendee.getCheckedInAt()).isNull();
    }

    @Test
    @DisplayName("Should register on-site walk-in attendee with unique registrationId")
    void shouldRegisterWalkInAttendeeSuccessfully() throws Exception {
        WalkInRegistrationRequest request = new WalkInRegistrationRequest(
                "Ananya Verma",
                "ananya@siliconcore.com",
                "9811122233",
                "Silicon Core Ltd",
                "Senior Design Engineer",
                "Full Access Delegate Pass",
                "DELEGATE"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/organizer/attendees/walk-in")
                        .header("Authorization", "Bearer " + tokenOrganizer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ananya Verma"))
                .andExpect(jsonPath("$.email").value("ananya@siliconcore.com"))
                .andExpect(jsonPath("$.registrationId").value(startsWith("#WLK-")))
                .andExpect(jsonPath("$.checkedIn").value(true)) // Walk-in is auto checked-in
                .andReturn();

        String newId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
        assertThat(attendeeRepository.existsById(newId)).isTrue();
    }

    @Test
    @DisplayName("Should reject walk-in registration with duplicate mobile number")
    void shouldRejectWalkInWithDuplicateMobile() throws Exception {
        // 9876543210 is already registered for Rohit Sharma
        WalkInRegistrationRequest request = new WalkInRegistrationRequest(
                "Duplicate Attendee",
                "dup@tech.com",
                "9876543210",
                "Duplicate Inc",
                "Staff",
                "Delegate Pass",
                "DELEGATE"
        );

        mockMvc.perform(post("/api/v1/organizer/attendees/walk-in")
                        .header("Authorization", "Bearer " + tokenOrganizer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("already registered for this event")));
    }

    @Test
    @DisplayName("Should bulk import attendees from CSV file and skip duplicates")
    void shouldImportAttendeesFromCsvSuccessfully() throws Exception {
        String csvContent = "name,email,mobile,company,designation,ticketType\n" +
                "Vikram Malhotra,vikram@hcl.com,9833344455,HCL Tech,Principal Architect,Delegate Pass\n" +
                "Sneha Kapoor,sneha@wipro.com,9844455566,Wipro VLSI,Verification Lead,VIP Pass\n" +
                "Duplicate Person,dup@test.com,9876543210,Existing Corp,Engineer,Delegate Pass\n"; // Duplicate mobile

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "attendees_batch.csv",
                "text/csv",
                csvContent.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/organizer/attendees/import")
                        .file(file)
                        .header("Authorization", "Bearer " + tokenOrganizer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProcessed").value(3))
                .andExpect(jsonPath("$.totalImported").value(2))
                .andExpect(jsonPath("$.totalSkipped").value(1))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0]").value(containsString("already registered")));
    }

    @Test
    @DisplayName("Should create session, update details, change status to live, and delete cleanly")
    void shouldCreateUpdateAndChangeSessionStatus() throws Exception {
        // 1. Create Session
        CreateSessionRequest createReq = new CreateSessionRequest(
                "Keynote: RISC-V Custom Silicon Architectures",
                "Silicon Track",
                1,
                "11:30 AM – 12:30 PM",
                "Hall C",
                "upcoming",
                "Krishnan Nair",
                "VP Architecture",
                "OpenSilicon",
                "Exploring customized RISC-V instruction extensions.",
                List.of("RISC-V Vector Extensions", "Edge AI acceleration")
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/organizer/sessions")
                        .header("Authorization", "Bearer " + tokenOrganizer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Keynote: RISC-V Custom Silicon Architectures"))
                .andExpect(jsonPath("$.status").value("upcoming"))
                .andReturn();

        String sessionId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // 2. Change Status to Live
        UpdateSessionStatusRequest statusReq = new UpdateSessionStatusRequest("live");
        mockMvc.perform(patch("/api/v1/organizer/sessions/" + sessionId + "/status")
                        .header("Authorization", "Bearer " + tokenOrganizer)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("live"));

        // 3. Delete Session
        mockMvc.perform(delete("/api/v1/organizer/sessions/" + sessionId)
                        .header("Authorization", "Bearer " + tokenOrganizer))
                .andExpect(status().isNoContent());

        assertThat(sessionRepository.existsById(sessionId)).isFalse();
    }

    @Test
    @DisplayName("Security Guard: Non-organizers (Delegates) must be denied access with 403 Forbidden")
    void shouldDenyAccessToNonOrganizers() throws Exception {
        mockMvc.perform(get("/api/v1/organizer/metrics")
                        .header("Authorization", "Bearer " + tokenDelegate))
                .andExpect(status().isForbidden());
    }
}
