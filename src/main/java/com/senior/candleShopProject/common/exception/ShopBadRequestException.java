package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;

public class ShopBadRequestException extends ShopServiceApiException {
    public ShopBadRequestException(Status status) {
        super(status);
    }

    public ShopBadRequestException(Status status, String remark) {
        super(status, remark);
    }
}
