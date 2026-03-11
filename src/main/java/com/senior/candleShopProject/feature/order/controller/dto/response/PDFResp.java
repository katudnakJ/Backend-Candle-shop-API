package com.senior.candleShopProject.feature.order.controller.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PDFResp {
    byte[] pdf;
    String pdfName;
}
