package com.senior.candleShopProject.common.SupabaseService.Dto;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class SignedFileUrlResp {
    private String signedFileUrl;
    private ZonedDateTime expiresAt;
}
