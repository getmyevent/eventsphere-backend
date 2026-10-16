package com.eventsphere.modules.session;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sessions")
public class SessionEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "event_id", nullable = false, length = 32)
    private String eventId;

    @Column(name = "day_number", nullable = false)
    private int dayNumber = 1;

    @Column(name = "time_range", nullable = false, length = 64)
    private String timeRange;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 100)
    private String track;

    @Column(nullable = false, length = 100)
    private String room;

    @Column(name = "session_type", nullable = false, length = 32)
    private String sessionType = "session";

    @Column(name = "speaker_name")
    private String speakerName;

    @Column(name = "speaker_designation")
    private String speakerDesignation;

    @Column(name = "speaker_company")
    private String speakerCompany;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "takeaways_json", columnDefinition = "JSON")
    private String takeawaysJson;

    @Column(name = "speaker_ids_json", columnDefinition = "JSON")
    private String speakerIdsJson;

    @Column(nullable = false, length = 32)
    private String status = "upcoming";

    @Column(name = "attendance_count", nullable = false)
    private int attendanceCount = 0;

    @Column(name = "questions_count", nullable = false)
    private int questionsCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public SessionEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public int getDayNumber() { return dayNumber; }
    public void setDayNumber(int dayNumber) { this.dayNumber = dayNumber; }

    public String getTimeRange() { return timeRange; }
    public void setTimeRange(String timeRange) { this.timeRange = timeRange; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTrack() { return track; }
    public void setTrack(String track) { this.track = track; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    public String getSessionType() { return sessionType; }
    public void setSessionType(String sessionType) { this.sessionType = sessionType; }

    public String getSpeakerName() { return speakerName; }
    public void setSpeakerName(String speakerName) { this.speakerName = speakerName; }

    public String getSpeakerDesignation() { return speakerDesignation; }
    public void setSpeakerDesignation(String speakerDesignation) { this.speakerDesignation = speakerDesignation; }

    public String getSpeakerCompany() { return speakerCompany; }
    public void setSpeakerCompany(String speakerCompany) { this.speakerCompany = speakerCompany; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTakeawaysJson() { return takeawaysJson; }
    public void setTakeawaysJson(String takeawaysJson) { this.takeawaysJson = takeawaysJson; }

    public String getSpeakerIdsJson() { return speakerIdsJson; }
    public void setSpeakerIdsJson(String speakerIdsJson) { this.speakerIdsJson = speakerIdsJson; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getAttendanceCount() { return attendanceCount; }
    public void setAttendanceCount(int attendanceCount) { this.attendanceCount = attendanceCount; }

    public int getQuestionsCount() { return questionsCount; }
    public void setQuestionsCount(int questionsCount) { this.questionsCount = questionsCount; }

    public Instant getCreatedAt() { return createdAt; }
}
