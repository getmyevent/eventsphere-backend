-- =============================================================================
-- Flyway Migration: V10__add_poll_active_flag.sql
-- Add is_active column to polls table to track which poll is currently live.
-- =============================================================================

ALTER TABLE polls ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT FALSE;

-- Mark the initial summit poll as active by default
UPDATE polls SET is_active = TRUE WHERE id = 'poll-mpss26-01';

-- Create index for fast active poll lookup
CREATE INDEX idx_polls_active_status ON polls (is_active, status);
