-- =============================================================================
-- Flyway Migration: V8__add_unique_constraint_and_indexes.sql
-- 1. Unique constraint on (event_id, normalized_mobile) to prevent duplicate login rows
-- 2. Performance indexes for name search and exhibitor leads lookup
-- =============================================================================

-- Normalize organizer mobile to standard 10-digit
UPDATE attendees 
SET mobile = '+91 98765 43212', normalized_mobile = '9876543212' 
WHERE id = 'att-organizer-staff-01';

ALTER TABLE attendees ADD CONSTRAINT uk_attendees_event_normalized_mobile UNIQUE (event_id, normalized_mobile);

CREATE INDEX idx_attendees_event_name ON attendees(event_id, name);
CREATE INDEX idx_leads_event_exhibitor ON leads(exhibitor_id, status);
