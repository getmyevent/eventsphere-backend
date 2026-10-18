-- =============================================================================
-- Flyway Migration: V9__ensure_organizer_accounts.sql
-- Guarantee that organizer accounts exist with auth_role = 'ROLE_ORGANIZER'
-- and category = 'organizer_staff' across all database environments.
-- =============================================================================

-- 1. Ensure primary organizer account (Operations Lead: 9876543212)
INSERT INTO attendees (
    id,
    event_id,
    registration_id,
    name,
    email,
    mobile,
    normalized_mobile,
    designation,
    company,
    category,
    auth_role,
    ticket_type,
    bio,
    avatar_url,
    checked_in
) VALUES (
    'att-organizer-staff-01',
    'MPSS26',
    '#REG-MPSS26-3212',
    'Summit Operations Lead',
    'ops@mpss26.org',
    '+91 98765 43212',
    '9876543212',
    'Operations Director',
    'MPSEDC & EventSphere Operations',
    'organizer_staff',
    'ROLE_ORGANIZER',
    'Organizer All-Access Pass',
    'Organizing committee operations and stage moderation console manager.',
    '/images/avatars/hari-gupta.png',
    TRUE
)
ON DUPLICATE KEY UPDATE
    auth_role = 'ROLE_ORGANIZER',
    category = 'organizer_staff',
    mobile = '+91 98765 43212',
    normalized_mobile = '9876543212';

-- 2. Ensure secondary organizer admin account (Super Admin: 9999999999)
INSERT INTO attendees (
    id,
    event_id,
    registration_id,
    name,
    email,
    mobile,
    normalized_mobile,
    designation,
    company,
    category,
    auth_role,
    ticket_type,
    bio,
    avatar_url,
    checked_in
) VALUES (
    'att-organizer-admin-01',
    'MPSS26',
    '#ORG-MPSS26-0001',
    'EventSphere Chief Organizer',
    'organizer@eventsphere.com',
    '+91 99999 99999',
    '9999999999',
    'Head of Operations & Logistics',
    'MPSEDC & EventSphere Operations',
    'organizer_staff',
    'ROLE_ORGANIZER',
    'Organizer All-Access Pass',
    'Chief event administrator with all-access permissions across sessions, attendees, and polls.',
    '/images/avatars/hari-gupta.png',
    TRUE
)
ON DUPLICATE KEY UPDATE
    auth_role = 'ROLE_ORGANIZER',
    category = 'organizer_staff',
    mobile = '+91 99999 99999',
    normalized_mobile = '9999999999';

-- 3. Ensure privacy consent records exist for both organizer accounts
INSERT IGNORE INTO privacy_consents (attendee_id, preset, share_email, share_phone, share_linkedin)
VALUES 
    ('att-organizer-staff-01', 'Open', TRUE, TRUE, TRUE),
    ('att-organizer-admin-01', 'Open', TRUE, TRUE, TRUE);
