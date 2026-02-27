package com.senior.candleShopProject.feature.shoppingCart.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.exception.ShopUnAuthorizedException;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.request.AddShoppingCartItemReq;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.request.DeleteShoppingCartItemReq;
import com.senior.candleShopProject.feature.shoppingCart.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Candle Shop Shopping cart API.")
@RequestMapping("v1/cart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @GetMapping()
    @Operation(summary = "Get shopping cart API.", description = "Get shopping cart with all items.")
    public ResponseEntity getShoppingCart(@RequestAttribute("userId") String userId) throws ShopServiceApiException {
        log.info("Get shopping cart by user id {}", userId);

        if (userId == null)
            throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "User id is missing.");

        UUID userUUID = UUID.fromString(userId);
        GenericResponse response = shoppingCartService.getShoppingCart(userUUID);
        return ResponseEntity.ok(response);
    }

    @PostMapping()
    @Operation(summary = "Add shopping cart item API.", description = "Add items list to shopping cart.")
    public ResponseEntity addShoppingCartItem(@RequestAttribute("userId") String userId,
                                                     @RequestBody AddShoppingCartItemReq addShoppingCartItemReq) throws ShopServiceApiException {
        log.info("Add shopping cart item by user id.");
        UUID userUUID = UUID.fromString(userId);

        GenericResponse response = shoppingCartService.addShoppingCartItem(userUUID, addShoppingCartItemReq);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping()
    @Operation(summary = "Delete shopping cart item API.", description = "Delete items list from shopping cart.")
    public ResponseEntity deleteShoppingCartItem(@RequestAttribute("userId") String userId,
                                                 @RequestBody DeleteShoppingCartItemReq deleteShoppingCartItemReq) throws ShopServiceApiException {
        log.info("Delete shopping cart item by user id.");
        UUID userUUID = UUID.fromString(userId);

        GenericResponse response = shoppingCartService.deleteShoppingCartItem(userUUID, deleteShoppingCartItemReq);

        return ResponseEntity.ok(response);
    }
}
