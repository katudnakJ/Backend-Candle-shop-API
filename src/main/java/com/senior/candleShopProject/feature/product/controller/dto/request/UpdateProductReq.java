package com.senior.candleShopProject.feature.product.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class UpdateProductReq {

    @Schema(example = "blue candle")
    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("weight")
    private Double weight;

    @JsonProperty("description")
    private String description;

    @Schema(example = "true", defaultValue = "true")
    @JsonProperty("active")
    private Boolean active =true;

    @Schema(example = "false", defaultValue = "false")
    @JsonProperty("featured")
    private Boolean featured = false;

    @Schema(defaultValue = "null")
    @JsonProperty("re_upload_image_ids")
    private List<String> reUploadImageIds = null;

    @Schema(example = "0", defaultValue = "null")
    @JsonProperty("primary_index")
    private Integer primaryIndex = null;

    @Schema(defaultValue = "null")
    @JsonProperty("delete_image_ids")
    private List<String> deleteImageIds = null;
}
