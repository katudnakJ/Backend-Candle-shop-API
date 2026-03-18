package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;

public class ShopDataNotFoundException extends ShopServiceApiException {

    public ShopDataNotFoundException(Status statusCode) {
        super(statusCode);
    }

    public ShopDataNotFoundException(Status statusCode, String remark) {
        super(statusCode, null, remark);
    }

    public ShopDataNotFoundException(Status statusCode, String message, String remark) {
        super(statusCode, message, remark);
    }
}
