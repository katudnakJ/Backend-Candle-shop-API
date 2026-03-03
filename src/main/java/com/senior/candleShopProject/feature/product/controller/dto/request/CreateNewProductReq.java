package com.senior.candleShopProject.feature.product.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateNewProductReq {
    private String productName;
    private BigDecimal price;
    private int weight;
    private String description;
    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean isActive;
    private boolean isFeatured;
}
