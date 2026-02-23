package com.senior.candleShopProject.common.SupabaseService.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SignedUrlResp(
        @JsonProperty("signedUrl") String signedUrl,
        @JsonProperty("signedURL") String signedURL
) {
    public String getUrl() {
        return signedUrl != null ? signedUrl : signedURL;
    }
}
