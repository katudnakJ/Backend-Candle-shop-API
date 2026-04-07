package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;

public class ShopConflictException extends ShopServiceApiException {
    public ShopConflictException(Status statusCode) {
        super(statusCode);
    }

    public ShopConflictException(Status statusCode, String remark) {
        super(statusCode, null, remark);
    }

    public ShopConflictException(Status statusCode, String message, String remark) {
        super(statusCode, message, remark);
    }
}
