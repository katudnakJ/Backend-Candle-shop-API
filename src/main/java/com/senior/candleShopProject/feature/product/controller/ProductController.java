package com.senior.candleShopProject.feature.product.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopInvalidParamException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.product.service.ShopProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@Tag(name = "Candle Shop Service API.")
@RequestMapping("v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final ShopProductService shopProductService;

    @GetMapping("/details/{productId}")
    @Operation(summary = "Get product details API.")
    public ResponseEntity<GenericResponse> getProductDetailsById(@RequestHeader(name = "x-access-token") String xAccessToken,
                                                                 @PathVariable(name = "productId") String productId) throws ShopServiceApiException {
        log.info("Get product details by product id {}", productId);
        if(xAccessToken == null || xAccessToken.isEmpty())
            throw new ShopInvalidParamException(ResultCode.UNAUTHORIZED,"Access token is Invalid or Expired.");
        if(productId == null || productId.length() != 36)
            throw new ShopInvalidParamException(ResultCode.DATA_NOT_FOUND,"product ID not found.");

        GenericResponse response = new GenericResponse();
        UUID productUUID = UUID.fromString(productId);

        response = shopProductService.getProductsById(productUUID);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
