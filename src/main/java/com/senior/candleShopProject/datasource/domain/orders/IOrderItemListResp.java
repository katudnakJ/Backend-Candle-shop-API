package com.senior.candleShopProject.datasource.domain.orders;

import java.math.BigDecimal;
import java.util.UUID;

public interface IOrderItemListResp {
    UUID getOrderId();
    UUID getOrderItemId();
    String getProductName();
    Integer getQuantity();
    BigDecimal getPricePerUnit();
    BigDecimal getSubTotal();
    String getProductImgPath();
}
