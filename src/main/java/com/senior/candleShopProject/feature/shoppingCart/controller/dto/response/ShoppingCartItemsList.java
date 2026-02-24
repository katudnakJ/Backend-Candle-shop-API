package com.senior.candleShopProject.feature.shoppingCart.controller.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class ShoppingCartItemsList {
    private UUID shoppingCartItemId;
    private UUID productId;
    private Integer quantity;
    private String productName;
    private BigDecimal price;
    private String productSlug;
    private String productImgPath;
}
