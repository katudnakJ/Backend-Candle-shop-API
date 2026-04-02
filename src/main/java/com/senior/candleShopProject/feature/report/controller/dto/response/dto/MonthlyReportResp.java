package com.senior.candleShopProject.feature.report.controller.dto.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class MonthlyReportResp {
    private BigDecimal totalSalesThisMonth;
    private Long totalOrdersThisMonth;
    private Long totalTSOrders;
    private Long totalTROrders;
    private Long totalCPOrders;
    private BigDecimal ordersPercentageChange;
    private String orderTrend;

    private BigDecimal aovThisMonth;
    private BigDecimal aovPercentageChange;
    private String aovTrend;

    private Long totalNewCustomersThisMonth;
    private BigDecimal newCustomersPercentageChange;
    private String newCustomerTrend;

    List<ReportTopSellingProductsResp> topSellingProductsThisMonth;
}
