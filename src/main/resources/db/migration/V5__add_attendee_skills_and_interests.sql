-- =============================================================================
-- Flyway Migration: V5__add_attendee_skills_and_interests.sql
-- Add interests_json and skills_json columns to attendees table and populate
-- realistic tags, complete profile fields, and open public speaker profiles
-- =============================================================================

-- 1. Add interests_json and skills_json columns
ALTER TABLE attendees ADD COLUMN interests_json JSON NULL;
ALTER TABLE attendees ADD COLUMN skills_json JSON NULL;

-- 2. Populate Curated Key Profiles
UPDATE attendees 
SET interests_json = JSON_ARRAY('Event Tech', 'AI', 'Product Strategy'),
    skills_json = JSON_ARRAY('Product Leadership', 'Engineering', 'Architecture'),
    linkedin_url = 'https://linkedin.com/in/harigupta',
    twitter_url = 'https://x.com/harigupta'
WHERE id = 'att-hari-gupta';

UPDATE attendees 
SET interests_json = JSON_ARRAY('Semiconductors', 'Policy & Governance', 'Science & Technology'),
    skills_json = JSON_ARRAY('Scientific Advisory', 'Policy Analysis', 'Ecosystem Strategy'),
    linkedin_url = 'https://linkedin.com/in/santosh-kumar-aiggpa',
    twitter_url = 'https://x.com/DrVishvakarma?lang=en'
WHERE id = 'att-santosh-kumar';

UPDATE attendees 
SET interests_json = JSON_ARRAY('Enterprise SaaS', 'Cloud Computing', 'Product Strategy'),
    skills_json = JSON_ARRAY('Product Management', 'Agile Roadmaps', 'UX Architecture'),
    linkedin_url = 'https://linkedin.com/in/aaravsharma'
WHERE id = 'att-aarav-sharma';

UPDATE attendees 
SET interests_json = JSON_ARRAY('Real-time Data', 'Database Architecture', 'Infrastructure'),
    skills_json = JSON_ARRAY('Distributed Systems', 'Go / Rust', 'Cloud Native'),
    linkedin_url = 'https://linkedin.com/in/arjunpatel'
WHERE id = 'att-arjun-patel';

UPDATE attendees 
SET interests_json = JSON_ARRAY('Cloud Architecture', 'Enterprise AI', 'AWS Infrastructure'),
    skills_json = JSON_ARRAY('Solutions Architecture', 'Kubernetes', 'DevOps'),
    linkedin_url = 'https://linkedin.com/in/danielosei'
WHERE id = 'att-daniel-osei';

-- 3. Populate All Speakers with Semiconductor & VLSI Focus
UPDATE attendees 
SET interests_json = JSON_ARRAY('Semiconductors', 'Chip Design', 'VLSI Engineering', 'Wafer Fabrication'),
    skills_json = JSON_ARRAY('Semiconductor Engineering', 'Executive Leadership', 'Strategic Roadmap')
WHERE category = 'speaker';

-- 4. Populate Attendees based on designations
-- Product Managers / Leaders
UPDATE attendees 
SET interests_json = JSON_ARRAY('Product Management', 'Enterprise SaaS', 'Product Strategy'),
    skills_json = JSON_ARRAY('Product Strategy', 'Agile Roadmaps', 'User Research')
WHERE interests_json IS NULL AND (designation LIKE '%Product%' OR designation LIKE '%CPO%');

-- Data Science & AI / ML
UPDATE attendees 
SET interests_json = JSON_ARRAY('Artificial Intelligence', 'Machine Learning', 'Data Science'),
    skills_json = JSON_ARRAY('Deep Learning', 'Data Pipelines', 'Python / PyTorch')
WHERE interests_json IS NULL AND (designation LIKE '%Data%' OR designation LIKE '%AI%' OR designation LIKE '%ML%');

-- Cloud / DevOps / SRE
UPDATE attendees 
SET interests_json = JSON_ARRAY('Cloud Computing', 'DevOps', 'Distributed Systems'),
    skills_json = JSON_ARRAY('Kubernetes', 'CI/CD Pipelines', 'Cloud Architecture')
WHERE interests_json IS NULL AND (designation LIKE '%Cloud%' OR designation LIKE '%DevOps%' OR designation LIKE '%SRE%');

-- Security Engineers
UPDATE attendees 
SET interests_json = JSON_ARRAY('Cybersecurity', 'Zero Trust', 'Threat Modeling'),
    skills_json = JSON_ARRAY('Security Engineering', 'Penetration Testing', 'Compliance')
WHERE interests_json IS NULL AND designation LIKE '%Security%';

-- UX / UI Designers
UPDATE attendees 
SET interests_json = JSON_ARRAY('UI/UX Design', 'Design Systems', 'User Research'),
    skills_json = JSON_ARRAY('Figma', 'Interaction Design', 'Usability Testing')
WHERE interests_json IS NULL AND (designation LIKE '%UX%' OR designation LIKE '%UI%' OR designation LIKE '%Design%');

-- Software Developers / Engineers / Tech Leads / CTO
UPDATE attendees 
SET interests_json = JSON_ARRAY('Software Engineering', 'System Architecture', 'Cloud Services'),
    skills_json = JSON_ARRAY('Full Stack Development', 'Microservices', 'Distributed Computing')
WHERE interests_json IS NULL AND (designation LIKE '%Engineer%' OR designation LIKE '%Developer%' OR designation LIKE '%Lead%' OR designation LIKE '%CTO%' OR designation LIKE '%VP%');

-- Any remaining attendees
UPDATE attendees 
SET interests_json = JSON_ARRAY('Semiconductor Ecosystem', 'Technology Innovation', 'Strategic Growth'),
    skills_json = JSON_ARRAY('Executive Leadership', 'Technology Management', 'Strategic Roadmap')
WHERE interests_json IS NULL;

-- 5. Open privacy consent for Speakers and ensure LinkedIn visibility
UPDATE privacy_consents pc 
JOIN attendees a ON pc.attendee_id = a.id
SET pc.preset = 'Open', 
    pc.share_email = 1, 
    pc.share_phone = 0, 
    pc.share_linkedin = 1, 
    pc.share_twitter = 1
WHERE a.category = 'speaker';

-- Set default share_linkedin to 1 across attendees
UPDATE privacy_consents
SET share_linkedin = 1
WHERE share_linkedin = 0;
