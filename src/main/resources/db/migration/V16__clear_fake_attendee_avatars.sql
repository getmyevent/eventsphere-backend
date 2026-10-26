-- =============================================================================
-- Flyway Migration: V16__clear_fake_attendee_avatars.sql
-- Clear fake stock photo avatar URLs from attendees to use initials fallback
-- =============================================================================

UPDATE attendees 
SET avatar_url = '' 
WHERE id IN ('att-sanjeev-keskar', 'att-pratosh-bansal')
   OR avatar_url LIKE '%unsplash%'
   OR avatar_url LIKE '%pravatar%'
   OR avatar_url LIKE '%pratosh-bansal.jpg%';
