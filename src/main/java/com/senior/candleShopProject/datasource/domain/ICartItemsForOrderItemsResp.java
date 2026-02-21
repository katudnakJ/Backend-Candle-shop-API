package com.senior.candleShopProject.datasource.domain;

import java.math.BigDecimal;
import java.util.UUID;

public interface ICartItemsForOrderItemsResp {
    int getQuantity();
    UUID getProductId();
    String getProductName();
    BigDecimal getPrice();
}
