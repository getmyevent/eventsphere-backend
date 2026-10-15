package com.eventsphere.modules.session;

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
import com.eventsphere.modules.session.dto.CreateQuestionRequest;
import com.eventsphere.modules.session.dto.VotePollRequest;
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
class SessionControllerIntegrationTest {

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
    private SessionRepository sessionRepository;

    @Autowired
    private SessionBookmarkRepository sessionBookmarkRepository;

    @Autowired
    private SessionQuestionRepository sessionQuestionRepository;

    @Autowired
    private QuestionUpvoteRepository questionUpvoteRepository;

    @Autowired
    private PollRepository pollRepository;

    @Autowired
    private PollOptionRepository pollOptionRepository;

    @Autowired
    private PollVoteRepository pollVoteRepository;

    @Autowired
    private RateLimiterService rateLimiterService;

    private String tokenHari;
    private String tokenSantosh;

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

        // 2. Setup Hari
        if (!attendeeRepository.existsById("att-hari")) {
            AttendeeEntity hari = new AttendeeEntity();
            hari.setId("att-hari");
            hari.setEventId("MPSS26");
            hari.setRegistrationId("#REG-HARI");
            hari.setName("Hari Gupta");
            hari.setEmail("hari@eventsphere.com");
            hari.setMobile("+91 98765 43210");
            hari.setNormalizedMobile("9876543210");
            hari.setCompany("EventSphere");
            hari.setDesignation("CEO");
            hari.setCategory(AttendeeCategory.DELEGATE);
            hari.setAuthRole(UserRole.ROLE_DELEGATE);
            hari.setTicketType("VIP Pass");

            PrivacyConsentEntity p1 = new PrivacyConsentEntity("att-hari", PrivacyPreset.CONNECTIONS_ONLY, true, true, true, false);
            p1.setAttendee(hari);
            hari.setPrivacyConsent(p1);
            attendeeRepository.save(hari);
        }

        // 3. Setup Santosh
        if (!attendeeRepository.existsById("att-santosh")) {
            AttendeeEntity santosh = new AttendeeEntity();
            santosh.setId("att-santosh");
            santosh.setEventId("MPSS26");
            santosh.setRegistrationId("#REG-SANTOSH");
            santosh.setName("Santosh Kumar");
            santosh.setEmail("santosh@iiti.ac.in");
            santosh.setMobile("+91 97137 22841");
            santosh.setNormalizedMobile("9713722841");
            santosh.setCompany("IIT Indore");
            santosh.setDesignation("Professor");
            santosh.setCategory(AttendeeCategory.DELEGATE);
            santosh.setAuthRole(UserRole.ROLE_DELEGATE);
            santosh.setTicketType("Delegate Pass");

            PrivacyConsentEntity p2 = new PrivacyConsentEntity("att-santosh", PrivacyPreset.CONNECTIONS_ONLY, true, true, true, false);
            p2.setAttendee(santosh);
            santosh.setPrivacyConsent(p2);
            attendeeRepository.save(santosh);
        }

        // 4. Setup Sessions
        SessionEntity s1 = new SessionEntity();
        s1.setId("sess-test-01");
        s1.setEventId("MPSS26");
        s1.setDayNumber(1);
        s1.setTimeRange("10:00 AM – 11:00 AM");
        s1.setTitle("Keynote on Global Semiconductor Trends");
        s1.setTrack("Keynote");
        s1.setRoom("Main Auditorium");
        s1.setSessionType("session");
        s1.setSpeakerName("Jaswinder Ahuja");
        s1.setSpeakerDesignation("Advisor");
        s1.setSpeakerCompany("India Semiconductor Mission");
        s1.setDescription("Overview of semiconductor market and packaging trends.");
        s1.setTakeawaysJson("[\"Chiplet packaging trends\", \"India ecosystem growth\"]");
        s1.setStatus("live");
        sessionRepository.save(s1);

        SessionEntity s2 = new SessionEntity();
        s2.setId("sess-test-02");
        s2.setEventId("MPSS26");
        s2.setDayNumber(2);
        s2.setTimeRange("02:00 PM – 03:00 PM");
        s2.setTitle("Panel: VLSI Workforce & Talent Incubation");
        s2.setTrack("Workforce");
        s2.setRoom("Hall B");
        s2.setSessionType("session");
        s2.setSpeakerName("Dr. Amit Agarwal");
        s2.setSpeakerDesignation("Director");
        s2.setSpeakerCompany("IIITM Gwalior");
        s2.setDescription("Discussion on curriculum modernization and EDA tool access.");
        s2.setTakeawaysJson("[\"Hands-on silicon design training\"]");
        s2.setStatus("upcoming");
        sessionRepository.save(s2);

        // 5. Setup Polls
        PollEntity poll1 = new PollEntity();
        poll1.setId("poll-test-01");
        poll1.setSessionId("sess-test-01");
        poll1.setQuestion("Which domain holds highest potential for MP?");
        poll1.setStatus("open");
        poll1.setTotalVotes(0);
        pollRepository.save(poll1);

        PollOptionEntity opt1 = new PollOptionEntity();
        opt1.setId("opt-test-01-a");
        opt1.setPoll(poll1);
        opt1.setText("Fabless Chip Design");
        opt1.setVotes(0);
        opt1.setSortOrder(1);
        pollOptionRepository.save(opt1);

        PollOptionEntity opt2 = new PollOptionEntity();
        opt2.setId("opt-test-01-b");
        opt2.setPoll(poll1);
        opt2.setText("Advanced ATMP Packaging");
        opt2.setVotes(0);
        opt2.setSortOrder(2);
        pollOptionRepository.save(opt2);

        PollEntity poll2 = new PollEntity();
        poll2.setId("poll-test-02");
        poll2.setSessionId("sess-test-02");
        poll2.setQuestion("Is campus EDA training adequate?");
        poll2.setStatus("open");
        poll2.setTotalVotes(0);
        pollRepository.save(poll2);

        PollOptionEntity opt2_1 = new PollOptionEntity();
        opt2_1.setId("opt-test-02-a");
        opt2_1.setPoll(poll2);
        opt2_1.setText("Needs Major Upgrade");
        opt2_1.setVotes(0);
        opt2_1.setSortOrder(1);
        pollOptionRepository.save(opt2_1);

        tokenHari = loginAndGetToken("9876543210");
        tokenSantosh = loginAndGetToken("9713722841");
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
    @DisplayName("Should retrieve sessions list and filter by day number")
    void shouldGetAllSessionsAndFilterByDay() throws Exception {
        // All sessions
        mockMvc.perform(get("/api/v1/sessions")
                        .header("Authorization", "Bearer " + tokenHari))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[?(@.id == 'sess-test-01')].day", contains(1)))
                .andExpect(jsonPath("$[?(@.id == 'sess-test-02')].day", contains(2)));

        // Filter Day 1
        mockMvc.perform(get("/api/v1/sessions?day=1")
                        .header("Authorization", "Bearer " + tokenHari))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 'sess-test-01')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.id == 'sess-test-02')]", hasSize(0)));

        // Filter Day 2
        mockMvc.perform(get("/api/v1/sessions?day=2")
                        .header("Authorization", "Bearer " + tokenHari))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 'sess-test-02')]", hasSize(1)))
                .andExpect(jsonPath("$[?(@.id == 'sess-test-01')]", hasSize(0)));
    }

    @Test
    @DisplayName("Should search sessions across title, track, and speaker")
    void shouldFilterSessionsBySearchQuery() throws Exception {
        mockMvc.perform(get("/api/v1/sessions?search=Workforce")
                        .header("Authorization", "Bearer " + tokenHari))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == 'sess-test-02')]", hasSize(1)));
    }

    @Test
    @DisplayName("Should toggle session bookmark for authenticated attendee")
    void shouldToggleSessionBookmarkSuccessfully() throws Exception {
        // 1. Bookmark session
        mockMvc.perform(post("/api/v1/sessions/sess-test-01/bookmark")
                        .header("Authorization", "Bearer " + tokenHari))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("sess-test-01"))
                .andExpect(jsonPath("$.isBookmarked").value(true));

        // 2. Verify session details shows isBookmarked = true for Hari
        mockMvc.perform(get("/api/v1/sessions/sess-test-01")
                        .header("Authorization", "Bearer " + tokenHari))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBookmarked").value(true));

        // 3. Verify session details shows isBookmarked = false for Santosh (per-user scope)
        mockMvc.perform(get("/api/v1/sessions/sess-test-01")
                        .header("Authorization", "Bearer " + tokenSantosh))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBookmarked").value(false));

        // 4. Filter bookmarked only
        mockMvc.perform(get("/api/v1/sessions?bookmarkedOnly=true")
                        .header("Authorization", "Bearer " + tokenHari))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value("sess-test-01"));

        // 5. Unbookmark session
        mockMvc.perform(post("/api/v1/sessions/sess-test-01/bookmark")
                        .header("Authorization", "Bearer " + tokenHari))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBookmarked").value(false));

        // 6. Verify now unbookmarked
        mockMvc.perform(get("/api/v1/sessions/sess-test-01")
                        .header("Authorization", "Bearer " + tokenHari))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isBookmarked").value(false));
    }

    @Test
    @DisplayName("Invariant: Newly submitted question defaults strictly to 0 upvotes")
    void shouldSubmitSessionQuestionWithInitialUpvotesZero() throws Exception {
        CreateQuestionRequest request = new CreateQuestionRequest(
                "What is the targeted timeline for the proposed MP semiconductor pilot plant?",
                false
        );

        MvcResult result = mockMvc.perform(post("/api/v1/sessions/sess-test-01/questions")
                        .header("Authorization", "Bearer " + tokenHari)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("sess-test-01"))
                .andExpect(jsonPath("$.authorName").value("Hari Gupta"))
                .andExpect(jsonPath("$.authorId").value("att-hari"))
                .andExpect(jsonPath("$.isAnonymous").value(false))
                .andExpect(jsonPath("$.upvotes").value(0)) // Invariant: starts at 0!
                .andExpect(jsonPath("$.hasUpvoted").value(false))
                .andReturn();

        String questionId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        // Verify session's questionsCount incremented
        mockMvc.perform(get("/api/v1/sessions/sess-test-01")
                        .header("Authorization", "Bearer " + tokenHari))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionsCount").value(1));
    }

    @Test
    @DisplayName("Should submit anonymous question and mask author identity")
    void shouldSubmitAnonymousQuestionAndMaskAuthorIdentity() throws Exception {
        CreateQuestionRequest request = new CreateQuestionRequest(
                "Are there additional subsidies for early-stage fabless startups?",
                true
        );

        mockMvc.perform(post("/api/v1/sessions/sess-test-01/questions")
                        .header("Authorization", "Bearer " + tokenHari)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isAnonymous").value(true))
                .andExpect(jsonPath("$.authorName").value("Anonymous Attendee"))
                .andExpect(jsonPath("$.authorId").value("usr-anon"));
    }

    @Test
    @DisplayName("Should toggle question upvote atomically")
    void shouldToggleQuestionUpvoteAtomically() throws Exception {
        // 1. Submit question
        CreateQuestionRequest request = new CreateQuestionRequest("Can academic research designs be taped out via ISM?", false);
        MvcResult submitResult = mockMvc.perform(post("/api/v1/sessions/sess-test-01/questions")
                        .header("Authorization", "Bearer " + tokenHari)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        String questionId = objectMapper.readTree(submitResult.getResponse().getContentAsString()).get("id").asText();

        // 2. Santosh upvotes question (0 -> 1)
        mockMvc.perform(post("/api/v1/sessions/questions/" + questionId + "/upvote")
                        .header("Authorization", "Bearer " + tokenSantosh))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.questionId").value(questionId))
                .andExpect(jsonPath("$.upvotes").value(1))
                .andExpect(jsonPath("$.hasUpvoted").value(true));

        // 3. Hari also upvotes question (1 -> 2)
        mockMvc.perform(post("/api/v1/sessions/questions/" + questionId + "/upvote")
                        .header("Authorization", "Bearer " + tokenHari))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotes").value(2))
                .andExpect(jsonPath("$.hasUpvoted").value(true));

        // 4. Santosh removes his upvote (2 -> 1)
        mockMvc.perform(post("/api/v1/sessions/questions/" + questionId + "/upvote")
                        .header("Authorization", "Bearer " + tokenSantosh))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvotes").value(1))
                .andExpect(jsonPath("$.hasUpvoted").value(false));

        // 5. In database, exactly 1 row exists in question_upvotes
        assertThat(questionUpvoteRepository.existsByIdQuestionIdAndIdAttendeeId(questionId, "att-hari")).isTrue();
        assertThat(questionUpvoteRepository.existsByIdQuestionIdAndIdAttendeeId(questionId, "att-santosh")).isFalse();
    }

    @Test
    @DisplayName("Invariant: Poll voting enforces that option_id strictly belongs to the specified poll")
    void shouldEnforcePollOptionBelongsToSamePoll() throws Exception {
        // opt-test-02-a belongs to poll-test-02, NOT poll-test-01
        VotePollRequest request = new VotePollRequest("opt-test-02-a");

        mockMvc.perform(post("/api/v1/polls/poll-test-01/vote")
                        .header("Authorization", "Bearer " + tokenHari)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("does not belong to poll")));
    }

    @Test
    @DisplayName("Should vote in poll and increment tallies correctly")
    void shouldVoteInPollAndIncrementCounts() throws Exception {
        VotePollRequest request = new VotePollRequest("opt-test-01-a");

        mockMvc.perform(post("/api/v1/polls/poll-test-01/vote")
                        .header("Authorization", "Bearer " + tokenHari)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("poll-test-01"))
                .andExpect(jsonPath("$.totalVotes").value(1))
                .andExpect(jsonPath("$.hasVoted").value(true))
                .andExpect(jsonPath("$.votedOptionId").value("opt-test-01-a"))
                .andExpect(jsonPath("$.options[?(@.id == 'opt-test-01-a')].votes", contains(1)));
    }

    @Test
    @DisplayName("Should prevent duplicate vote in same poll by same attendee")
    void shouldPreventDuplicateVoteInSamePoll() throws Exception {
        VotePollRequest request = new VotePollRequest("opt-test-01-a");

        // First vote succeeds
        mockMvc.perform(post("/api/v1/polls/poll-test-01/vote")
                        .header("Authorization", "Bearer " + tokenHari)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Second vote fails with 400 Bad Request
        mockMvc.perform(post("/api/v1/polls/poll-test-01/vote")
                        .header("Authorization", "Bearer " + tokenHari)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("already voted in this poll")));
    }

    @Test
    @DisplayName("Should allow resetting poll vote")
    void shouldResetPollVote() throws Exception {
        VotePollRequest request = new VotePollRequest("opt-test-01-a");

        // Vote
        mockMvc.perform(post("/api/v1/polls/poll-test-01/vote")
                        .header("Authorization", "Bearer " + tokenHari)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(1));

        // Reset vote
        mockMvc.perform(delete("/api/v1/polls/poll-test-01/vote")
                        .header("Authorization", "Bearer " + tokenHari))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVotes").value(0))
                .andExpect(jsonPath("$.hasVoted").value(false))
                .andExpect(jsonPath("$.votedOptionId").doesNotExist());
    }
}
