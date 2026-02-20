package com.senior.candleShopProject.common.SupabaseService.Dto;

import lombok.Data;
import lombok.Getter;

import java.time.Instant;
import java.time.ZonedDateTime;

@Data
public class SignedImageUrlResp {
    private String signedImageUrl;
    private ZonedDateTime expiresAt;
}
