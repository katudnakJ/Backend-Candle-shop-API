package com.senior.candleShopProject.feature.report.controller.dto.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MonthlySalesSummaryDto {
    private BigDecimal totalSalesThisMonth;
    private BigDecimal percentageChangeTotalSales;
    private String totalSalesTrend;
}
