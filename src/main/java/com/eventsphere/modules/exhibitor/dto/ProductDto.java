package com.eventsphere.modules.exhibitor.dto;

import java.util.ArrayList;
import java.util.List;

public class ProductDto {

    private String id;
    private String name;
    private String tagline;
    private String company;
    private String category;
    private String booth;
    private String image;
    private int views;
    private int enquiries;
    private int interestedVisitorsCount;
    private String overview;
    private List<String> features = new ArrayList<>();
    private List<String> benefits = new ArrayList<>();
    private List<String> useCases = new ArrayList<>();
    private ProductResourcesDto resources = new ProductResourcesDto();

    public static class ProductResourcesDto {
        private String brochureUrl;
        private String videoUrl;
        private String websiteUrl;

        public ProductResourcesDto() {}

        public ProductResourcesDto(String brochureUrl, String videoUrl, String websiteUrl) {
            this.brochureUrl = brochureUrl;
            this.videoUrl = videoUrl;
            this.websiteUrl = websiteUrl;
        }

        public String getBrochureUrl() { return brochureUrl; }
        public void setBrochureUrl(String brochureUrl) { this.brochureUrl = brochureUrl; }

        public String getVideoUrl() { return videoUrl; }
        public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

        public String getWebsiteUrl() { return websiteUrl; }
        public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }
    }

    public ProductDto() {}

    public ProductDto(
            String id,
            String name,
            String tagline,
            String company,
            String category,
            String booth,
            String image,
            int views,
            int enquiries,
            int interestedVisitorsCount,
            String overview,
            List<String> features,
            List<String> benefits,
            List<String> useCases,
            ProductResourcesDto resources
    ) {
        this.id = id;
        this.name = name;
        this.tagline = tagline;
        this.company = company;
        this.category = category;
        this.booth = booth;
        this.image = image;
        this.views = views;
        this.enquiries = enquiries;
        this.interestedVisitorsCount = interestedVisitorsCount;
        this.overview = overview;
        this.features = features != null ? features : new ArrayList<>();
        this.benefits = benefits != null ? benefits : new ArrayList<>();
        this.useCases = useCases != null ? useCases : new ArrayList<>();
        this.resources = resources != null ? resources : new ProductResourcesDto();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBooth() { return booth; }
    public void setBooth(String booth) { this.booth = booth; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getViews() { return views; }
    public void setViews(int views) { this.views = views; }

    public int getEnquiries() { return enquiries; }
    public void setEnquiries(int enquiries) { this.enquiries = enquiries; }

    public int getInterestedVisitorsCount() { return interestedVisitorsCount; }
    public void setInterestedVisitorsCount(int interestedVisitorsCount) { this.interestedVisitorsCount = interestedVisitorsCount; }

    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public List<String> getBenefits() { return benefits; }
    public void setBenefits(List<String> benefits) { this.benefits = benefits; }

    public List<String> getUseCases() { return useCases; }
    public void setUseCases(List<String> useCases) { this.useCases = useCases; }

    public ProductResourcesDto getResources() { return resources; }
    public void setResources(ProductResourcesDto resources) { this.resources = resources; }
}
