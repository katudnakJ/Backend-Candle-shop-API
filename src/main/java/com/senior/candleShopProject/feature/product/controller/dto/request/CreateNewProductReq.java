package com.senior.candleShopProject.feature.product.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateNewProductReq {
    private String productName;
    private BigDecimal price;
    private double weight;
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "true", defaultValue = "true")
    private boolean active =true;

    @Schema(example = "false", defaultValue = "false")
    private boolean featured = false;
}
