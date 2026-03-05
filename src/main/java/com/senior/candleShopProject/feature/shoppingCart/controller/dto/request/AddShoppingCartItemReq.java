package com.senior.candleShopProject.feature.shoppingCart.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddShoppingCartItemReq {

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("quantity")
    private Integer quantity;
}
