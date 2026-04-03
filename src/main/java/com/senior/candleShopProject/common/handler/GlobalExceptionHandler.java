package com.senior.candleShopProject.common.handler;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GenericResponse> handleGeneralException(Exception ex) {
        log.error("General Exception : {}",ex.getMessage(), ex);
        ex.printStackTrace();
        GenericResponse response = new GenericResponse();
        response.setStatus(ResultCode.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<GenericResponse> handleMissingRequestHeader(MissingRequestHeaderException ex) {
        log.error("Missing Request Header : {}", ex.getHeaderName(), ex);
        GenericResponse response = new GenericResponse();
        response.setStatus(ResultCode.UNAUTHORIZED);
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ShopServiceApiException.class)
    public ResponseEntity<GenericResponse> handleShopServiceApiException(ShopServiceApiException ex) {
        log.error("Service Api Exception : {} {}",ex.getStatusCode(), ex.getRemark(), ex);
        ex.printStackTrace();
        GenericResponse response = new GenericResponse();
        response.setStatus(ResultCode.SYSTEM_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ShopInvalidParamException.class)
    public ResponseEntity<GenericResponse> handleShopInvalidParamException(ShopInvalidParamException ex) {
        log.error("Invalid Param Exception : {} {}",ex.getStatusCode().getRemark(), ex.getRemark(), ex);
        GenericResponse response = new GenericResponse();
        response.setStatus(ex.getStatusCode());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ShopForbiddenException.class)
    public ResponseEntity<GenericResponse> handleShopForbiddenException(ShopForbiddenException ex) {
        log.error("Forbidden Exception : {} {}",ex.getStatusCode().getRemark(), ex.getRemark(), ex);
        GenericResponse response = new GenericResponse();
        response.setStatus(ex.getStatusCode());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ShopUnAuthorizedException.class)
    public ResponseEntity<GenericResponse> handleShopUnAuthorizedException(ShopUnAuthorizedException ex) {
        log.error("Unauthorized Exception : {} {}",ex.getStatusCode().getRemark(), ex.getRemark(), ex);
        GenericResponse response = new GenericResponse();
        response.setStatus(ex.getStatusCode());
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }


    @ExceptionHandler(ShopDataNotFoundException.class)
    public ResponseEntity<GenericResponse> handleShopDataNotFoundException(ShopDataNotFoundException ex) {
        log.error("Data not found Exception : {} {}",ex.getStatusCode().getRemark(), ex.getRemark(), ex);
        GenericResponse response = new GenericResponse();
        response.setStatus(ex.getStatusCode());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public  ResponseEntity<GenericResponse> handleMissingServletRequestParameterException(MissingServletRequestParameterException ex) {
        log.error("Missing Servlet Request Parameter Exception : {}",ex.getMessage(), ex);
        GenericResponse response = new GenericResponse();
        response.setStatus(ResultCode.INVALID_PARAMS);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ShopBadRequestException.class)
    public ResponseEntity<GenericResponse> handleShopBadRequestException(ShopBadRequestException ex) {
        log.error("Bad Request Exception : {}", ex.getRemark());
        GenericResponse response = new GenericResponse();
        response.setStatus(ex.getStatusCode());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ShopConflictException.class)
    public ResponseEntity<GenericResponse> handleShopConflictException(ShopConflictException ex) {
        log.error("Conflict Exception : {}", ex.getRemark(), ex);
        GenericResponse response = new GenericResponse();
        response.setStatus(ex.getStatusCode());
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<GenericResponse> handleShopUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error("Max Upload Size Exceeded Exception : {}", ex.getMessage(), ex);
        GenericResponse response = new GenericResponse();
        response.setStatus(ResultCode.FILE_TOO_LARGE);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.error("Http Message Not Readable Exception : ", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setStatus(ResultCode.INVALID_PARAMS);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<GenericResponse> handleNoHandlerFoundException(NoHandlerFoundException ex) {
        log.error("No Handler Found Exception : ", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setStatus(ResultCode.NOT_FOUND);
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("Method Argument Not Valid Exception : ", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setStatus(ResultCode.INVALID_PARAMS);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<GenericResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.error("HTTP Request Method Not Supported Exception : ", ex.getMessage());
        GenericResponse response = new GenericResponse();
        response.setStatus(ResultCode.BAD_REQUEST);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ShopBadRequestRuntimeExc.class)
    public ResponseEntity<GenericResponse> handleShopBadRequestRuntimeExc(ShopBadRequestRuntimeExc ex) {
        log.error("Bad Request Runtime Exception : {}", ex.getRemark(), ex);
        GenericResponse response = new GenericResponse();
        response.setStatus(ex.getStatusCode());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
    public ResponseEntity<?> handleAuthDenied() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "status", ResultCode.UNAUTHORIZED,
                "message", "คุณไม่มีสิทธิ์ในการเข้าถึง"
                ,"remark", "User does not have permission to access this resource"
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal Argument Exception : {}", ex.getMessage(), ex);
        GenericResponse response = new GenericResponse();
        response.setStatus(ResultCode.INVALID_PARAMS);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
