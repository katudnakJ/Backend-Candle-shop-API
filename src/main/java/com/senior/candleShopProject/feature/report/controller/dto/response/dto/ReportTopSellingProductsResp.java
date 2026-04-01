package com.senior.candleShopProject.feature.report.controller.dto.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReportTopSellingProductsResp {
    private UUID productId;
    private String productName;
    private Long totalQuantitySales;
}
