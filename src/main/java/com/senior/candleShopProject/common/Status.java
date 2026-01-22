package com.senior.candleShopProject.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Setter
@Getter
public class Status {
    private HttpStatus status;
    private String message;
    private String remark;

    public Status(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public Status(HttpStatus status, String message, String remark) {
        this.status = status;
        this.message = message;
        this.remark = remark;
    }
}
