package com.senior.candleShopProject.feature.report.controller.dto.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MonthlyTotalOrdersSummaryDto {
    private Long totalOrdersThisMonth;
    private BigDecimal percentageChangeTotalOrders;
    private String totalOrdersTrend;
    private Long totalTSOrders;
    private Long totalTROrders;
    private Long totalCPOrders;
}
