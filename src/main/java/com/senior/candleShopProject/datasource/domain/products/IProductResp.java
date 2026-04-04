package com.senior.candleShopProject.datasource.domain.products;

import java.math.BigDecimal;
import java.util.UUID;

public interface IProductResp {
    UUID getProductId();
    String getProductName();
    BigDecimal getPrice();
    double getWeight();
    String getDescription();
    String getSlug();
    boolean getIsActive();
    boolean getIsFeatured();
    Integer getTotalSold();
}