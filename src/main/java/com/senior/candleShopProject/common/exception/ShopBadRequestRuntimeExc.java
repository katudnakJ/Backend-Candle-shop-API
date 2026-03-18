package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopBadRequestRuntimeExc extends RuntimeException {
    private Status statusCode;
    private String message;
    private String remark;

    public ShopBadRequestRuntimeExc(Status statusCode) {
        this.statusCode = statusCode;
    }
    public ShopBadRequestRuntimeExc(Status statusCode, String remark) {
        this.statusCode = statusCode;
        this.remark = remark;
    }

    public ShopBadRequestRuntimeExc(Status statusCode, String message, String remark) {
        this.statusCode = statusCode;
        this.message = message;
        this.remark = remark;
    }

}
