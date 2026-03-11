package com.senior.candleShopProject.feature.order.controller.dto.response;

import com.senior.candleShopProject.common.SupabaseService.Dto.SignedFileUrlResp;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PDFResp {
    private SignedFileUrlResp pdfSignedUrl;
    private String pdfName;
}
