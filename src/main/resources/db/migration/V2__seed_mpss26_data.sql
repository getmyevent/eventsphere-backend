-- =============================================================================
-- Flyway Migration: V2__seed_mpss26_data.sql
-- Seed Data: Madhya Pradesh Semiconductor Summit 2026 (MPSS26)
-- =============================================================================

-- 1. Seed Event
INSERT INTO events (id, name, start_date, end_date, dates_display, city, venue, status)
VALUES (
    'MPSS26',
    'Madhya Pradesh Semiconductor Summit 2026',
    '2026-10-23',
    '2026-10-24',
    '23–24 Oct 2026',
    'Indore',
    'Brilliant Convention Centre, Indore',
    'UPCOMING'
);

-- 2. Seed Core Attendees (Delegate, Exhibitor Staff, Organizer Staff, Keynote Speakers)
INSERT INTO attendees (id, event_id, registration_id, name, email, mobile, normalized_mobile, designation, company, category, auth_role, ticket_type, bio, avatar_url, checked_in)
VALUES
(
    'att-hari-gupta',
    'MPSS26',
    '#REG-MPSS26-5023',
    'Hari Gupta',
    'hari@eventsphere.com',
    '+91 98765 43210',
    '9876543210',
    'CEO & Founder',
    'EventSphere',
    'delegate',
    'ROLE_DELEGATE',
    'Full Access Delegate Pass',
    'Founder & CEO building next-gen event engagement platforms.',
    '/images/avatars/hari-gupta.png',
    TRUE
),
(
    'att-santosh-kumar',
    'MPSS26',
    '#REG-MPSS26-0523',
    'Santosh Kumar',
    'skvishvakarma@iiti.ac.in',
    '+91 97137 22841',
    '9713722841',
    'Chief Scientific Advisor',
    'AIGGPA Bhopal, Govt. of Madhya Pradesh',
    'delegate',
    'ROLE_DELEGATE',
    'VIP Delegate Pass',
    'Scientific Advisor focused on state technology roadmaps and semiconductor policies.',
    '/images/avatars/santosh-kumar.png',
    TRUE
),
(
    'att-truechip-staff-01',
    'MPSS26',
    '#REG-MPSS26-3211',
    'Truechip Solutions Staff',
    'booth@truechip.net',
    '+91 98765 43211',
    '9876543211',
    'Senior Applications Engineer',
    'Truechip Solutions',
    'exhibitor_staff',
    'ROLE_EXHIBITOR',
    'Exhibitor Staff Pass',
    'Verification IP Lead at Truechip Solutions Booth A-02.',
    '/images/exhibitors/truechip.png',
    TRUE
),
(
    'att-organizer-staff-01',
    'MPSS26',
    '#REG-MPSS26-3212',
    'Summit Operations Lead',
    'ops@mpss26.org',
    '+91 90876 543212',
    '90876543212',
    'Operations Director',
    'MPSEDC & EventSphere Operations',
    'organizer_staff',
    'ROLE_ORGANIZER',
    'Organizer All-Access Pass',
    'Organizing committee operations and stage moderation console manager.',
    '/images/avatars/hari-gupta.png',
    TRUE
),
(
    'speaker-jaswinder-ahuja',
    'MPSS26',
    '#SPK-MPSS26-01',
    'Jaswinder Ahuja',
    'jaswinder@ism.gov.in',
    'Not publicly listed',
    '0000000001',
    'Advisor',
    'India Semiconductor Mission',
    'speaker',
    'ROLE_DELEGATE',
    'Speaker Pass',
    'Industry veteran and senior advisor driving India Semiconductor Mission initiatives.',
    '/images/speakers/jaswinder-ahuja.png',
    TRUE
),
(
    'speaker-dr-amit-agarwal',
    'MPSS26',
    '#SPK-MPSS26-02',
    'Dr. Amit Agarwal',
    'director@iiitm.ac.in',
    'Not publicly listed',
    '0000000002',
    'Director',
    'IIITM Gwalior',
    'speaker',
    'ROLE_DELEGATE',
    'Speaker Pass',
    'Leading academic researcher in VLSI architectures and microelectronics workforce training.',
    '/images/speakers/dr-amit-agarwal.png',
    TRUE
),
(
    'speaker-sanjay-gupta',
    'MPSS26',
    '#SPK-MPSS26-03',
    'Sanjay Gupta',
    'president@vlsisociety.org',
    'Not publicly listed',
    '0000000003',
    'President',
    'VLSI Society of India',
    'speaker',
    'ROLE_DELEGATE',
    'Speaker Pass',
    'President of VSI fostering national industry-academia collaboration in silicon design.',
    '/images/speakers/sanjay-gupta.png',
    TRUE
);

-- 3. Seed Privacy Consents
INSERT INTO privacy_consents (attendee_id, preset, share_email, share_phone, share_linkedin)
VALUES
('att-hari-gupta', 'Connections Only', TRUE, TRUE, TRUE),
('att-santosh-kumar', 'Connections Only', TRUE, TRUE, TRUE),
('att-truechip-staff-01', 'Open', TRUE, TRUE, TRUE),
('att-organizer-staff-01', 'Open', TRUE, TRUE, TRUE),
('speaker-jaswinder-ahuja', 'Connections Only', FALSE, FALSE, TRUE),
('speaker-dr-amit-agarwal', 'Connections Only', FALSE, FALSE, TRUE),
('speaker-sanjay-gupta', 'Connections Only', FALSE, FALSE, TRUE);

-- 4. Seed Exhibitors
INSERT INTO exhibitors (id, event_id, company, booth, category, logo_url, team_size, status)
VALUES (
    'exh-truechip',
    'MPSS26',
    'Truechip Solutions',
    'Hall 1 · A-02',
    'Verification IP & EDA',
    '/images/exhibitors/truechip.png',
    14,
    'Active'
);

-- 5. Seed Exhibitor Staff Mapping
INSERT INTO exhibitor_staff (event_id, exhibitor_id, attendee_id, is_booth_admin)
VALUES ('MPSS26', 'exh-truechip', 'att-truechip-staff-01', TRUE);

-- 6. Seed Exhibitor Products
INSERT INTO products (id, exhibitor_id, name, tagline, category, image_url, views, enquiries, interested_visitors_count, overview, features_json, benefits_json, use_cases_json, website_url)
VALUES
(
    'prod-truechip-1',
    'exh-truechip',
    'PCIe Gen 6 / 7 VIP',
    'Comprehensive Verification IP for Next-Gen PCI Express architectures',
    'Verification IP & EDA',
    '/images/exhibitors/truechip.png',
    420,
    38,
    29,
    'Silicon-proven Verification IP supporting FLIT mode, PAM4 modulation, and low-latency protocol stacks.',
    '["PAM4 & NRZ signaling", "FLIT mode verification", "Built-in error injection test suite"]',
    '["Drastically cuts verification cycle", "First-pass silicon success", "Supports all major EDA simulators"]',
    '["Data Center Accelerators", "AI/ML Workload Processors", "Enterprise Storage Controllers"]',
    'https://www.truechip.net/pcie-vip'
),
(
    'prod-truechip-2',
    'exh-truechip',
    'UCIe Chiplet Interconnect VIP',
    'Universal Chiplet Interconnect Express native verification suite',
    'Chiplet & Packaging',
    '/images/exhibitors/truechip.png',
    310,
    24,
    18,
    'Comprehensive solution for verifying die-to-die interconnects across advanced 2.5D and 3D heterogeneous packaging.',
    '["Die-to-Die Adapter testing", "Physical layer sideband modeling", "Multi-vendor chiplet interop"]',
    '["Ensures heterogenous package compliance", "Reduces physical prototyping costs"]',
    '["High Performance Computing", "Automotive SoCs", "5G Infrastructure Silicon"]',
    'https://www.truechip.net/ucie-vip'
);

-- 7. Seed Sessions
INSERT INTO sessions (id, event_id, day_number, time_range, title, track, room, session_type, speaker_name, speaker_designation, speaker_company, description, status)
VALUES
(
    'mpss26-d1-01',
    'MPSS26',
    1,
    '08:30 AM – 09:30 AM',
    'Delegate Registration & Welcome High Tea',
    'General',
    'Convention Center Foyer',
    'break',
    NULL,
    NULL,
    NULL,
    'On-site registration, badge badge verification, and opening networking refreshments.',
    'completed'
),
(
    'mpss26-d1-02',
    'MPSS26',
    1,
    '10:00 AM – 10:30 AM',
    'Keynote on Global Semiconductor Industry & Technology Trends',
    'Keynote',
    'Main Auditorium',
    'session',
    'Jaswinder Ahuja',
    'Advisor',
    'India Semiconductor Mission',
    'An overview of geopolitical shifts, packaging trends, and the strategic positioning of central India in the semiconductor value chain.',
    'live'
),
(
    'mpss26-d1-03',
    'MPSS26',
    1,
    '10:30 AM – 11:30 AM',
    'Panel: Building the Semiconductor Ecosystem in Madhya Pradesh',
    'Ecosystem & Policy',
    'Hall A',
    'session',
    'Dr. Amit Agarwal, Sanjay Gupta',
    'Director / President',
    'IIITM Gwalior / VLSI Society of India',
    'Discussion among government leaders and industry heads on fabless design incentives, talent incubation, and infrastructure readiness.',
    'upcoming'
);

-- 8. Seed Live Poll
INSERT INTO polls (id, session_id, question, status, total_votes)
VALUES (
    'poll-mpss26-01',
    'mpss26-d1-02',
    'Which semiconductor domain holds the highest potential for MP in the next 5 years?',
    'open',
    480
);

INSERT INTO poll_options (id, poll_id, text, votes, sort_order)
VALUES
('opt-1', 'poll-mpss26-01', 'Fabless Chip Design & Verification IP', 142, 1),
('opt-2', 'poll-mpss26-01', 'Advanced Packaging & ATMP Units', 198, 2),
('opt-3', 'poll-mpss26-01', 'Compound Semiconductors (SiC & GaN)', 95, 3),
('opt-4', 'poll-mpss26-01', 'Skilled Workforce & Academic Incubation', 45, 4);

-- 9. Seed Initial Leads for Truechip
INSERT INTO leads (id, exhibitor_id, attendee_id, status, source, notes, interests_json)
VALUES (
    'lead-init-01',
    'exh-truechip',
    'att-santosh-kumar',
    'Interested',
    'Summit Directory Lead Capture',
    'Expressed strong interest in PCIe Gen 6 VIP for state research incubation center.',
    '["PCIe Gen 6 / 7 VIP", "UCIe Chiplet Interconnect VIP"]'
);
