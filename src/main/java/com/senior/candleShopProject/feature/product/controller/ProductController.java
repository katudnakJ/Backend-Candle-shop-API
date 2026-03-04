package com.senior.candleShopProject.feature.product.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.product.controller.dto.request.CreateNewProductReq;
import com.senior.candleShopProject.feature.product.controller.dto.request.UpdateProductReq;
import com.senior.candleShopProject.feature.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@Tag(name = "Products Service API.")
@RequestMapping("v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping("/details/{productId}")
    @Operation(summary = "Get product details API.", description = "Get product details by product id.")
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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create new product API.", description = "สร้างสินค้าใหม่โดยผู้ขาย")
    public ResponseEntity<GenericResponse> createNewProduct(@RequestAttribute("userId") String userId,

                                                            @ParameterObject
                                                            @ModelAttribute CreateNewProductReq createNewProductReq,

                                                            @RequestPart("imagesData") List<MultipartFile> imagesReqList,
                                                            @RequestParam("primaryIndex") Integer primaryIndex
                                                            ) throws ShopServiceApiException, IOException {
        log.info("Create new product");
        UUID userUUID = UUID.fromString(userId);

        GenericResponse response = productService.createNewProduct(userUUID, createNewProductReq, imagesReqList,primaryIndex);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(value = "/{productId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update product API.", description = "อัพเดตข้อมูลสินค้าโดยผู้ขาย")
    public ResponseEntity<GenericResponse> updateProduct(@RequestAttribute("userId") String userId,
                                                            @PathVariable(name = "productId") String productId,

                                                            @ParameterObject
                                                             @ModelAttribute CreateNewProductReq updateProductReq,

                                                            @RequestPart(value = "images",required = false) List<MultipartFile> imagesReqList,

                                                         @RequestParam("primaryIndex") Integer primaryIndex
    ) throws ShopServiceApiException, IOException {
        log.info("Update product with id {}", productId);
        UUID userUUID = UUID.fromString(userId);
        UUID productUUID = UUID.fromString(productId);

        GenericResponse response = productService.updateProduct(userUUID, productUUID, updateProductReq, imagesReqList,primaryIndex);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete product API.", description = "ลบสินค้าโดยผู้ขาย")
    public ResponseEntity<GenericResponse> deleteProduct(@RequestAttribute("userId") String userId,
                                                            @PathVariable(name = "productId") String productId) throws ShopServiceApiException {
        log.info("Delete product with id {}", productId);
        UUID userUUID = UUID.fromString(userId);
        UUID productUUID = UUID.fromString(productId);

        GenericResponse response = productService.deleteProduct(userUUID, productUUID);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
