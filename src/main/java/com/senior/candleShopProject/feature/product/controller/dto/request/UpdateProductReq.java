package com.senior.candleShopProject.feature.product.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductReq {

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Candle")
        @JsonProperty("product_name")
        @NotNull
        @NotBlank
        private String productName;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "19.99")
        @JsonProperty("price")
        @NotNull
        @Positive
        private BigDecimal price;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "0.5")
        @JsonProperty("weight")
        @NotNull
        @Positive
        private double weight;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "A scented candle with a relaxing aroma.")
        @JsonProperty("description")
        private String description;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                example = "true", defaultValue = "true")
        @JsonProperty("active")
        @NotNull
        private boolean active =true;

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "false", defaultValue = "false")
        @JsonProperty("featured")
        @NotNull
        private boolean featured = false;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @JsonProperty("primary_index")
        private Integer primaryIndex = null;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @JsonProperty("exist_into_primary")
        private String existIntoPrimary = null;

        @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @JsonProperty("delete_image_ids")
        private List<String> deleteImageIds = new ArrayList<>();
}


