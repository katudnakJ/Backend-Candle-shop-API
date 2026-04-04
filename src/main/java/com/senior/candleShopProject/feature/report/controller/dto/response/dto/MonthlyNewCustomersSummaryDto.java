package com.senior.candleShopProject.feature.report.controller.dto.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MonthlyNewCustomersSummaryDto {
    private Long totalNewCustomersThisMonth;
    private BigDecimal newCustomersPercentageChange;
    private String newCustomerTrend;
}
