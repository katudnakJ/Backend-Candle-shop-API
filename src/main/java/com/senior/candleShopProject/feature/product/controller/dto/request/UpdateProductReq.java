package com.senior.candleShopProject.feature.product.controller.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class UpdateProductReq {

    @Schema(example = "blue candle")
    private String productName;

    private BigDecimal price;

    private Double weight;

    private String description;

    @Schema(example = "true", defaultValue = "true")
    private Boolean active =true;

    @Schema(example = "false", defaultValue = "false")
    private Boolean featured = false;

    @Schema(defaultValue = "null")
    private List<String> reUploadImageIds = null;

    @Schema(example = "0", defaultValue = "null")
    private Integer primaryIndex = null;

    @Schema(defaultValue = "null")
    private List<String> deleteImageIds = null;
}
