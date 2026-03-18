package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;

public class ShopUploadSizeExceededException extends ShopServiceApiException {
    public ShopUploadSizeExceededException(Status statusCode) {
        super(statusCode);
    }

    public ShopUploadSizeExceededException(Status statusCode, String message) {
        super(statusCode,null, message);
    }

    public ShopUploadSizeExceededException(Status statusCode, String message, String remark) {
        super(statusCode, message, remark);
    }
}
