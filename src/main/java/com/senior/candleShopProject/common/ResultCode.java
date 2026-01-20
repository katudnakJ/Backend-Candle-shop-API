package com.senior.candleShopProject.common;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ResultCode {
    private static final Status SUCCESS = new Status(HttpStatus.OK,"Success.");
    private static final Status INVALID_PARAMS = new Status(HttpStatus.BAD_REQUEST,"Invalid Parameters.");
    private static final Status UNAUTHORIZED = new Status(HttpStatus.UNAUTHORIZED,"Unauthorized.");
    private static final Status FORBIDDEN = new Status(HttpStatus.FORBIDDEN,"Forbidden");
    private static final Status DATA_NOT_FOUND = new Status(HttpStatus.NOT_FOUND,"Data Not Found");
    private static final Status INTERNAL_SERVER_ERROR = new Status(HttpStatus.INTERNAL_SERVER_ERROR,"General Error.");
}
