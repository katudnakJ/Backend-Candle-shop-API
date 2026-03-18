package com.senior.candleShopProject.common;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class Status {
    private HttpStatus statusCode;
    private String message;
    private String remark;

    public Status(HttpStatus statusCode) {
        this.statusCode = statusCode;
    }

    public Status(HttpStatus statusCode, String remark) {
        this.statusCode = statusCode;
        this.remark = remark;
    }

    public Status(HttpStatus statusCode, String message, String remark) {
        this.statusCode = statusCode;
        this.message = message;
        this.remark = remark;
    }

    public Status withMessage(String message) {
        return new Status(this.statusCode, message, this.remark);
    }

    public Status withRemark(String remark) {
        return new Status(this.statusCode, this.message, remark);
    }

    public Status withMessageAndRemark(String message, String remark) {
        return new Status(this.statusCode, message, remark);
    }
}
