package com.senior.candleShopProject.feature.shoppingCart.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class DeleteShoppingCartItemReq {

    @JsonProperty("shopping_cart_id")
    private String shoppingCartId;

    @JsonProperty("shopping_cart_item_id")
    private String shoppingCartItemId;
}
