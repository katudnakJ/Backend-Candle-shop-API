package com.senior.candleShopProject.feature.product.controller.dto.response;

import com.senior.candleShopProject.feature.product.controller.dto.response.dto.ProductDetailDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductDetailResp {
    private ProductDetailDto product;
    private List<ProductImagesResp> productImages;
}
