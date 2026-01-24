package com.senior.candleShopProject.datasource.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductHomeListItemResp {
    private List<IProductHomeListItemResp> featuredProduct;
    private Integer featuredTotal;
    private List<IProductHomeListItemResp> nonFeaturedProduct;
    private Integer nonFeaturedTotal;
}
