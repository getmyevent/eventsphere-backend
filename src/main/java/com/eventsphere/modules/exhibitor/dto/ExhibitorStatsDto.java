package com.eventsphere.modules.exhibitor.dto;

public class ExhibitorStatsDto {

    private String id;
    private String company;
    private String booth;
    private String category;
    private String logoUrl;
    private int teamSize;
    private String status;
    private String brochureUrl;
    private String brochureName;
    private long totalLeads;
    private long newLeads;
    private long interestedLeads;
    private long followUpLeads;
    private long totalProducts;
    private long totalViews;
    private long totalEnquiries;

    public ExhibitorStatsDto() {}

    public ExhibitorStatsDto(
            String id,
            String company,
            String booth,
            String category,
            String logoUrl,
            int teamSize,
            String status,
            String brochureUrl,
            String brochureName,
            long totalLeads,
            long newLeads,
            long interestedLeads,
            long followUpLeads,
            long totalProducts,
            long totalViews,
            long totalEnquiries
    ) {
        this.id = id;
        this.company = company;
        this.booth = booth;
        this.category = category;
        this.logoUrl = logoUrl;
        this.teamSize = teamSize;
        this.status = status;
        this.brochureUrl = brochureUrl;
        this.brochureName = brochureName;
        this.totalLeads = totalLeads;
        this.newLeads = newLeads;
        this.interestedLeads = interestedLeads;
        this.followUpLeads = followUpLeads;
        this.totalProducts = totalProducts;
        this.totalViews = totalViews;
        this.totalEnquiries = totalEnquiries;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public long getTotalLeads() { return totalLeads; }
    public void setTotalLeads(long totalLeads) { this.totalLeads = totalLeads; }

    public long getNewLeads() { return newLeads; }
    public void setNewLeads(long newLeads) { this.newLeads = newLeads; }

    public long getInterestedLeads() { return interestedLeads; }
    public void setInterestedLeads(long interestedLeads) { this.interestedLeads = interestedLeads; }

    public long getFollowUpLeads() { return followUpLeads; }
    public void setFollowUpLeads(long followUpLeads) { this.followUpLeads = followUpLeads; }

    public long getTotalProducts() { return totalProducts; }
    public void setTotalProducts(long totalProducts) { this.totalProducts = totalProducts; }

    public long getTotalViews() { return totalViews; }
    public void setTotalViews(long totalViews) { this.totalViews = totalViews; }

    public long getTotalEnquiries() { return totalEnquiries; }
    public void setTotalEnquiries(long totalEnquiries) { this.totalEnquiries = totalEnquiries; }
}
