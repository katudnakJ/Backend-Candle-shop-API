package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;

public class ShopForbiddenException extends ShopServiceApiException {
    public ShopForbiddenException(Status statusCode) {
        super(statusCode);
    }

    public ShopForbiddenException(Status statusCode, String remark) {
        super(statusCode, null,remark);
    }

    public ShopForbiddenException(Status statusCode, String message, String remark) {
        super(statusCode, message, remark);
    }
}
