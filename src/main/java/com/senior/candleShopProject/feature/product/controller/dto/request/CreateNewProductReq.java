package com.senior.candleShopProject.feature.product.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CreateNewProductReq {

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("weight")
    private double weight;

    @JsonProperty("description")
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED,
            example = "true", defaultValue = "true")
    @JsonProperty("active")
    private boolean active =true;

    @Schema(example = "false", defaultValue = "false", required = false)
    @JsonProperty("featured")
    private boolean featured = false;
}
