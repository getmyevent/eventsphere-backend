-- =============================================================================
-- Flyway Migration: V12__seed_authorized_delegate_logins.sql
-- Seed / update registered delegates with authorized login phone numbers
-- =============================================================================

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
) VALUES 
(
    'att-delegate-9845021078',
    'MPSS26',
    '#REG-MPSS26-1078',
    'Summit Delegate',
    'delegate.1078@getmyevent.in',
    '+91 98450 21078',
    '9845021078',
    'Delegate',
    'MPSS 2026',
    'delegate',
    'ROLE_DELEGATE',
    'Full Access Delegate Pass',
    'Registered delegate attending Madhya Pradesh Semiconductor Summit 2026.',
    '/images/avatars/hari-gupta.png',
    TRUE
),
(
    'att-delegate-7089157046',
    'MPSS26',
    '#REG-MPSS26-7046',
    'Hari Gupta',
    'harigupta@getmyevent.in',
    '+91 70891 57046',
    '7089157046',
    'Co-Founder',
    'GetMyEvent',
    'delegate',
    'ROLE_DELEGATE',
    'Full Access Delegate Pass',
    'Co-Founder building event technology and delegate experience platforms.',
    'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792450462/hari.png',
    TRUE
),
(
    'att-delegate-9981643512',
    'MPSS26',
    '#REG-MPSS26-3512',
    'VIP Delegate',
    'delegate.3512@getmyevent.in',
    '+91 99816 43512',
    '9981643512',
    'VIP Delegate',
    'MPSS 2026',
    'delegate',
    'ROLE_DELEGATE',
    'VIP Delegate Pass',
    'VIP delegate participating in leadership roundtables and industry panels.',
    '/images/avatars/hari-gupta.png',
    TRUE
)
ON DUPLICATE KEY UPDATE
    auth_role = VALUES(auth_role),
    category = VALUES(category),
    mobile = VALUES(mobile),
    normalized_mobile = VALUES(normalized_mobile);

-- Ensure privacy consents exist
INSERT IGNORE INTO privacy_consents (attendee_id, preset, share_email, share_phone, share_linkedin)
VALUES 
    ('att-delegate-9845021078', 'Connections Only', TRUE, TRUE, TRUE),
    ('att-delegate-7089157046', 'Connections Only', TRUE, TRUE, TRUE),
    ('att-delegate-9981643512', 'Connections Only', TRUE, TRUE, TRUE);
