package com.eventsphere.modules.auth.dto;

public class UserSummaryDto {

    private String id;
    private String name;
    private String category;
    private String role;
    private String exhibitorId;
    private String eventId;
    private String company;
    private String designation;
    private String email;
    private String mobile;
    private String avatar;
    private String ticketType;
    private boolean checkedIn;
    private String boothNumber;

    public UserSummaryDto() {}

    public UserSummaryDto(
            String id,
            String name,
            String category,
            String role,
            String exhibitorId,
            String eventId,
            String company,
            String designation,
            String email,
            String mobile,
            String avatar,
            String ticketType,
            boolean checkedIn,
            String boothNumber
    ) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.role = role;
        this.exhibitorId = exhibitorId;
        this.eventId = eventId;
        this.company = company;
        this.designation = designation;
        this.email = email;
        this.mobile = mobile;
        this.avatar = avatar;
        this.ticketType = ticketType;
        this.checkedIn = checkedIn;
        this.boothNumber = boothNumber;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getExhibitorId() { return exhibitorId; }
    public void setExhibitorId(String exhibitorId) { this.exhibitorId = exhibitorId; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }

    public boolean isCheckedIn() { return checkedIn; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }

    public String getBoothNumber() { return boothNumber; }
    public void setBoothNumber(String boothNumber) { this.boothNumber = boothNumber; }
}
