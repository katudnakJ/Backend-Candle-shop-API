package com.senior.candleShopProject.datasource.domain.orders;

import java.util.UUID;

public interface IReportTopSellingProductsResp {
    UUID getProductId();
    String getProductName();
    Long getTotalQuantitySales();
}
