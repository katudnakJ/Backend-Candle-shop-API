package com.senior.candleShopProject.feature.report.controller.dto.response.dto;

import com.senior.candleShopProject.datasource.domain.orders.IReportTopSellingProductsResp;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class MonthlyReportResp {
    private BigDecimal totalSalesThisMonth;
    private Long totalOrdersThisMonth;
    private BigDecimal percentageChangeOrder;
    private String orderTrend;

    private BigDecimal aovThisMonth;
    private BigDecimal percentageChangeAOV;
    private String aovTrend;

    private Long totalNewCustomersThisMonth;
    private BigDecimal percentageNewCustomersThisMonth;
    private String newCustomerTrend;

    List<ReportTopSellingProductsResp> topSellingProductsThisMonth;
}
