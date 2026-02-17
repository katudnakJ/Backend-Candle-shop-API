package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;

public class ShopForbiddenException extends ShopServiceApiException {
    public ShopForbiddenException(Status status) {
        super(status);
    }

    public ShopForbiddenException(Status status, String remark) {
        super(status, remark);
    }
}
