package com.senior.candleShopProject.datasource.domain.products;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductManagementResp {
    private List<IProductHomeListItemResp> allProducts;
    private int page;
    private int size;
    private int startAt;
    private int endAt;
    private Long totalProducts;
    private boolean hasNext;
}
