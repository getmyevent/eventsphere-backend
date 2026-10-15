package com.eventsphere.modules;

import com.eventsphere.modules.attendee.AttendeeEntity;
import com.eventsphere.modules.attendee.AttendeeRepository;
import com.eventsphere.modules.attendee.model.AttendeeCategory;
import com.eventsphere.modules.attendee.model.UserRole;
import com.eventsphere.modules.connection.ConnectionEntity;
import com.eventsphere.modules.connection.ConnectionRepository;
import com.eventsphere.modules.event.EventEntity;
import com.eventsphere.modules.event.EventRepository;
import com.eventsphere.modules.exhibitor.ExhibitorEntity;
import com.eventsphere.modules.exhibitor.ExhibitorRepository;
import com.eventsphere.modules.exhibitor.LeadEntity;
import com.eventsphere.modules.exhibitor.LeadRepository;
import com.eventsphere.modules.exhibitor.model.LeadStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private AttendeeRepository attendeeRepository;

    @Autowired
    private ExhibitorRepository exhibitorRepository;

    @Autowired
    private LeadRepository leadRepository;

    @Autowired
    private ConnectionRepository connectionRepository;

    private EventEntity event;
    private AttendeeEntity attendeeA;
    private AttendeeEntity attendeeB;
    private ExhibitorEntity exhibitor;

    @BeforeEach
    void setUp() {
        event = eventRepository.save(new EventEntity(
                "TEST_EVENT", "Test Summit", LocalDate.now(), LocalDate.now().plusDays(1),
                "Day 1-2", "Indore", "Convention Center", "UPCOMING"
        ));

        attendeeA = new AttendeeEntity();
        attendeeA.setId("att-user-a");
        attendeeA.setEventId(event.getId());
        attendeeA.setRegistrationId("#REG-001");
        attendeeA.setName("Alice");
        attendeeA.setEmail("alice@test.com");
        attendeeA.setMobile("+91 98765 00001");
        attendeeA.setCategory(AttendeeCategory.DELEGATE);
        attendeeA.setAuthRole(UserRole.ROLE_DELEGATE);
        attendeeRepository.save(attendeeA);

        attendeeB = new AttendeeEntity();
        attendeeB.setId("att-user-b");
        attendeeB.setEventId(event.getId());
        attendeeB.setRegistrationId("#REG-002");
        attendeeB.setName("Bob");
        attendeeB.setEmail("bob@test.com");
        attendeeB.setMobile("+91 98765 00002");
        attendeeB.setCategory(AttendeeCategory.DELEGATE);
        attendeeB.setAuthRole(UserRole.ROLE_DELEGATE);
        attendeeRepository.save(attendeeB);

        exhibitor = new ExhibitorEntity();
        exhibitor.setId("exh-alpha");
        exhibitor.setEventId(event.getId());
        exhibitor.setCompany("Alpha Corp");
        exhibitor.setBooth("Booth 101");
        exhibitor.setCategory("Cloud");
        exhibitorRepository.save(exhibitor);
    }

    @Test
    void testAttendeeNormalizationAndLookup() {
        Optional<AttendeeEntity> found = attendeeRepository.findByEventIdAndNormalizedMobile("TEST_EVENT", "9876500001");
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
        assertEquals(AttendeeCategory.DELEGATE, found.get().getCategory());
        assertEquals(UserRole.ROLE_DELEGATE, found.get().getAuthRole());
    }

    @Test
    void testLeadDefaultStatusAndUniqueConstraint() {
        LeadEntity lead1 = new LeadEntity("lead-1", exhibitor.getId(), attendeeA.getId(), LeadStatus.NEW, "Scan", "Notes 1");
        leadRepository.saveAndFlush(lead1);

        assertEquals(LeadStatus.NEW, lead1.getStatus());

        // Duplicate lead for same exhibitor and attendee must trigger data integrity violation
        LeadEntity duplicateLead = new LeadEntity("lead-2", exhibitor.getId(), attendeeA.getId(), LeadStatus.INTERESTED, "Scan", "Notes 2");
        assertThrows(DataIntegrityViolationException.class, () -> {
            leadRepository.saveAndFlush(duplicateLead);
        });
    }

    @Test
    void testConnectionCanonicalPairPreventsDualReciprocalRows() {
        ConnectionEntity conn1 = new ConnectionEntity("c-1", event.getId(), attendeeA.getId(), attendeeB.getId(), "pending", "direct");
        connectionRepository.saveAndFlush(conn1);

        assertEquals("att-user-a", conn1.getUserLow());
        assertEquals("att-user-b", conn1.getUserHigh());

        // Reciprocal insertion (B -> A) triggers unique constraint on (event_id, user_low, user_high)
        ConnectionEntity connReciprocal = new ConnectionEntity("c-2", event.getId(), attendeeB.getId(), attendeeA.getId(), "pending", "direct");
        assertThrows(DataIntegrityViolationException.class, () -> {
            connectionRepository.saveAndFlush(connReciprocal);
        });
    }
}
