package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopUnAuthorizedException extends ShopServiceApiException {
    public ShopUnAuthorizedException(Status statusCode) {
        super(statusCode);
    }

    public ShopUnAuthorizedException(Status statusCode, String message) {
        super(statusCode, null, message);
    }

    public ShopUnAuthorizedException(Status statusCode, String message, String remark) {
        super(statusCode, message, remark);
    }

}
