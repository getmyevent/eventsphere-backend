package com.eventsphere.modules.exhibitor.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    @NotBlank(message = "Product tagline is required")
    private String tagline;

    @NotBlank(message = "Product category is required")
    private String category;

    @NotBlank(message = "Product overview is required")
    private String overview;

    private List<String> features;
    private List<String> benefits;
    private List<String> useCases;
    private String websiteUrl;
    private String brochureUrl;
    private String imageUrl;

    public CreateProductRequest() {}

    public CreateProductRequest(
            String name,
            String tagline,
            String category,
            String overview,
            List<String> features,
            List<String> benefits,
            List<String> useCases,
            String websiteUrl,
            String brochureUrl,
            String imageUrl
    ) {
        this.name = name;
        this.tagline = tagline;
        this.category = category;
        this.overview = overview;
        this.features = features;
        this.benefits = benefits;
        this.useCases = useCases;
        this.websiteUrl = websiteUrl;
        this.brochureUrl = brochureUrl;
        this.imageUrl = imageUrl;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public List<String> getBenefits() { return benefits; }
    public void setBenefits(List<String> benefits) { this.benefits = benefits; }

    public List<String> getUseCases() { return useCases; }
    public void setUseCases(List<String> useCases) { this.useCases = useCases; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public String getBrochureUrl() { return brochureUrl; }
    public void setBrochureUrl(String brochureUrl) { this.brochureUrl = brochureUrl; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
