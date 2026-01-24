package com.senior.candleShopProject.datasource.domain;

import java.math.BigDecimal;
import java.util.UUID;

public interface IProductResp {
    UUID getProductId();
    String getProductName();
    BigDecimal getPrice();
    Integer getWeight();
    String getDescription();
    String getSlug();
    String getIsActive();
    String getIsFeatured();
}