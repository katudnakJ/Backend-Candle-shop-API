package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopInvalidParamException extends ShopServiceApiException {

    public ShopInvalidParamException(Status status) {
        super(status);
    }

    public ShopInvalidParamException(Status status, String remark) {
        super(status, remark);
    }
}
