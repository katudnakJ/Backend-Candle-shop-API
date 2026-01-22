package com.senior.candleShopProject.feature.product.controller.dto.domain;

import java.util.UUID;

public interface IProductImagesResp {
    UUID getProductImgId();
    String getProductImgSlug();
    Boolean getIsPrimary();
}
