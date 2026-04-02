package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopBadRequestException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.domain.orders.IOrdersReportDataResp;
import com.senior.candleShopProject.datasource.domain.orders.ReportOfRangeLastMonthResp;
import com.senior.candleShopProject.datasource.domain.orders.ReportOrderOfRangeResp;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.feature.order.generator.Excel.ExcelGenerators;
import com.senior.candleShopProject.feature.report.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestComponent
public class ReportServiceTest {
    @InjectMocks
    private ReportService reportService;

    @Mock
    private OrdersRepo ordersRepo;

    @Mock
    private ExcelGenerators excelGenerators;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getMonthlyReport_Success() throws ShopServiceApiException {
        int month = 3;
        int year = 2026;
        String userRole = "ADMIN";

        when(ordersRepo.findReportByRange(any(), any())).thenReturn(new ReportOrderOfRangeResp(BigDecimal.valueOf(1000),10L,2L,3L,5L));
        when(ordersRepo.findReportByRangeLastMonth(any(), any())).thenReturn(new ReportOfRangeLastMonthResp(BigDecimal.valueOf(500),5L));
        when(ordersRepo.countNewCustomerThisMonth(any(), any())).thenReturn(1L);
        when(ordersRepo.findTopSellingProductsByRange(any(), any(), anyInt())).thenReturn(Collections.emptyList());

        var resp = reportService.getMonthlyReport(userRole, month, year);

        assertNotNull(resp);
        assertEquals(ResultCode.SUCCESS, resp.getStatus());
    }

    @Test
    void getOrderReportByRangeAndFormat_Success() throws Exception {
        int month = 2;
        int year = 2023;
        String format = "excel";

        IOrdersReportDataResp row = mock(IOrdersReportDataResp.class);
        when(ordersRepo.findOrdersReportDataByRange(any(), any())).thenReturn(List.of(row));

        doAnswer(inv -> {
            ByteArrayOutputStream baos = inv.getArgument(0);
            baos.write(new byte[]{1,2,3});
            return null;
        }).when(excelGenerators).generateSaleReport(any(ByteArrayOutputStream.class), anyList(), anyString(), anyString());

        ResponseEntity<?> resp = reportService.getOrderReportByRangeAndFormat(format, month, year);

        assertNotNull(resp);
    }

    @Test
    void testGetOrderReportByRangeAndFormat_UnsupportedFormat_Throws() {
        int month = 2;
        int year = 2023;
        String format = "pdf";

        assertThrows(ShopBadRequestException.class, () ->
                reportService.getOrderReportByRangeAndFormat(format, month, year)
        );
    }
}