-- =============================================================================
-- Flyway Migration: V11__add_delegate_email_index.sql
-- Add index on attendees(email) for high-concurrency delegate lookup
-- Supports Google OAuth and Email OTP authentication under peak loads
-- =============================================================================

CREATE INDEX idx_delegate_email ON attendees (email);
