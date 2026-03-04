package com.senior.candleShopProject.common.exception;

import com.senior.candleShopProject.common.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopBadRequestRuntimeExc extends RuntimeException {
    private Status status;
    private String remark;
    public ShopBadRequestRuntimeExc(Status status) {
        this.status = status;
    }
    public ShopBadRequestRuntimeExc(Status status, String remark) {
        this.status = status;
        this.remark = remark;
    }

}
