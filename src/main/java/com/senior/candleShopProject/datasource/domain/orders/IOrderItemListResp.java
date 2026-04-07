package com.senior.candleShopProject.datasource.domain.orders;

import java.math.BigDecimal;
import java.util.UUID;

public interface IOrderItemListResp {
    UUID getOrderId();
    UUID getOrderItemId();
    UUID getProductId();
    String getProductName();
    Integer getQuantity();
    BigDecimal getPricePerUnit();
    BigDecimal getSubTotal();
    String getProductImagePath();
}
