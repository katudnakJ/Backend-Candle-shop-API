package com.senior.candleShopProject.common.handler;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopInvalidParamException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.exception.ShopUnAuthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleGeneralException(Exception ex) {
        log.error("General Exception : ",ex.getMessage());
        ex.printStackTrace();
        GenericResponse response = new GenericResponse();
        response.setStatus(ResultCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ShopServiceApiException.class)
    public ResponseEntity<GenericResponse> handleShopServiceApiException(ShopServiceApiException ex) {
        log.error("Service Api Exception : ",ex.getMessage());
        ex.printStackTrace();
        GenericResponse response = new GenericResponse();
        response.setStatus(ResultCode.SYSTEM_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ShopInvalidParamException.class)
    public ResponseEntity<GenericResponse> handleShopInvalidParamException(ShopInvalidParamException ex) {
        log.error("Invalid Param Exception : ",ex.getStatus().getRemark());
        GenericResponse response = new GenericResponse();
        response.setStatus(ex.getStatus());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ShopUnAuthorizedException.class)
    public ResponseEntity<GenericResponse> handleShopUnAuthorizedException(ShopUnAuthorizedException ex) {
        log.error("Unauthorized Exception : ",ex.getStatus().getRemark());
        GenericResponse response = new GenericResponse();
        response.setStatus(ex.getStatus());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(ShopDataNotFoundException.class)
    public ResponseEntity<GenericResponse> handleShopDataNotFoundException(ShopDataNotFoundException ex) {
        log.error("Data not found Exception : ",ex.getStatus().getRemark());
        GenericResponse response = new GenericResponse();
        response.setStatus(ex.getStatus());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }


}
