package com.senior.candleShopProject.datasource.domain.products;

import java.util.UUID;

public interface IProductImagesResp {
    UUID getProductImgId();
    String getProductImgPath();
   Boolean getIsPrimary();
}
