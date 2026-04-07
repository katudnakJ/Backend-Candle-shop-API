package com.senior.candleShopProject.feature.product.controller.dto.response;

import com.senior.candleShopProject.datasource.domain.products.IProductHomeListItemResp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductsBySearchResp {
    List<IProductHomeListItemResp> products;
    private int page;
    private int size;
    private int startAt;
    private int endAt;
    private Long totalProducts;
    private boolean hasNext;
}
