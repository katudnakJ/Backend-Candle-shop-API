package com.senior.candleShopProject.datasource.domain;

import java.util.UUID;

public interface IProductImagesResp {
    UUID getProductImgId();
    String getProductImgSlug();
    Boolean getIsPrimary();
}
