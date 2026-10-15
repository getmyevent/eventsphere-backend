package com.eventsphere.modules.exhibitor.dto;

import com.eventsphere.modules.exhibitor.model.LeadStatus;

import java.util.ArrayList;
import java.util.List;

public class LeadDto {

    private String id;
    private String personId;
    private String name;
    private String avatar;
    private String company;
    private String designation;
    private String email;
    private String mobile;
    private String linkedin;
    private String source;
    private LeadStatus status;
    private List<String> interests = new ArrayList<>();
    private String notes;
    private String capturedAt;
    private String boothId;
    private List<String> productsViewed = new ArrayList<>();
    private String syncStatus = "synced";

    public LeadDto() {}

    public LeadDto(
            String id,
            String personId,
            String name,
            String avatar,
            String company,
            String designation,
            String email,
            String mobile,
            String linkedin,
            String source,
            LeadStatus status,
            List<String> interests,
            String notes,
            String capturedAt,
            String boothId,
            List<String> productsViewed,
            String syncStatus
    ) {
        this.id = id;
        this.personId = personId;
        this.name = name;
        this.avatar = avatar;
        this.company = company;
        this.designation = designation;
        this.email = email;
        this.mobile = mobile;
        this.linkedin = linkedin;
        this.source = source;
        this.status = status;
        this.interests = interests != null ? interests : new ArrayList<>();
        this.notes = notes;
        this.capturedAt = capturedAt;
        this.boothId = boothId;
        this.productsViewed = productsViewed != null ? productsViewed : new ArrayList<>();
        this.syncStatus = syncStatus != null ? syncStatus : "synced";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LeadStatus getStatus() { return status; }
    public void setStatus(LeadStatus status) { this.status = status; }

    public List<String> getInterests() { return interests; }
    public void setInterests(List<String> interests) { this.interests = interests; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCapturedAt() { return capturedAt; }
    public void setCapturedAt(String capturedAt) { this.capturedAt = capturedAt; }

    public String getBoothId() { return boothId; }
    public void setBoothId(String boothId) { this.boothId = boothId; }

    public List<String> getProductsViewed() { return productsViewed; }
    public void setProductsViewed(List<String> productsViewed) { this.productsViewed = productsViewed; }

    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
}
