package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;

public class ShopDataNotFoundException extends ShopServiceApiException {

    public ShopDataNotFoundException(Status status) {
        super(status);
    }

    public ShopDataNotFoundException(Status status, String remark) {
        super(status, remark);
    }
}
