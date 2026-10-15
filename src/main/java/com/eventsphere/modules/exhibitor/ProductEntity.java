package com.eventsphere.modules.exhibitor;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "exhibitor_id", nullable = false, length = 64)
    private String exhibitorId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String tagline;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(nullable = false)
    private int views = 0;

    @Column(nullable = false)
    private int enquiries = 0;

    @Column(name = "interested_visitors_count", nullable = false)
    private int interestedVisitorsCount = 0;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String overview;

    @Column(name = "features_json", columnDefinition = "JSON")
    private String featuresJson;

    @Column(name = "benefits_json", columnDefinition = "JSON")
    private String benefitsJson;

    @Column(name = "use_cases_json", columnDefinition = "JSON")
    private String useCasesJson;

    @Column(name = "website_url", length = 512)
    private String websiteUrl;

    @Column(name = "brochure_url", length = 512)
    private String brochureUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public ProductEntity() {}

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getExhibitorId() { return exhibitorId; }
    public void setExhibitorId(String exhibitorId) { this.exhibitorId = exhibitorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public int getEnquiries() { return enquiries; }
    public void setEnquiries(int enquiries) { this.enquiries = enquiries; }

    public int getInterestedVisitorsCount() { return interestedVisitorsCount; }
    public void setInterestedVisitorsCount(int interestedVisitorsCount) { this.interestedVisitorsCount = interestedVisitorsCount; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public String getFeaturesJson() { return featuresJson; }
    public void setFeaturesJson(String featuresJson) { this.featuresJson = featuresJson; }

    public String getBenefitsJson() { return benefitsJson; }
    public void setBenefitsJson(String benefitsJson) { this.benefitsJson = benefitsJson; }

    public String getUseCasesJson() { return useCasesJson; }
    public void setUseCasesJson(String useCasesJson) { this.useCasesJson = useCasesJson; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public String getBrochureUrl() { return brochureUrl; }
    public void setBrochureUrl(String brochureUrl) { this.brochureUrl = brochureUrl; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
