package com.senior.candleShopProject.feature.product.controller.dto.response.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ProductDetailDto {
    private UUID productId;
    private String productName;
    private String description;
    private double weight;
    private BigDecimal price;
    private String slug;
    private boolean isActive;
    private boolean isFeatured;
    private Integer totalSold;
}
