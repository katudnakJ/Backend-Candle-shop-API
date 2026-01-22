package com.senior.candleShopProject.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
public class ResultCode {
    public static final Status SUCCESS = new Status(HttpStatus.OK,"Success.");
    public static final Status INVALID_PARAMS = new Status(HttpStatus.BAD_REQUEST,"Invalid Parameters.");
    public static final Status UNAUTHORIZED = new Status(HttpStatus.UNAUTHORIZED,"Unauthorized.");
    public static final Status FORBIDDEN = new Status(HttpStatus.FORBIDDEN,"Forbidden");
    public static final Status DATA_NOT_FOUND = new Status(HttpStatus.NOT_FOUND,"Data Not Found");
    public static final Status INTERNAL_SERVER_ERROR = new Status(HttpStatus.INTERNAL_SERVER_ERROR,"General Error.");
    public static final Status SYSTEM_ERROR = new Status(HttpStatus.INTERNAL_SERVER_ERROR,"System Error.");
}
