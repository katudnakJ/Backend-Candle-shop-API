package com.senior.candleShopProject.feature.product.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @NotNull
    @NotBlank
    private String productName;

    @JsonProperty("price")
    @NotNull
    @Positive
    private BigDecimal price;

    @JsonProperty("weight")
    @NotNull
    @Positive
    private double weight;

    @JsonProperty("description")
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
            example = "true", defaultValue = "true")
    @JsonProperty("active")
    @NotNull
    private boolean active =true;

    @Schema(example = "false", defaultValue = "false")
    @JsonProperty("featured")
    @NotNull
    private boolean featured = false;
}
