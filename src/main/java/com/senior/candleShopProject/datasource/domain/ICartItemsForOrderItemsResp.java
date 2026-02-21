package com.senior.candleShopProject.datasource.domain;

import java.math.BigDecimal;
import java.util.UUID;

public interface ICartItemsForOrderItemsResp {
    int getQuantity(); // from shopping_cart_items table (per type of product)
    UUID getProductId(); // from products table
    String getProductName(); //from products table
    BigDecimal getPricePerUnit(); //from products table
}
