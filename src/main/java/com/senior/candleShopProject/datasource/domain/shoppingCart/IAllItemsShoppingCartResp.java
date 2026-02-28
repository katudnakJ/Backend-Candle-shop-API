package com.senior.candleShopProject.datasource.domain.shoppingCart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class IAllItemsShoppingCartResp {
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
