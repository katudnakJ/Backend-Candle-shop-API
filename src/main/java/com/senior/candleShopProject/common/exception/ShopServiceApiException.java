package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;
import com.senior.candleShopProject.common.utils.Constants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopServiceApiException extends Exception {
    private Status status;

    public ShopServiceApiException(Status status)  {
        this.status = status;
    }

    public ShopServiceApiException(Status status, String remark) {
        super(remark);
        status.setRemark(remark);
        this.status = status;
    }
}
