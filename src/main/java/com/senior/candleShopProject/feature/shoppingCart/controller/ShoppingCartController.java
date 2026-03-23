package com.senior.candleShopProject.feature.shoppingCart.controller;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.request.AddShoppingCartItemReq;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.request.DeleteShoppingCartItemReq;
import com.senior.candleShopProject.feature.shoppingCart.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Shopping cart API.")
@RequestMapping("v1/cart")
public class ShoppingCartController {

    private final ShoppingCartService shoppingCartService;

    @GetMapping()
    @Operation(summary = "Get shopping cart with items API.", description = "Get shopping cart with all items.")
    @PreAuthorize("hasRole('CUST') or hasRole('ADMIN')")
    public ResponseEntity getShoppingCart(@RequestAttribute("userId") String userId,
                                          @RequestParam(value = "page", defaultValue = "0") int page,
                                          @RequestParam(value = "size", defaultValue = "10") int size) throws ShopServiceApiException {
        log.info("Get shopping cart by user id {}", userId);

        UUID userUUID = UUID.fromString(userId);
        GenericResponse response = shoppingCartService.getShoppingCart(userUUID, page, size );
        return ResponseEntity.ok(response);
    }

    @PostMapping()
    @Operation(summary = "Add shopping cart item API.", description = "Add items list to shopping cart.")
    @PreAuthorize("hasRole('CUST') or hasRole('ADMIN')")
    public ResponseEntity addShoppingCartItem(@RequestAttribute("userId") String userId,
                                                     @RequestBody AddShoppingCartItemReq addShoppingCartItemReq) throws ShopServiceApiException {
        log.info("Add shopping cart item by user id.");
        UUID userUUID = UUID.fromString(userId);

        GenericResponse response = shoppingCartService.addShoppingCartItem(userUUID, addShoppingCartItemReq);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping()
    @Operation(summary = "Delete shopping cart item API.", description = "Delete items list from shopping cart.")
    @PreAuthorize("hasRole('CUST') or hasRole('ADMIN')")
    public ResponseEntity deleteShoppingCartItem(@RequestAttribute("userId") String userId,
                                                 @RequestBody DeleteShoppingCartItemReq deleteShoppingCartItemReq) throws ShopServiceApiException {
        log.info("Delete shopping cart item by user id.");
        UUID userUUID = UUID.fromString(userId);

        GenericResponse response = shoppingCartService.deleteShoppingCartItem(userUUID, deleteShoppingCartItemReq);

        return ResponseEntity.ok(response);
    }
}
