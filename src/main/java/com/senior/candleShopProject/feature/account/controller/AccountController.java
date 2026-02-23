package com.senior.candleShopProject.feature.account.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.exception.ShopUnAuthorizedException;
import com.senior.candleShopProject.feature.account.controller.dto.request.AddUserAddressReq;
import com.senior.candleShopProject.feature.account.controller.dto.request.SyncUserAddressReq;
import com.senior.candleShopProject.feature.account.service.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Candle Shop Service API.")
@RequestMapping("v1/account")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/address")
    public ResponseEntity<GenericResponse> getUserAddresses(@RequestAttribute("userId") String userId) throws ShopServiceApiException {
        log.info("Get user address by user id {}", userId);

        UUID userUUID = UUID.fromString(userId);

        GenericResponse response = accountService.getUserAddresses(userUUID);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/address/{addressId}")
    public ResponseEntity<GenericResponse> getUserAddressesByAddressId(@RequestAttribute("userId") String userId,
                                                                       @PathVariable("addressId") String addressId) throws ShopServiceApiException {
        log.info("Get user address by user address id {}", userId);

        UUID userUUID = UUID.fromString(userId);
        UUID addressUUID = UUID.fromString(addressId);

        GenericResponse response = accountService.getUserAddressByAddressId(userUUID, addressUUID);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/address")
    public ResponseEntity<GenericResponse> addUserAddress(@RequestAttribute("userId") String userId,
                                                          @RequestBody AddUserAddressReq addUserAddressReq) throws ShopServiceApiException {
        log.info("Add user address by user id {}", userId);

        UUID userUUID = UUID.fromString(userId);

        GenericResponse response = accountService.addUserAddress(userUUID, addUserAddressReq);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/address")
    public ResponseEntity<GenericResponse> syncUserAddress(@RequestAttribute("userId") String userId,
                                                          @RequestBody SyncUserAddressReq syncUserAddressReq) throws ShopServiceApiException {
        log.info("Sync user address by user id {}", userId);

        UUID userUUID = UUID.fromString(userId);

        GenericResponse response = accountService.syncUserAddress(userUUID, syncUserAddressReq);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping  ("/address/{addressId}")
    public ResponseEntity<GenericResponse> deleteUserAddress(@RequestAttribute("userId") String userId,
                                                           @PathVariable("addressId") String addressId) throws ShopServiceApiException {
        log.info("Delete user address by user id {}", userId);

        UUID userUUID = UUID.fromString(userId);
        UUID addressUUID = UUID.fromString(addressId);

        GenericResponse response = accountService.deleteAddress(userUUID, addressUUID);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
