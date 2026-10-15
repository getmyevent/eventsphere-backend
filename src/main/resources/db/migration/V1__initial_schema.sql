-- =============================================================================
-- Flyway Migration: V1__initial_schema.sql
-- Module: Core Domain Schema for EventSphere
-- Database: MySQL 8.0+
-- =============================================================================

-- 1. Events Table
CREATE TABLE events (
    id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    dates_display VARCHAR(64) NOT NULL,
    city VARCHAR(100) NOT NULL,
    venue VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'UPCOMING',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Attendees Table
CREATE TABLE attendees (
    id VARCHAR(64) PRIMARY KEY,
    event_id VARCHAR(32) NOT NULL,
    registration_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    mobile VARCHAR(32) NOT NULL,
    normalized_mobile VARCHAR(20) NOT NULL,
    designation VARCHAR(255),
    company VARCHAR(255),
    category VARCHAR(32) NOT NULL DEFAULT 'delegate',
    auth_role VARCHAR(32) NOT NULL DEFAULT 'ROLE_DELEGATE',
    ticket_type VARCHAR(100) NOT NULL DEFAULT 'Delegate',
    bio TEXT,
    avatar_url VARCHAR(512),
    linkedin_url VARCHAR(255),
    twitter_url VARCHAR(255),
    relevance_score INT DEFAULT 0,
    checked_in BOOLEAN NOT NULL DEFAULT FALSE,
    checked_in_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_attendees_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT uk_attendees_event_reg UNIQUE (event_id, registration_id),
    CONSTRAINT uk_attendees_id_event UNIQUE (id, event_id),
    INDEX idx_attendees_event_mobile (event_id, normalized_mobile),
    INDEX idx_attendees_event_category (event_id, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Privacy Consents
CREATE TABLE privacy_consents (
    attendee_id VARCHAR(64) PRIMARY KEY,
    preset VARCHAR(32) NOT NULL DEFAULT 'Connections Only',
    share_email BOOLEAN NOT NULL DEFAULT TRUE,
    share_phone BOOLEAN NOT NULL DEFAULT TRUE,
    share_linkedin BOOLEAN NOT NULL DEFAULT TRUE,
    share_twitter BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_privacy_attendee FOREIGN KEY (attendee_id) REFERENCES attendees(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Attendee Interests & Skills Tags
CREATE TABLE attendee_tags (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    attendee_id VARCHAR(64) NOT NULL,
    tag_type VARCHAR(32) NOT NULL,
    tag_value VARCHAR(100) NOT NULL,
    CONSTRAINT fk_tags_attendee FOREIGN KEY (attendee_id) REFERENCES attendees(id) ON DELETE CASCADE,
    INDEX idx_tags_lookup (tag_type, tag_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Exhibitors Table
CREATE TABLE exhibitors (
    id VARCHAR(64) PRIMARY KEY,
    event_id VARCHAR(32) NOT NULL,
    company VARCHAR(255) NOT NULL,
    booth VARCHAR(64) NOT NULL,
    category VARCHAR(100) NOT NULL,
    logo_url VARCHAR(512),
    team_size INT NOT NULL DEFAULT 1,
    status VARCHAR(32) NOT NULL DEFAULT 'Active',
    brochure_url VARCHAR(512),
    brochure_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_exhibitors_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT uk_exhibitors_id_event UNIQUE (id, event_id),
    INDEX idx_exhibitors_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Exhibitor Staff Mapping (Compound FK ensures same event)
CREATE TABLE exhibitor_staff (
    event_id VARCHAR(32) NOT NULL,
    exhibitor_id VARCHAR(64) NOT NULL,
    attendee_id VARCHAR(64) NOT NULL,
    is_booth_admin BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (exhibitor_id, attendee_id),
    CONSTRAINT fk_staff_exhibitor_event FOREIGN KEY (exhibitor_id, event_id) REFERENCES exhibitors(id, event_id) ON DELETE CASCADE,
    CONSTRAINT fk_staff_attendee_event FOREIGN KEY (attendee_id, event_id) REFERENCES attendees(id, event_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Products Table
CREATE TABLE products (
    id VARCHAR(64) PRIMARY KEY,
    exhibitor_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    tagline VARCHAR(255) NOT NULL,
    category VARCHAR(100) NOT NULL,
    image_url VARCHAR(512),
    views INT NOT NULL DEFAULT 0,
    enquiries INT NOT NULL DEFAULT 0,
    interested_visitors_count INT NOT NULL DEFAULT 0,
    overview TEXT NOT NULL,
    features_json JSON,
    benefits_json JSON,
    use_cases_json JSON,
    website_url VARCHAR(512),
    brochure_url VARCHAR(512),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_exhibitor FOREIGN KEY (exhibitor_id) REFERENCES exhibitors(id) ON DELETE CASCADE,
    INDEX idx_products_exhibitor (exhibitor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Leads Table (Default 'New', Unique per exhibitor and attendee)
CREATE TABLE leads (
    id VARCHAR(64) PRIMARY KEY,
    exhibitor_id VARCHAR(64) NOT NULL,
    attendee_id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'New',
    source VARCHAR(100) NOT NULL DEFAULT 'Directory Lead Capture',
    notes TEXT,
    interests_json JSON,
    products_viewed_json JSON,
    sync_status VARCHAR(32) NOT NULL DEFAULT 'synced',
    captured_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_leads_exhibitor FOREIGN KEY (exhibitor_id) REFERENCES exhibitors(id) ON DELETE CASCADE,
    CONSTRAINT fk_leads_attendee FOREIGN KEY (attendee_id) REFERENCES attendees(id) ON DELETE CASCADE,
    CONSTRAINT uk_leads_exhibitor_attendee UNIQUE (exhibitor_id, attendee_id),
    INDEX idx_leads_exhibitor_status (exhibitor_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Product Enquiries / Interactions
CREATE TABLE product_interactions (
    id VARCHAR(64) PRIMARY KEY,
    product_id VARCHAR(64) NOT NULL,
    attendee_id VARCHAR(64) NOT NULL,
    interaction_type VARCHAR(32) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pi_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_pi_attendee FOREIGN KEY (attendee_id) REFERENCES attendees(id) ON DELETE CASCADE,
    INDEX idx_pi_product (product_id, interaction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Sessions Table (Agenda)
CREATE TABLE sessions (
    id VARCHAR(64) PRIMARY KEY,
    event_id VARCHAR(32) NOT NULL,
    day_number INT NOT NULL,
    time_range VARCHAR(64) NOT NULL,
    title VARCHAR(255) NOT NULL,
    track VARCHAR(100) NOT NULL,
    room VARCHAR(100) NOT NULL,
    session_type VARCHAR(32) NOT NULL DEFAULT 'session',
    speaker_name VARCHAR(255),
    speaker_designation VARCHAR(255),
    speaker_company VARCHAR(255),
    description TEXT,
    takeaways_json JSON,
    status VARCHAR(32) NOT NULL DEFAULT 'upcoming',
    attendance_count INT NOT NULL DEFAULT 0,
    questions_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sessions_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    INDEX idx_sessions_event_day (event_id, day_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Session Bookmarks (Per-User Scope)
CREATE TABLE session_bookmarks (
    session_id VARCHAR(64) NOT NULL,
    attendee_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (session_id, attendee_id),
    CONSTRAINT fk_sb_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_sb_attendee FOREIGN KEY (attendee_id) REFERENCES attendees(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Session Questions (Defaults to 0 upvotes)
CREATE TABLE session_questions (
    id VARCHAR(64) PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    attendee_id VARCHAR(64) NOT NULL,
    text TEXT NOT NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT FALSE,
    author_name_display VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'Submitted',
    upvotes INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sq_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE,
    CONSTRAINT fk_sq_attendee FOREIGN KEY (attendee_id) REFERENCES attendees(id) ON DELETE CASCADE,
    INDEX idx_sq_session_status (session_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Question Upvotes
CREATE TABLE question_upvotes (
    question_id VARCHAR(64) NOT NULL,
    attendee_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (question_id, attendee_id),
    CONSTRAINT fk_qu_question FOREIGN KEY (question_id) REFERENCES session_questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_qu_attendee FOREIGN KEY (attendee_id) REFERENCES attendees(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Polls & Poll Options (Compound unique constraint on poll_id, id)
CREATE TABLE polls (
    id VARCHAR(64) PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    question VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'open',
    total_votes INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_polls_session FOREIGN KEY (session_id) REFERENCES sessions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE poll_options (
    id VARCHAR(64) NOT NULL,
    poll_id VARCHAR(64) NOT NULL,
    text VARCHAR(255) NOT NULL,
    votes INT NOT NULL DEFAULT 0,
    sort_order INT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_poll_option UNIQUE (poll_id, id),
    CONSTRAINT fk_po_poll FOREIGN KEY (poll_id) REFERENCES polls(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Poll Votes (Compound FK ensures option belongs to this poll)
CREATE TABLE poll_votes (
    poll_id VARCHAR(64) NOT NULL,
    attendee_id VARCHAR(64) NOT NULL,
    option_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (poll_id, attendee_id),
    CONSTRAINT fk_pv_poll FOREIGN KEY (poll_id) REFERENCES polls(id) ON DELETE CASCADE,
    CONSTRAINT fk_pv_attendee FOREIGN KEY (attendee_id) REFERENCES attendees(id) ON DELETE CASCADE,
    CONSTRAINT fk_pv_poll_and_option FOREIGN KEY (poll_id, option_id) REFERENCES poll_options(poll_id, id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. Mutual Connections (Canonical pair columns prevent dual reciprocal rows)
CREATE TABLE connections (
    id VARCHAR(64) PRIMARY KEY,
    event_id VARCHAR(32) NOT NULL,
    sender_id VARCHAR(64) NOT NULL,
    receiver_id VARCHAR(64) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'pending',
    source VARCHAR(32) NOT NULL DEFAULT 'direct',
    user_low VARCHAR(64) NOT NULL,
    user_high VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_conn_event FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_conn_sender FOREIGN KEY (sender_id) REFERENCES attendees(id) ON DELETE CASCADE,
    CONSTRAINT fk_conn_receiver FOREIGN KEY (receiver_id) REFERENCES attendees(id) ON DELETE CASCADE,
    CONSTRAINT uk_conn_canonical_pair UNIQUE (event_id, user_low, user_high),
    INDEX idx_conn_receiver_status (receiver_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. Normalized Saved Items with true Foreign Keys
CREATE TABLE saved_people (
    attendee_id VARCHAR(64) NOT NULL,
    target_person_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (attendee_id, target_person_id),
    CONSTRAINT fk_sp_attendee FOREIGN KEY (attendee_id) REFERENCES attendees(id) ON DELETE CASCADE,
    CONSTRAINT fk_sp_target FOREIGN KEY (target_person_id) REFERENCES attendees(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE saved_exhibitors (
    attendee_id VARCHAR(64) NOT NULL,
    target_exhibitor_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (attendee_id, target_exhibitor_id),
    CONSTRAINT fk_se_attendee FOREIGN KEY (attendee_id) REFERENCES attendees(id) ON DELETE CASCADE,
    CONSTRAINT fk_se_target FOREIGN KEY (target_exhibitor_id) REFERENCES exhibitors(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE saved_products (
    attendee_id VARCHAR(64) NOT NULL,
    target_product_id VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (attendee_id, target_product_id),
    CONSTRAINT fk_spr_attendee FOREIGN KEY (attendee_id) REFERENCES attendees(id) ON DELETE CASCADE,
    CONSTRAINT fk_spr_target FOREIGN KEY (target_product_id) REFERENCES products(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
