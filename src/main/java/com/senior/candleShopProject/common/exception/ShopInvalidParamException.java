package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopInvalidParamException extends ShopServiceApiException {

    public ShopInvalidParamException(Status statusCode) {
        super(statusCode);
    }

    public ShopInvalidParamException(Status statusCode, String remark) {
        super(statusCode, null,remark);
    }

    public ShopInvalidParamException(Status statusCode, String message, String remark) {
        super(statusCode, message, remark);
    }
}
