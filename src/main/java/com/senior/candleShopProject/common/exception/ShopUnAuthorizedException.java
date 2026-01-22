package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopUnAuthorizedException extends ShopServiceApiException {
    public ShopUnAuthorizedException(Status status) {
        super(status);
    }

    public ShopUnAuthorizedException(Status status, String remark) {
        super(status, remark);
    }

}
