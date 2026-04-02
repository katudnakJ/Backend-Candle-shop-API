package com.senior.candleShopProject.datasource.domain.orders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportOfRangeLastMonthResp {
    private BigDecimal totalSales;
    private Long totalOrderCount;
}
