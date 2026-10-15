package com.eventsphere.modules.exhibitor;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "exhibitors")
public class ExhibitorEntity
{
    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "event_id", nullable = false, length = 32)
    private String eventId;

    @Column(nullable = false)
    private String company;

    @Column(nullable = false, length = 64)
    private String booth;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    @Column(name = "team_size", nullable = false)
    private int teamSize = 1;

    @Column(nullable = false, length = 32)
    private String status = "Active";

    @Column(name = "brochure_url", length = 512)
    private String brochureUrl;

    @Column(name = "brochure_name")
    private String brochureName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public ExhibitorEntity() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getBooth() { return booth; }
    public void setBooth(String booth) { this.booth = booth; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public int getTeamSize() { return teamSize; }
    public void setTeamSize(int teamSize) { this.teamSize = teamSize; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBrochureUrl() { return brochureUrl; }
    public void setBrochureUrl(String brochureUrl) { this.brochureUrl = brochureUrl; }

    public String getBrochureName() { return brochureName; }
    public void setBrochureName(String brochureName) { this.brochureName = brochureName; }

    public Instant getCreatedAt() { return createdAt; }
}
