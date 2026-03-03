package com.senior.candleShopProject.feature.product.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class UpdateProductReq {

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "blue candle")
    private String productName;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private BigDecimal price;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private double weight;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "true", defaultValue = "true")
    private boolean active =true;

    @Schema(example = "false", defaultValue = "false")
    private boolean featured = false;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> reUploadImageIds;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> deleteImageIds;
}
