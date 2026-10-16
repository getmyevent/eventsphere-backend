package com.eventsphere.modules.attendee.dto;

public class UpdateProfileRequest {

    private String name;
    private String designation;
    private String company;
    private String bio;
    private String avatar;
    private String linkedin;
    private String twitter;
    private String email;
    private String mobile;
    private String ticketType;
    private String category;
    private java.util.List<String> skills;
    private java.util.List<String> interests;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String name, String designation, String company, String bio, String avatar, String linkedin, String twitter, String email, String mobile) {
        this.name = name;
        this.designation = designation;
        this.company = company;
        this.bio = bio;
        this.avatar = avatar;
        this.linkedin = linkedin;
        this.twitter = twitter;
        this.email = email;
        this.mobile = mobile;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getTwitter() { return twitter; }
    public void setTwitter(String twitter) { this.twitter = twitter; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public java.util.List<String> getSkills() { return skills; }
    public void setSkills(java.util.List<String> skills) { this.skills = skills; }

    public java.util.List<String> getInterests() { return interests; }
    public void setInterests(java.util.List<String> interests) { this.interests = interests; }
}
