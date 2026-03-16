package com.senior.candleShopProject.datasource.domain.products;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class ProductByOrderIdResp {
    private UUID productId;
    private String productName;
    private BigDecimal price;
    private double weight;
    private String description;
    private String slug;
    private boolean isActive;
    private boolean isFeatured;
    private Instant productCreatedDate;
    private Instant productUpdatedDate;
    private Integer totalSold;
    private int itemSoldQuantity;
}
