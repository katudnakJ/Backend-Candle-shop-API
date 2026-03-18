package com.senior.candleShopProject.common;

import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ResultCode {
//    HTTP status code
    public static final Status SUCCESS = new Status(HttpStatus.OK, "Success");
    public static final Status CREATED = new Status(HttpStatus.CREATED, "Created");

    public static final Status INVALID_PARAMS = new Status(HttpStatus.BAD_REQUEST);
    public static final Status UNAUTHORIZED = new Status(HttpStatus.UNAUTHORIZED);
    public static final Status FORBIDDEN = new Status(HttpStatus.FORBIDDEN);
    public static final Status DATA_NOT_FOUND = new Status(HttpStatus.NOT_FOUND);
    public static final Status INTERNAL_SERVER_ERROR = new Status(HttpStatus.INTERNAL_SERVER_ERROR);
    public static final Status SYSTEM_ERROR = new Status(HttpStatus.INTERNAL_SERVER_ERROR);
    public static final Status CONFLICT = new Status(HttpStatus.CONFLICT);
    public static final Status BAD_REQUEST = new Status(HttpStatus.BAD_REQUEST);
    public static final Status FILE_TOO_LARGE = new Status(HttpStatus.CONTENT_TOO_LARGE);
    public static final Status NOT_FOUND = new Status(HttpStatus.NOT_FOUND);
}
