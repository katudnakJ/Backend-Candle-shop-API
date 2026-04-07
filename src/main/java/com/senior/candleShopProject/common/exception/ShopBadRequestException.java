package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;

public class ShopBadRequestException extends ShopServiceApiException {
    public ShopBadRequestException(Status statusCode) {
        super(statusCode);
    }

    public ShopBadRequestException(Status statusCode, String remark) {
        super(statusCode, null, remark);
    }

    public ShopBadRequestException(Status statusCode, String message, String remark) {
        super(statusCode, message, remark);
    }
}
