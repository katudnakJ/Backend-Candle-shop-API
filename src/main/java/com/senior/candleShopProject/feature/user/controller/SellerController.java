package com.senior.candleShopProject.feature.user.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.utils.ImageValidationUtils;
import com.senior.candleShopProject.common.utils.JwtUtils;
import com.senior.candleShopProject.feature.user.service.SellerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@Tag(name = "Seller API.")
@RequestMapping("v1/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerService sellerService;


    @GetMapping("/orders/count")
    @Operation(summary = "Get order count by status API.", description = "Using for seller's home page.")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity getSellerOrderCount(@RequestParam(value = "status", required = true) String status) throws ShopServiceApiException {
        log.info("Get seller order count by status {}", status);
        GenericResponse response = sellerService.getSellerOrderCountByStatus(status);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/qr-payment")
    @Operation(summary = "Get QR code payment image API.", description = "Get QR code payment image when seller click the button.")
    @PreAuthorize("hasRole('CUST') or hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity getQrCodePayment(@RequestAttribute("userId") String userId) throws ShopServiceApiException{
        log.info("Get QR code payment");
        UUID userUuid = UUID.fromString(userId);

        GenericResponse response = sellerService.getQrCodePayment(userUuid);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(value = "/qr-payment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Add new QR code payment image API.")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity addQrCodePayment(@RequestAttribute("userId") String userId,
                                            @RequestAttribute("isOwner") boolean isOwner,
                                            @RequestPart("imageData") MultipartFile imageData ) throws ShopServiceApiException, IOException {
        log.info("Add QR code payment");
        ImageValidationUtils.validateImage(imageData);
        UUID userUuid = UUID.fromString(userId);

        GenericResponse response = sellerService.addQrCodePayment(userUuid, imageData, isOwner);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping(value = "/qr-payment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update QR code payment API.")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity syncQrCodePayment(@RequestAttribute("userId") String userId,
                                            @RequestAttribute("isOwner") boolean isOwner,
                                            @RequestPart("imageData") MultipartFile imageData ) throws ShopServiceApiException, IOException {
        log.info("Sync QR code payment");
        ImageValidationUtils.validateImage(imageData);
        UUID userUuid = UUID.fromString(userId);

        GenericResponse response = sellerService.syncQrCodePayment(userUuid, imageData, isOwner);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
