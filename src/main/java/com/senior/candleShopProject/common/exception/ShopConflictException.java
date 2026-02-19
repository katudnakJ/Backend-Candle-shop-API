package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;

public class ShopConflictException extends ShopServiceApiException {
    public ShopConflictException(Status status) {
        super(status);
    }

    public ShopConflictException(Status status, String remark) {
        super(status, remark);
    }
}
