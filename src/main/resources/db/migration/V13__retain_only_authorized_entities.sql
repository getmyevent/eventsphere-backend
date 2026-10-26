-- =============================================================================
-- Flyway Migration: V13__retain_only_authorized_entities.sql
-- Retain ONLY the 4 authorized entities and delete all other attendee entries
-- =============================================================================

SET FOREIGN_KEY_CHECKS = 0;

-- 1. Insert or update the 4 authorized entities
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
    'att-sanjeev-keskar',
    'MPSS26',
    '#REG-MPSS26-1078',
    'Sanjeev Keskar',
    'sanjeev.keskar@arvindconsultancy.com',
    '+91 98450 21078',
    '9845021078',
    'CEO',
    'Arvind Consultancy',
    'delegate',
    'ROLE_DELEGATE',
    'Full Access Delegate Pass',
    'CEO at Arvind Consultancy. Veteran semiconductor industry leader and featured summit participant.',
    'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&auto=format&fit=crop&q=80',
    TRUE
),
(
    'att-hari-gupta-7046',
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
    'att-pratosh-bansal',
    'MPSS26',
    '#REG-MPSS26-3512',
    'Dr. Pratosh Bansal',
    'pbansal@ietdavv.edu.in',
    '+91 99816 43512',
    '9981643512',
    'Director',
    'IET DAVV',
    'delegate',
    'ROLE_DELEGATE',
    'Director',
    'Director at Institute of Engineering & Technology, Devi Ahilya Vishwavidyalaya (IET-DAVV), Indore. Driving institutional research and industry partnerships.',
    '/images/mentors/pratosh-bansal.jpg',
    TRUE
),
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
    'https://res.cloudinary.com/vc0wtqtw/image/upload/v1792450462/hari.png',
    TRUE
)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    designation = VALUES(designation),
    company = VALUES(company),
    category = VALUES(category),
    auth_role = VALUES(auth_role),
    ticket_type = VALUES(ticket_type),
    email = VALUES(email),
    mobile = VALUES(mobile),
    normalized_mobile = VALUES(normalized_mobile),
    bio = VALUES(bio),
    avatar_url = VALUES(avatar_url);

-- 2. Delete ALL other attendee records from the database
DELETE FROM attendees 
WHERE id NOT IN ('att-sanjeev-keskar', 'att-hari-gupta-7046', 'att-pratosh-bansal', 'att-hari-gupta')
  AND (normalized_mobile NOT IN ('9845021078', '7089157046', '9981643512', '9876543210') OR normalized_mobile IS NULL);

-- 3. Clean up associated tables
DELETE FROM privacy_consents 
WHERE attendee_id NOT IN ('att-sanjeev-keskar', 'att-hari-gupta-7046', 'att-pratosh-bansal', 'att-hari-gupta');

DELETE FROM attendee_tags 
WHERE attendee_id NOT IN ('att-sanjeev-keskar', 'att-hari-gupta-7046', 'att-pratosh-bansal', 'att-hari-gupta');

DELETE FROM connections 
WHERE sender_id NOT IN ('att-sanjeev-keskar', 'att-hari-gupta-7046', 'att-pratosh-bansal', 'att-hari-gupta')
   OR receiver_id NOT IN ('att-sanjeev-keskar', 'att-hari-gupta-7046', 'att-pratosh-bansal', 'att-hari-gupta');

DELETE FROM saved_people 
WHERE attendee_id NOT IN ('att-sanjeev-keskar', 'att-hari-gupta-7046', 'att-pratosh-bansal', 'att-hari-gupta')
   OR target_person_id NOT IN ('att-sanjeev-keskar', 'att-hari-gupta-7046', 'att-pratosh-bansal', 'att-hari-gupta');

-- 4. Ensure privacy consents for all 4 authorized entities
INSERT IGNORE INTO privacy_consents (attendee_id, preset, share_email, share_phone, share_linkedin)
VALUES 
    ('att-sanjeev-keskar', 'Connections Only', TRUE, TRUE, TRUE),
    ('att-hari-gupta-7046', 'Connections Only', TRUE, TRUE, TRUE),
    ('att-pratosh-bansal', 'Connections Only', TRUE, TRUE, TRUE),
    ('att-hari-gupta', 'Connections Only', TRUE, TRUE, TRUE);

SET FOREIGN_KEY_CHECKS = 1;
