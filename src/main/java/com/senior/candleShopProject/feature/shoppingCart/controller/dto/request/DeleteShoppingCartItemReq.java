package com.senior.candleShopProject.feature.shoppingCart.controller.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class DeleteShoppingCartItemReq {
    private String shoppingCartId;
    private String shoppingCartItemId;
}
