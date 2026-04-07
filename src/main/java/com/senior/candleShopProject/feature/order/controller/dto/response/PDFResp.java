package com.senior.candleShopProject.feature.order.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.senior.candleShopProject.common.SupabaseService.Dto.SignedFileUrlResp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PDFResp {

    @JsonProperty("pdf_signed_url")
    private SignedFileUrlResp pdfSignedUrl;

    @JsonProperty("pdf_name")
    private String pdfName;

    @JsonProperty("expires_at")
    private String expiresAt;
}
