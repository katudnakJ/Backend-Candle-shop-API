package com.senior.candleShopProject.feature.report.controller.dto.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MonthlyAOVSummaryDto {
    private BigDecimal aovThisMonth;
    private BigDecimal aovPercentageChange;
    private String aovTrend;
}
