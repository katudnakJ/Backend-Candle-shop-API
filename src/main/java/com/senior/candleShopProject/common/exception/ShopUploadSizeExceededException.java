package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;

public class ShopUploadSizeExceededException extends ShopServiceApiException {
    public ShopUploadSizeExceededException(Status status) {
        super(status);
    }

    public ShopUploadSizeExceededException(Status status, String remark) {
        super(status, remark);
    }
}
