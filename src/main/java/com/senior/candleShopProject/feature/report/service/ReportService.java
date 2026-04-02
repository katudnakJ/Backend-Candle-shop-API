package com.senior.candleShopProject.feature.report.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopBadRequestException;
import com.senior.candleShopProject.common.exception.ShopForbiddenException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.ReportUtils;
import com.senior.candleShopProject.common.utils.dto.RangeOfMonthResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrdersReportDataResp;
import com.senior.candleShopProject.datasource.domain.orders.IReportTopSellingProductsResp;
import com.senior.candleShopProject.datasource.domain.orders.ReportOfRangeLastMonthResp;
import com.senior.candleShopProject.datasource.domain.orders.ReportOrderOfRangeResp;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.feature.order.generator.Excel.ExcelGenerators;
import com.senior.candleShopProject.feature.report.controller.dto.response.dto.MonthlyReportResp;
import com.senior.candleShopProject.feature.report.controller.dto.response.dto.ReportTopSellingProductsResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.ErrorResponse;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final OrdersRepo ordersRepo;
    private final ExcelGenerators excelGenerators;

    @Cacheable(
            value = "monthlyReportCache",
            key = "T(String).format('%s-%02d-%d', #userRole, #month, #year)",
            condition = "#month >= 1 && #month <= 12 && #year > 2000 && #month != T(java.time.LocalDate).now().getMonthValue()"
    )
    @Transactional(readOnly = true)
    public GenericResponse getMonthlyReport(int month, int year) throws ShopServiceApiException {

//        Total Sales for this month
        RangeOfMonthResp rangeOfMonthResp = ReportUtils.getRangeOfMonthUTC(month, year);
        ReportOrderOfRangeResp reportByRangeThisMonth = ordersRepo.findReportByRange(
                rangeOfMonthResp.getStartDate(),
                rangeOfMonthResp.getEndDate()
        );

        if ( reportByRangeThisMonth.getTotalSales() == null )
            reportByRangeThisMonth.setTotalSales(BigDecimal.ZERO);

//        Total Sales last month
        RangeOfMonthResp rangeOfLastMonthResp = ReportUtils.getRangeOfMonthUTC(month-1, year);
        ReportOfRangeLastMonthResp reportByRangeLastMonth = ordersRepo.findReportByRangeLastMonth(
                rangeOfLastMonthResp.getStartDate(),
                rangeOfLastMonthResp.getEndDate()
        );

        if ( reportByRangeThisMonth.getTotalSales() == null )
            reportByRangeLastMonth.setTotalSales(BigDecimal.ZERO);

        BigDecimal percentageChangeOrder;
        if ( reportByRangeLastMonth.getTotalSales().compareTo(BigDecimal.ZERO) == 0 ) {
            percentageChangeOrder = reportByRangeThisMonth.getTotalSales().compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100);
        } else {
            percentageChangeOrder = (reportByRangeThisMonth.getTotalSales().subtract(reportByRangeLastMonth.getTotalSales())
                            .divide(reportByRangeLastMonth.getTotalSales(), 4, RoundingMode.HALF_UP))
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal totalAOVThisMonth = reportByRangeThisMonth.getTotalOrderCount() > 0 ?
                reportByRangeThisMonth.getTotalSales()
                        .divide(BigDecimal.valueOf(reportByRangeThisMonth.getTotalOrderCount()), 4, RoundingMode.HALF_UP)
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal totalAOVLastMonth = reportByRangeLastMonth.getTotalOrderCount() > 0 ?
                reportByRangeLastMonth.getTotalSales()
                        .divide(BigDecimal.valueOf(reportByRangeLastMonth.getTotalOrderCount()), 4, RoundingMode.HALF_UP)
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal percentageChangeAOV =
                totalAOVLastMonth.compareTo(BigDecimal.ZERO) == 0 ?
                    (totalAOVThisMonth.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100))
                    : (totalAOVThisMonth.subtract(totalAOVLastMonth))
                        .divide(totalAOVLastMonth, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

        Long totalNewCustomerThisMonth = ordersRepo.countNewCustomerThisMonth(
                rangeOfMonthResp.getStartDate(),
                rangeOfMonthResp.getEndDate()
        );
        Long totalNewCustomerLastMonth = ordersRepo.countNewCustomerThisMonth(
                rangeOfLastMonthResp.getStartDate(),
                rangeOfLastMonthResp.getEndDate()
        );
            BigDecimal percentageChangeNewCustomer =
                    totalNewCustomerLastMonth == 0 ?
                            (totalNewCustomerThisMonth == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100))
                            : (BigDecimal.valueOf(totalNewCustomerThisMonth).subtract(BigDecimal.valueOf(totalNewCustomerLastMonth)))
                            .divide(BigDecimal.valueOf(totalNewCustomerLastMonth), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);

        List<IReportTopSellingProductsResp> iTopSellingProductsThisMonth = ordersRepo.findTopSellingProductsByRange(
                rangeOfMonthResp.getStartDate(),
                rangeOfMonthResp.getEndDate(),
                5
        );
        List<ReportTopSellingProductsResp> topSellingProductsThisMonth = new ArrayList<>();
        for (IReportTopSellingProductsResp iTopSellingProduct : iTopSellingProductsThisMonth) {
            ReportTopSellingProductsResp productInfo = new ReportTopSellingProductsResp();
            productInfo.setProductId(iTopSellingProduct.getProductId());
            productInfo.setProductName(iTopSellingProduct.getProductName());
            productInfo.setTotalQuantitySales(iTopSellingProduct.getTotalQuantitySales());

            topSellingProductsThisMonth.add(productInfo);
        }


        MonthlyReportResp monthlyReportResp = new MonthlyReportResp();
        monthlyReportResp.setTotalSalesThisMonth(reportByRangeThisMonth.getTotalSales());
        monthlyReportResp.setTotalOrdersThisMonth(reportByRangeThisMonth.getTotalOrderCount());
        monthlyReportResp.setTotalTSOrders(reportByRangeThisMonth.getTotalTSOrders());
        monthlyReportResp.setTotalTROrders(reportByRangeThisMonth.getTotalTROrders());
        monthlyReportResp.setTotalCPOrders(reportByRangeThisMonth.getTotalCPOrders());
        monthlyReportResp.setOrdersPercentageChange(percentageChangeOrder);
        monthlyReportResp.setOrderTrend(
                determineTrend(percentageChangeOrder)
        );

        monthlyReportResp.setAovThisMonth(totalAOVThisMonth);
        monthlyReportResp.setAovPercentageChange(percentageChangeAOV);
        monthlyReportResp.setAovTrend(
                determineTrend(percentageChangeAOV)
        );

        monthlyReportResp.setTotalNewCustomersThisMonth(totalNewCustomerThisMonth);
        monthlyReportResp.setNewCustomersPercentageChange(percentageChangeNewCustomer);
        monthlyReportResp.setNewCustomerTrend(
                determineTrend(percentageChangeNewCustomer)
        );

        monthlyReportResp.setTopSellingProductsThisMonth(topSellingProductsThisMonth);

        GenericResponse response = new GenericResponse();
        response.setData(monthlyReportResp);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    public ResponseEntity<?> getOrderReportByRangeAndFormat(String format, int month, int year) throws ShopServiceApiException {

        if ( !format.equalsIgnoreCase(Constants.REPORT_GENERATION_FORMAT_EXCEL) )
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Unsupported report generation format. Supported format");

        try {
            RangeOfMonthResp rangeOfMonthResp = ReportUtils.getRangeOfMonthUTC(month, year);
            List<IOrdersReportDataResp> ordersReportData = ordersRepo.findOrdersReportDataByRange(
                    rangeOfMonthResp.getStartDate(),
                    rangeOfMonthResp.getEndDate()
            );

            String monthInThai = ReportUtils.getMonthNameInThai(month);
            String monthInEnglish = ReportUtils.getMonthNameInEnglish(month);
            String yearInThai = ReportUtils.getYearInThai(year);
            String fileName = Constants.REPORT_PREFIX_FILE_NAME + monthInEnglish + "_" + year + ".xlsx";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            excelGenerators.generateSaleReport(
                    baos,
                    ordersReportData,
                    monthInThai,
                    yearInThai
            );

            byte[] fileBytes = baos.toByteArray();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(Constants.EXCEL_XLSX_TYPE))
                    .contentLength(fileBytes.length)
                    .body(fileBytes);
        }catch (Exception e) {
            log.error("Error generating report", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "code", "REPORT_GENERATION_FAILED",
                            "message", "ไม่สามารถสร้างรายงานได้"
                    ));
        }
    }

    private String determineTrend(BigDecimal percentageChange) {
        if (percentageChange.compareTo(BigDecimal.ZERO) > 0) {
            return Constants.TREND_INCREASE;
        } else if (percentageChange.compareTo(BigDecimal.ZERO) < 0) {
            return Constants.TREND_DECREASE;
        } else {
            return Constants.TREND_STABLE;
        }
    }
}
