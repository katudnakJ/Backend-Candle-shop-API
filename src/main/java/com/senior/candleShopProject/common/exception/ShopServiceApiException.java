package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopServiceApiException extends Exception {

    private final Status statusCode;
    private final String remark;

    public ShopServiceApiException(Status statusCode) {
        super(statusCode.getMessage());
        this.statusCode = statusCode;
        this.remark = statusCode.getRemark();
    }

    public ShopServiceApiException(Status templateStatus, String message, String remark) {
        super(message);
        this.statusCode = new Status(
                templateStatus.getStatusCode(),
                message,
                remark
        );
        this.remark = remark;
    }

}
