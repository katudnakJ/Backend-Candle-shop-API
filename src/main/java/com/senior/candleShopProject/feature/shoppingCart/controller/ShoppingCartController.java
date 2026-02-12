package com.senior.candleShopProject.feature.shoppingCart.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.exception.ShopUnAuthorizedException;
import com.senior.candleShopProject.feature.shoppingCart.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Candle Shop Shopping cart API.")
@RequestMapping("v1/cart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @GetMapping()
    public ResponseEntity getShoppingCart(@RequestAttribute("userId") String userId) throws ShopServiceApiException {
        log.info("Get shopping cart by user id {}", userId);

        if (userId == null)
            throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "User id is missing.");

        UUID userUUID = UUID.fromString(userId);
        GenericResponse response = shoppingCartService.getShoppingCart(userUUID);
        return ResponseEntity.ok(response);
    }
}
