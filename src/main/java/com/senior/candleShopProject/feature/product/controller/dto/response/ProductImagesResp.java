package com.senior.candleShopProject.feature.product.controller.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ProductImagesResp {
    private UUID productImgId;
    private String productImgPath;
    private Boolean isPrimary;
}
