package com.senior.candleShopProject.datasource.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class IShoppingCartResp {
    UUID shoppingCartId;
    UUID shoppingCartItemId;
    UUID productId;
    Integer quantity;
    String productName;
    BigDecimal price;
    Integer weight;
    String description;
    String productSlug;
    String productImgPath;
}
