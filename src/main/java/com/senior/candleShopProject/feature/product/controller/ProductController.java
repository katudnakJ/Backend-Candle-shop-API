package com.senior.candleShopProject.feature.product.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.product.service.ProductService;
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
@RequestMapping("v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/details/{productId}")
    @Operation(summary = "Get product details API.")
    public ResponseEntity<GenericResponse> getProductDetailsById(@PathVariable(name = "productId") String productId) throws ShopServiceApiException {
        log.info("Get product details by product id {}", productId);
        UUID productUUID = UUID.fromString(productId);

        GenericResponse response = productService.getProductsById(productUUID);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping()
    @Operation(summary = "Get product home list API.")
    public ResponseEntity<GenericResponse> getProductHomeList() throws ShopServiceApiException {
        log.info("Get product home list");
        GenericResponse response = productService.getProductHomeListItem();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
