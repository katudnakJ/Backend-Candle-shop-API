package com.senior.candleShopProject.feature.shoppingCart.controller.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddShoppingCartItemReq {
    private String productId;
    private Integer quantity;
}
