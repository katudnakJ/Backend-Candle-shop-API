package com.senior.candleShopProject.feature.report.controller.dto.response;

import com.senior.candleShopProject.feature.report.controller.dto.response.dto.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class MonthlyReportResp {
    private MonthlySalesSummaryDto monthlySalesSummary;
    private MonthlyTotalOrdersSummaryDto monthlyTotalOrdersSummary;
    private MonthlyAOVSummaryDto monthlyAOVSummary;
    private MonthlyNewCustomersSummaryDto monthlyNewCustomersSummary;
    List<ReportTopSellingProductsResp> topSellingProductsThisMonth;
}
