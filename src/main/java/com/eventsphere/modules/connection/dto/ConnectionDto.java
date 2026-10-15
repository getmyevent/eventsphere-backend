package com.eventsphere.modules.connection.dto;

public class ConnectionDto {

    private String id;
    private String personId;
    private String personName;
    private String designation;
    private String company;
    private String avatar;
    private String status; // 'pending' | 'connected'
    private String type;   // 'sent' | 'received'
    private String source; // 'direct' | 'qr_scan' | 'recommended'
    private String connectedAt;

    public ConnectionDto() {}

    public ConnectionDto(
            String id,
            String personId,
            String personName,
            String designation,
            String company,
            String avatar,
            String status,
            String type,
            String source,
            String connectedAt
    ) {
        this.id = id;
        this.personId = personId;
        this.personName = personName;
        this.designation = designation;
        this.company = company;
        this.avatar = avatar;
        this.status = status;
        this.type = type;
        this.source = source;
        this.connectedAt = connectedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPersonId() { return personId; }
    public void setPersonId(String personId) { this.personId = personId; }

    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }

    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getConnectedAt() { return connectedAt; }
    public void setConnectedAt(String connectedAt) { this.connectedAt = connectedAt; }
}
