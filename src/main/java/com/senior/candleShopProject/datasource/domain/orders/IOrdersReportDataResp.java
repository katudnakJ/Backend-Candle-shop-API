package com.senior.candleShopProject.datasource.domain.orders;

import java.math.BigDecimal;

public interface IOrdersReportDataResp {
    String getProductName();
    BigDecimal getPricePerUnit();
    Long getTotalSales();
    BigDecimal getSubtotal();
}
