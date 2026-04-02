package com.senior.candleShopProject.feature.report.controller.dto;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Report Service API.")
@RequestMapping("v1/reports")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get report data for dashboard", description = "Get report data for making dashboard")
    @PreAuthorize(value = "hasAnyRole('DEVELOPER', 'SELLER')")
    public ResponseEntity<GenericResponse> getDashboardReportData(
                                                  @RequestParam("month") int month,
                                                  @RequestParam("year") int year) throws ShopServiceApiException {

        log.info("Received request to get dashboard report data for month: {}, year: {}", month, year);
        GenericResponse response = reportService.getMonthlyReport( month, year);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
