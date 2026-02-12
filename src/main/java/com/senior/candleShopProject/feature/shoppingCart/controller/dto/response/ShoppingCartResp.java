package com.senior.candleShopProject.feature.shoppingCart.controller.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class ShoppingCartResp {
    private UUID shoppingCartId;
    private List<ShoppingCartItemsList> items = new ArrayList<>();
}
