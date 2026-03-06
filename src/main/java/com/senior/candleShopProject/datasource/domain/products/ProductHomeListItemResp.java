package com.senior.candleShopProject.datasource.domain.products;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductHomeListItemResp {
    private List<IProductHomeListItemResp> featuredProducts;
    private List<IProductHomeListItemResp> allProducts;
    private Integer totalProducts;
}
