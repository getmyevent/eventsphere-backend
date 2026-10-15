package com.eventsphere.modules.attendee.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class AttendeeProfileDto {

    private String id;
    private String name;
    private String avatar;
    private String designation;
    private String company;
    private String role;
    private String ticketType;
    private String bio;
    private String email;
    private String mobile;
    private String linkedin;
    private String twitter;
    private Integer relevanceScore;
    private boolean checkedIn;
    private String registrationId;
    private PrivacyConsentDto privacy;
    private String connectionStatus = "none"; // 'none', 'pending', 'connected', 'incoming_request'
    private String connectionId;

    @JsonProperty("isSaved")
    private boolean isSaved;

    private List<String> skills = new ArrayList<>();
    private List<String> interests = new ArrayList<>();

    public AttendeeProfileDto() {}

    public AttendeeProfileDto(
            String id,
            String name,
            String avatar,
            String designation,
            String company,
            String role,
            String ticketType,
            String bio,
            String email,
            String mobile,
            String linkedin,
            String twitter,
            Integer relevanceScore,
            boolean checkedIn,
            String registrationId,
            PrivacyConsentDto privacy,
            String connectionStatus,
            String connectionId,
            boolean isSaved,
            List<String> skills,
            List<String> interests
    ) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.designation = designation;
        this.company = company;
        this.role = role;
        this.ticketType = ticketType;
        this.bio = bio;
        this.email = email;
        this.mobile = mobile;
        this.linkedin = linkedin;
        this.twitter = twitter;
        this.relevanceScore = relevanceScore;
        this.checkedIn = checkedIn;
        this.registrationId = registrationId;
        this.privacy = privacy;
        this.connectionStatus = connectionStatus != null ? connectionStatus : "none";
        this.connectionId = connectionId;
        this.isSaved = isSaved;
        this.skills = skills != null ? skills : new ArrayList<>();
        this.interests = interests != null ? interests : new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getTwitter() { return twitter; }
    public void setTwitter(String twitter) { this.twitter = twitter; }

    public Integer getRelevanceScore() { return relevanceScore; }
    public void setRelevanceScore(Integer relevanceScore) { this.relevanceScore = relevanceScore; }

    public boolean isCheckedIn() { return checkedIn; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }

    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    public PrivacyConsentDto getPrivacy() { return privacy; }
    public void setPrivacy(PrivacyConsentDto privacy) { this.privacy = privacy; }

    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) { this.connectionStatus = connectionStatus; }

    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }

    @JsonProperty("isSaved")
    public boolean isSaved() { return isSaved; }

    @JsonProperty("isSaved")
    public void setSaved(boolean saved) { isSaved = saved; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
}
