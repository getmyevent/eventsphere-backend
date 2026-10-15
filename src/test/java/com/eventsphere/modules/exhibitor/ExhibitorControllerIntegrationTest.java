package com.eventsphere.modules.exhibitor;

import com.eventsphere.common.security.RateLimiterService;
import com.eventsphere.modules.attendee.AttendeeEntity;
import com.eventsphere.modules.attendee.AttendeeRepository;
import com.eventsphere.modules.attendee.PrivacyConsentEntity;
import com.eventsphere.modules.attendee.model.AttendeeCategory;
import com.eventsphere.modules.attendee.model.PrivacyPreset;
import com.eventsphere.modules.attendee.model.UserRole;
import com.eventsphere.modules.auth.dto.AccessRequest;
import com.eventsphere.modules.event.EventEntity;
import com.eventsphere.modules.event.EventRepository;
import com.eventsphere.modules.exhibitor.dto.*;
import com.eventsphere.modules.exhibitor.model.LeadStatus;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ExhibitorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private ExhibitorRepository exhibitorRepository;

    @Autowired
    private ExhibitorStaffRepository exhibitorStaffRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RateLimiterService rateLimiterService;

    private static final String EVENT_ID = "MPSS26";
    private static final String EXHIBITOR_TRUECHIP_ID = "exh-truechip";
    private static final String EXHIBITOR_SAMSUNG_ID = "exh-samsung";

    private String exhibitorToken;
    private String organizerToken;
    private String delegateToken;

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

        // 2. Seed Exhibitors
        ExhibitorEntity truechip = new ExhibitorEntity();
        truechip.setId(EXHIBITOR_TRUECHIP_ID);
        truechip.setEventId(EVENT_ID);
        truechip.setCompany("Truechip Solutions");
        truechip.setBooth("Hall 1 · A-02");
        truechip.setCategory("Verification IP");
        truechip.setTeamSize(14);
        exhibitorRepository.save(truechip);

        ExhibitorEntity samsung = new ExhibitorEntity();
        samsung.setId(EXHIBITOR_SAMSUNG_ID);
        samsung.setEventId(EVENT_ID);
        samsung.setCompany("Samsung Semiconductor");
        samsung.setBooth("Hall 2 · B-01");
        samsung.setCategory("Foundry");
        samsung.setTeamSize(20);
        exhibitorRepository.save(samsung);

        // 3. Seed Exhibitor Staff (Truechip)
        AttendeeEntity exhStaff = new AttendeeEntity();
        exhStaff.setId("att-truechip-staff");
        exhStaff.setEventId(EVENT_ID);
        exhStaff.setRegistrationId("#REG-EXH-01");
        exhStaff.setName("Truechip Lead");
        exhStaff.setEmail("staff@truechip.net");
        exhStaff.setMobile("+91 98765 00001");
        exhStaff.setNormalizedMobile("9876500001");
        exhStaff.setCategory(AttendeeCategory.EXHIBITOR_STAFF);
        exhStaff.setAuthRole(UserRole.ROLE_EXHIBITOR);
        exhStaff.setCompany("Truechip Solutions");
        exhStaff.setDesignation("Lead Engineer");
        exhStaff.setTicketType("Exhibitor Pass");
        attendeeRepository.save(exhStaff);

        ExhibitorStaffEntity staffMapping = new ExhibitorStaffEntity(EVENT_ID, EXHIBITOR_TRUECHIP_ID, exhStaff.getId(), true);
        exhibitorStaffRepository.save(staffMapping);

        // 4. Seed Organizer Staff
        AttendeeEntity orgStaff = new AttendeeEntity();
        orgStaff.setId("att-org-staff");
        orgStaff.setEventId(EVENT_ID);
        orgStaff.setRegistrationId("#REG-ORG-01");
        orgStaff.setName("Operations Lead");
        orgStaff.setEmail("ops@mpss26.org");
        orgStaff.setMobile("+91 98765 00002");
        orgStaff.setNormalizedMobile("9876500002");
        orgStaff.setCategory(AttendeeCategory.ORGANIZER_STAFF);
        orgStaff.setAuthRole(UserRole.ROLE_ORGANIZER);
        orgStaff.setCompany("MPSEDC Operations");
        orgStaff.setDesignation("Director");
        orgStaff.setTicketType("Organizer Pass");
        attendeeRepository.save(orgStaff);

        // 5. Seed Delegates with Privacy Consents
        // Delegate A: Open privacy
        AttendeeEntity delA = new AttendeeEntity();
        delA.setId("att-del-a");
        delA.setEventId(EVENT_ID);
        delA.setRegistrationId("#REG-DEL-A");
        delA.setName("Aarav Mehta");
        delA.setEmail("aarav@ti.com");
        delA.setMobile("+91 98111 11111");
        delA.setNormalizedMobile("9811111111");
        delA.setCategory(AttendeeCategory.DELEGATE);
        delA.setAuthRole(UserRole.ROLE_DELEGATE);
        delA.setCompany("Texas Instruments");
        delA.setDesignation("Staff Architect");
        delA.setTicketType("Delegate");
        delA.setLinkedinUrl("https://linkedin.com/in/aaravmehta");
        PrivacyConsentEntity privacyA = new PrivacyConsentEntity("att-del-a", PrivacyPreset.OPEN, true, true, true, false);
        privacyA.setAttendee(delA);
        delA.setPrivacyConsent(privacyA);
        attendeeRepository.save(delA);

        // Delegate B: Private (masked contacts)
        AttendeeEntity delB = new AttendeeEntity();
        delB.setId("att-del-b");
        delB.setEventId(EVENT_ID);
        delB.setRegistrationId("#REG-DEL-B");
        delB.setName("Priya Sharma");
        delB.setEmail("priya@qualcomm.com");
        delB.setMobile("+91 98222 22222");
        delB.setNormalizedMobile("9822222222");
        delB.setCategory(AttendeeCategory.DELEGATE);
        delB.setAuthRole(UserRole.ROLE_DELEGATE);
        delB.setCompany("Qualcomm");
        delB.setDesignation("Senior Director");
        delB.setTicketType("VIP");
        delB.setLinkedinUrl("https://linkedin.com/in/priyasharma");
        PrivacyConsentEntity privacyB = new PrivacyConsentEntity("att-del-b", PrivacyPreset.CONNECTIONS_ONLY, false, false, false, false);
        privacyB.setAttendee(delB);
        delB.setPrivacyConsent(privacyB);
        attendeeRepository.save(delB);

        // 6. Seed Existing Products for Truechip
        ProductEntity prod1 = new ProductEntity();
        prod1.setId("prod-pcie-vip");
        prod1.setExhibitorId(EXHIBITOR_TRUECHIP_ID);
        prod1.setName("PCIe Gen 6 / 7 Verification IP");
        prod1.setTagline("Industry-leading PAM4 PCIe VIP with complete test suite");
        prod1.setCategory("Verification IP & EDA");
        prod1.setOverview("Silicon-proven comprehensive verification environment.");
        prod1.setFeaturesJson("[\"PAM4 Signal Integrity\",\"PIPE 6.0 Support\"]");
        prod1.setBenefitsJson("[\"First-pass silicon success\"]");
        prod1.setUseCasesJson("[\"Datacenter AI ASICs\"]");
        prod1.setViews(120);
        prod1.setEnquiries(15);
        productRepository.save(prod1);

        // 7. Seed Existing Lead for Truechip
        LeadEntity lead1 = new LeadEntity(
                "lead-aarav",
                EXHIBITOR_TRUECHIP_ID,
                "att-del-a",
                LeadStatus.INTERESTED,
                "Directory Lead Capture",
                "Interested in PCIe Gen 7 licensing for next tapeout."
        );
        lead1.setAttendee(delA);
        lead1.setInterestsJson("[\"PCIe Gen 7\",\"UCIe Chiplet\"]");
        leadRepository.save(lead1);

        // 8. Seed Product and Lead for Samsung (Tenant Isolation test)
        ProductEntity samsungProd = new ProductEntity();
        samsungProd.setId("prod-samsung-gateallaround");
        samsungProd.setExhibitorId(EXHIBITOR_SAMSUNG_ID);
        samsungProd.setName("2nm GAA Process");
        samsungProd.setTagline("Next-gen Gate-All-Around foundry technology");
        samsungProd.setCategory("Foundry");
        samsungProd.setOverview("Cutting-edge 2nm foundry nodes.");
        productRepository.save(samsungProd);

        LeadEntity samsungLead = new LeadEntity(
                "lead-samsung-exclusive",
                EXHIBITOR_SAMSUNG_ID,
                "att-del-b",
                LeadStatus.NEW,
                "Samsung Private Booth",
                "Strictly confidential foundry discussion"
        );
        samsungLead.setAttendee(delB);
        leadRepository.save(samsungLead);

        // Obtain JWT tokens
        exhibitorToken = obtainToken("9876500001");
        organizerToken = obtainToken("9876500002");
        delegateToken = obtainToken("9811111111");
    }

    @Test
    @DisplayName("GET /api/v1/exhibitor/stats returns accurate booth metrics")
    void testStatsEndpointReturnsAccurateMetrics() throws Exception {
        mockMvc.perform(get("/api/v1/exhibitor/stats")
                        .header("Authorization", "Bearer " + exhibitorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(EXHIBITOR_TRUECHIP_ID)))
                .andExpect(jsonPath("$.company", is("Truechip Solutions")))
                .andExpect(jsonPath("$.booth", is("Hall 1 · A-02")))
                .andExpect(jsonPath("$.totalLeads", is(1)))
                .andExpect(jsonPath("$.interestedLeads", is(1)))
                .andExpect(jsonPath("$.newLeads", is(0)))
                .andExpect(jsonPath("$.followUpLeads", is(0)))
                .andExpect(jsonPath("$.totalProducts", is(1)))
                .andExpect(jsonPath("$.totalViews", is(120)))
                .andExpect(jsonPath("$.totalEnquiries", is(15)));
    }

    @Test
    @DisplayName("GET /api/v1/exhibitor/attendees/search respects query and privacy masking")
    void testCandidateSearchAndPrivacyMasking() throws Exception {
        // Search for Priya Sharma (who has privacy contacts hidden)
        mockMvc.perform(get("/api/v1/exhibitor/attendees/search")
                        .param("query", "Priya")
                        .header("Authorization", "Bearer " + exhibitorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Priya Sharma")))
                .andExpect(jsonPath("$[0].company", is("Qualcomm")))
                // Contact info masked due to privacy preset
                .andExpect(jsonPath("$[0].email", is("contact@eventsphere.internal")))
                .andExpect(jsonPath("$[0].mobile", is("+91 98765 00000")))
                .andExpect(jsonPath("$[0].linkedin", is("")))
                .andExpect(jsonPath("$[0].isLead", is(false)));

        // Search for Aarav Mehta (who has open privacy and is already a captured lead)
        mockMvc.perform(get("/api/v1/exhibitor/attendees/search")
                        .param("query", "Aarav")
                        .header("Authorization", "Bearer " + exhibitorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Aarav Mehta")))
                .andExpect(jsonPath("$[0].email", is("aarav@ti.com")))
                .andExpect(jsonPath("$[0].mobile", is("+91 98111 11111")))
                .andExpect(jsonPath("$[0].linkedin", is("https://linkedin.com/in/aaravmehta")))
                .andExpect(jsonPath("$[0].isLead", is(true)))
                .andExpect(jsonPath("$[0].leadStatus", is("Interested")));
    }

    @Test
    @DisplayName("Lead capture defaults status to 'New' when not provided")
    void testLeadCaptureWithDefaultStatusNew() throws Exception {
        CaptureLeadRequest request = new CaptureLeadRequest();
        request.setAttendeeId("att-del-b");
        // Status is left null to test the architectural invariant rule
        request.setNotes("First booth contact");

        mockMvc.perform(post("/api/v1/exhibitor/leads")
                        .header("Authorization", "Bearer " + exhibitorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.personId", is("att-del-b")))
                .andExpect(jsonPath("$.name", is("Priya Sharma")))
                .andExpect(jsonPath("$.status", is("New"))) // Invariant verified!
                .andExpect(jsonPath("$.notes", is("First booth contact")))
                .andExpect(jsonPath("$.boothId", is("Hall 1 · A-02")));

        // Verify count updated in database
        assertThat(leadRepository.countByExhibitorId(EXHIBITOR_TRUECHIP_ID)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lead capture idempotent upsert prevents duplicate key violation")
    void testLeadCaptureUpsertIdempotency() throws Exception {
        // Aarav is already captured as lead-aarav
        CaptureLeadRequest request = new CaptureLeadRequest();
        request.setAttendeeId("att-del-a");
        request.setStatus(LeadStatus.FOLLOW_UP);
        request.setNotes("Updated notes after 2nd booth visit");

        mockMvc.perform(post("/api/v1/exhibitor/leads")
                        .header("Authorization", "Bearer " + exhibitorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.personId", is("att-del-a")))
                .andExpect(jsonPath("$.status", is("Follow-up")))
                .andExpect(jsonPath("$.notes", is("Updated notes after 2nd booth visit")));

        // Verify total leads count for Truechip is STILL 1 (not duplicated)
        assertThat(leadRepository.countByExhibitorId(EXHIBITOR_TRUECHIP_ID)).isEqualTo(1);
    }

    @Test
    @DisplayName("PATCH /api/v1/exhibitor/leads/{leadId} updates status and notes")
    void testUpdateLeadStatusAndNotes() throws Exception {
        UpdateLeadStatusRequest request = new UpdateLeadStatusRequest(
                LeadStatus.FOLLOW_UP,
                "Executive meeting scheduled for tomorrow 11 AM"
        );

        mockMvc.perform(patch("/api/v1/exhibitor/leads/lead-aarav")
                        .header("Authorization", "Bearer " + exhibitorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("lead-aarav")))
                .andExpect(jsonPath("$.status", is("Follow-up")))
                .andExpect(jsonPath("$.notes", is("Executive meeting scheduled for tomorrow 11 AM")));
    }

    @Test
    @DisplayName("GET /api/v1/exhibitor/leads filters by status")
    void testGetLeadsFilterByStatus() throws Exception {
        // Query by status = Interested (lead-aarav is Interested)
        mockMvc.perform(get("/api/v1/exhibitor/leads")
                        .param("status", "Interested")
                        .header("Authorization", "Bearer " + exhibitorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("lead-aarav")));

        // Query by status = New (no leads are New yet for Truechip)
        mockMvc.perform(get("/api/v1/exhibitor/leads")
                        .param("status", "New")
                        .header("Authorization", "Bearer " + exhibitorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v1/exhibitor/leads/export returns RFC 4180 compliant CSV")
    void testExportLeadsCsv() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/exhibitor/leads/export")
                        .header("Authorization", "Bearer " + exhibitorToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", containsString("attachment; filename=\"booth-leads-exh-truechip.csv\"")))
                .andReturn();

        String csvContent = result.getResponse().getContentAsString();
        assertThat(csvContent).contains("\"Name\",\"Company\",\"Designation\",\"Email\",\"Mobile\",\"Status\",\"Interests\",\"Captured At\",\"Notes\"");
        assertThat(csvContent).contains("\"Aarav Mehta\",\"Texas Instruments\",\"Staff Architect\",\"aarav@ti.com\"");
        assertThat(csvContent).contains("\"Interested\"");
    }

    @Test
    @DisplayName("Product CRUD full lifecycle (Create, Read, Update, Delete)")
    void testProductCrudLifecycle() throws Exception {
        // 1. Create Product
        CreateProductRequest createReq = new CreateProductRequest(
                "UCIe 1.2 D2D Controller VIP",
                "High-speed die-to-die chiplet verification environment",
                "Verification IP & EDA",
                "Comprehensive chiplet interconnect solution supporting streaming and PCIe/CXL protocols.",
                List.of("Multi-die protocol checker", "Sideband signaling verification"),
                List.of("Accelerates chiplet tapeout"),
                List.of("AI Accelerator Chiplets", "Server Heterogeneous SoCs"),
                "https://www.truechip.net/ucie",
                "https://www.truechip.net/docs/ucie.pdf",
                "/images/products/ucie.png"
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/exhibitor/products")
                        .header("Authorization", "Bearer " + exhibitorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createReq)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name", is("UCIe 1.2 D2D Controller VIP")))
                .andExpect(jsonPath("$.company", is("Truechip Solutions")))
                .andExpect(jsonPath("$.booth", is("Hall 1 · A-02")))
                .andExpect(jsonPath("$.features", hasSize(2)))
                .andReturn();

        String newProductId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

        // 2. Read Product
        mockMvc.perform(get("/api/v1/exhibitor/products/" + newProductId)
                        .header("Authorization", "Bearer " + exhibitorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(newProductId)))
                .andExpect(jsonPath("$.tagline", is("High-speed die-to-die chiplet verification environment")));

        // 3. Update Product
        UpdateProductRequest updateReq = new UpdateProductRequest();
        updateReq.setName("UCIe 2.0 Next-Gen VIP");
        updateReq.setTagline("Updated 3D packaging support");
        updateReq.setCategory("Verification IP & EDA");
        updateReq.setOverview("Updated overview description.");

        mockMvc.perform(put("/api/v1/exhibitor/products/" + newProductId)
                        .header("Authorization", "Bearer " + exhibitorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("UCIe 2.0 Next-Gen VIP")))
                .andExpect(jsonPath("$.tagline", is("Updated 3D packaging support")));

        // 4. Delete Product
        mockMvc.perform(delete("/api/v1/exhibitor/products/" + newProductId)
                        .header("Authorization", "Bearer " + exhibitorToken))
                .andExpect(status().isNoContent());

        // 5. Subsequent Read returns 404
        mockMvc.perform(get("/api/v1/exhibitor/products/" + newProductId)
                        .header("Authorization", "Bearer " + exhibitorToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Tenant Isolation: Exhibitor A is strictly blocked from accessing Exhibitor B's data")
    void testTenantIsolationStrictlyEnforced() throws Exception {
        // Truechip attempts to view Samsung's stats by passing exhibitorId
        mockMvc.perform(get("/api/v1/exhibitor/stats")
                        .param("exhibitorId", EXHIBITOR_SAMSUNG_ID)
                        .header("Authorization", "Bearer " + exhibitorToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)))
                .andExpect(jsonPath("$.message", containsString("Cross-tenant access is prohibited")));

        // Truechip attempts to read Samsung's product
        mockMvc.perform(get("/api/v1/exhibitor/products/prod-samsung-gateallaround")
                        .header("Authorization", "Bearer " + exhibitorToken))
                .andExpect(status().isNotFound());

        // Truechip attempts to update Samsung's lead
        UpdateLeadStatusRequest req = new UpdateLeadStatusRequest(LeadStatus.FOLLOW_UP, "Hacked notes");
        mockMvc.perform(patch("/api/v1/exhibitor/leads/lead-samsung-exclusive")
                        .header("Authorization", "Bearer " + exhibitorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Organizer can view Exhibitor data using explicit exhibitorId")
    void testOrganizerCanAccessExhibitorDataWithScope() throws Exception {
        mockMvc.perform(get("/api/v1/exhibitor/stats")
                        .param("exhibitorId", EXHIBITOR_TRUECHIP_ID)
                        .header("Authorization", "Bearer " + organizerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company", is("Truechip Solutions")))
                .andExpect(jsonPath("$.booth", is("Hall 1 · A-02")));
    }

    @Test
    @DisplayName("Delegate role is rejected with 403 Forbidden on exhibitor endpoints")
    void testDelegateCannotAccessExhibitorEndpoints() throws Exception {
        mockMvc.perform(get("/api/v1/exhibitor/stats")
                        .header("Authorization", "Bearer " + delegateToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status", is(403)));
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
