package com.senior.candleShopProject.feature.product.controller.dto.response;

import com.senior.candleShopProject.datasource.domain.IProductResp;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductDetailResp {
    private IProductResp product;
    private List<ProductImagesResp> productImages;
}
