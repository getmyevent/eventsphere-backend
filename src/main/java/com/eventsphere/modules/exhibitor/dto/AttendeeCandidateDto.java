package com.eventsphere.modules.exhibitor.dto;

import com.eventsphere.modules.exhibitor.model.LeadStatus;

import java.util.List;

public class AttendeeCandidateDto {

    private String id;
    private String name;
    private String avatar;
    private String designation;
    private String company;
    private String role;
    private String email;
    private String mobile;
    private String linkedin;

    @com.fasterxml.jackson.annotation.JsonProperty("isLead")
    private boolean isLead;

    private LeadStatus leadStatus;
    private String leadId;
    private List<String> interests;

    public AttendeeCandidateDto() {}

    public AttendeeCandidateDto(
            String id,
            String name,
            String avatar,
            String designation,
            String company,
            String role,
            String email,
            String mobile,
            String linkedin,
            boolean isLead,
            LeadStatus leadStatus,
            String leadId,
            List<String> interests
    ) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
        this.designation = designation;
        this.company = company;
        this.role = role;
        this.email = email;
        this.mobile = mobile;
        this.linkedin = linkedin;
        this.isLead = isLead;
        this.leadStatus = leadStatus;
        this.leadId = leadId;
        this.interests = interests;
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

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    @com.fasterxml.jackson.annotation.JsonProperty("isLead")
    public boolean isLead() { return isLead; }
    public void setLead(boolean lead) { isLead = lead; }

    public LeadStatus getLeadStatus() { return leadStatus; }
    public void setLeadStatus(LeadStatus leadStatus) { this.leadStatus = leadStatus; }

    public String getLeadId() { return leadId; }
    public void setLeadId(String leadId) { this.leadId = leadId; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }
}
