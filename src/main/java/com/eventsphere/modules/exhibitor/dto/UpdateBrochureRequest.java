package com.eventsphere.modules.exhibitor.dto;

public class UpdateBrochureRequest {

    private String brochureUrl;
    private String brochureName;

    public UpdateBrochureRequest() {}

    public UpdateBrochureRequest(String brochureUrl, String brochureName) {
        this.brochureUrl = brochureUrl;
        this.brochureName = brochureName;
    }

    public String getBrochureUrl() { return brochureUrl; }
    public void setBrochureUrl(String brochureUrl) { this.brochureUrl = brochureUrl; }

    public String getBrochureName() { return brochureName; }
    public void setBrochureName(String brochureName) { this.brochureName = brochureName; }
}
