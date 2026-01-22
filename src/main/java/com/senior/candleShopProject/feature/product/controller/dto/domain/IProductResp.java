package com.senior.candleShopProject.feature.product.controller.dto.domain;

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