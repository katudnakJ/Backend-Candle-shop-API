package com.senior.candleShopProject.common.SupabaseService.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class SignedFileUrlResp {

    @JsonProperty("signed_file_url")
    private String signedFileUrl;

    @JsonProperty("expires_at")
    private ZonedDateTime expiresAt;
}
