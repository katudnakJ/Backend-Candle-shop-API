package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopForbiddenException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.utils.PaginationUtil;
import com.senior.candleShopProject.datasource.domain.shoppingCart.IAllItemsShoppingCartResp;
import com.senior.candleShopProject.datasource.entities.ShoppingCartItemsEntity;
import com.senior.candleShopProject.datasource.repo.ShoppingCartItemsRepo;
import com.senior.candleShopProject.datasource.repo.ShoppingCartRepo;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.request.AddShoppingCartItemReq;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.request.DeleteShoppingCartItemReq;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.response.ShoppingCartItemsList;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.response.ShoppingCartResp;
import com.senior.candleShopProject.feature.shoppingCart.service.ShoppingCartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

    @Mock
    private ShoppingCartRepo shoppingCartRepo;

    @Mock
    private ShoppingCartItemsRepo shoppingCartItemsRepo;

    @Mock
    private PaginationUtil paginationUtil;

    private ShoppingCartService shoppingCartService;

    @BeforeEach
    void setUp() {
        shoppingCartService = new ShoppingCartService(shoppingCartRepo, shoppingCartItemsRepo);
        ReflectionTestUtils.setField(shoppingCartService, "publicImageBaseUrl", "https://cdn.test/");
    }

    @Test
    void getShoppingCart_returnNullData_whenCartEmpty() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        int page = 0;
        int size = 10;

        when(shoppingCartRepo.getAllItemsFromShoppingCartByUserId(userId, size, page * size))
                .thenReturn(Collections.emptyList());

        GenericResponse response = shoppingCartService.getShoppingCart(userId, page, size);

        assertThat(response.getStatus()).isEqualTo(ResultCode.SUCCESS);
        assertThat(response.getData()).isNull();
    }

    @Test
    void getShoppingCart_mapDataCorrectly_whenCartHasItems() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        int page = 0;
        int size = 10;

        IAllItemsShoppingCartResp iShoppingCartResp = mock(IAllItemsShoppingCartResp.class);
        when(iShoppingCartResp.getShoppingCartId()).thenReturn(cartId);
        when(iShoppingCartResp.getShoppingCartItemId()).thenReturn(cartItemId);
        when(iShoppingCartResp.getProductId()).thenReturn(productId);
        when(iShoppingCartResp.getQuantity()).thenReturn(2);
        when(iShoppingCartResp.getProductName()).thenReturn("Candle A");
        when(iShoppingCartResp.getPrice()).thenReturn(new BigDecimal("199.00"));
        when(iShoppingCartResp.getProductSlug()).thenReturn("candle-a");
        when(iShoppingCartResp.getProductImgPath()).thenReturn("images/a.png");

        doReturn(List.of(iShoppingCartResp))
                .when(shoppingCartRepo).getAllItemsFromShoppingCartByUserId(eq(userId), anyInt(), anyInt());

        GenericResponse response = shoppingCartService.getShoppingCart(userId, page, size);

        assertThat(response.getStatus()).isEqualTo(ResultCode.SUCCESS);
        assertThat(response.getData()).isInstanceOf(ShoppingCartResp.class);

        ShoppingCartResp resp = (ShoppingCartResp) response.getData();
        assertThat(resp.getShoppingCartId()).isEqualTo(cartId);
        assertThat(resp.getCartItems()).hasSize(1);

        ShoppingCartItemsList item = resp.getCartItems().get(0);
        assertThat(item.getShoppingCartItemId()).isEqualTo(cartItemId);
        assertThat(item.getProductId()).isEqualTo(productId);
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getProductName()).isEqualTo("Candle A");
        assertThat(item.getPrice()).isEqualByComparingTo("199.00");
        assertThat(item.getProductSlug()).isEqualTo("candle-a");
        assertThat(item.getProductImgPath()).isEqualTo("https://cdn.test/images/a.png");
    }

    @Test
    void addShoppingCartItem_withExistCartItem_success() throws ShopServiceApiException {
        UUID shoppingCartId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID cartItemId = UUID.randomUUID();

        ShoppingCartItemsEntity shoppingCartItemsEntity = mock(ShoppingCartItemsEntity.class);
        when(shoppingCartItemsEntity.getShoppingCartItemId()).thenReturn(cartItemId);

        when(shoppingCartRepo.getShoppingCartIdByUserId(userId)).thenReturn(shoppingCartId);
        when(shoppingCartItemsRepo
                .findShoppingCartItemsEntitiesByShoppingCartEntity_ShoppingCartIdAndProductsEntity_ProductId(
                        shoppingCartId, productId))
                .thenReturn(Optional.of(shoppingCartItemsEntity));
        when(shoppingCartItemsRepo.save(shoppingCartItemsEntity)).thenReturn(shoppingCartItemsEntity);

        AddShoppingCartItemReq addShoppingCartItemReq = new AddShoppingCartItemReq();
        addShoppingCartItemReq.setProductId(productId.toString());
        addShoppingCartItemReq.setQuantity(5);

        GenericResponse response = shoppingCartService.addShoppingCartItem(userId, addShoppingCartItemReq);

        assertNotNull(response);
        assertThat(response.getStatus()).isEqualTo(ResultCode.SUCCESS);

        verify(shoppingCartRepo, times(1)).getShoppingCartIdByUserId(userId);
        verify(shoppingCartItemsRepo, times(1))
                .findShoppingCartItemsEntitiesByShoppingCartEntity_ShoppingCartIdAndProductsEntity_ProductId(
                        shoppingCartId, productId);
        verify(shoppingCartItemsRepo, times(1)).save(shoppingCartItemsEntity);
    }

    @Test
    void addShoppingCartItem_with_Not_ExistCartItem_success() throws ShopServiceApiException {
        UUID shoppingCartId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        ShoppingCartItemsEntity shoppingCartItemsEntity = mock(ShoppingCartItemsEntity.class);
        when(shoppingCartRepo.getShoppingCartIdByUserId(userId)).thenReturn(shoppingCartId);
        when(shoppingCartItemsRepo
                .findShoppingCartItemsEntitiesByShoppingCartEntity_ShoppingCartIdAndProductsEntity_ProductId
                        (shoppingCartId,productId))
                .thenReturn(Optional.empty());
        when(shoppingCartItemsRepo.save(any(ShoppingCartItemsEntity.class))).thenReturn(shoppingCartItemsEntity);

        AddShoppingCartItemReq addShoppingCartItemReq = new AddShoppingCartItemReq();
        addShoppingCartItemReq.setProductId(productId.toString());
        addShoppingCartItemReq.setQuantity(5);

        GenericResponse response = shoppingCartService.addShoppingCartItem(userId,addShoppingCartItemReq);

        assertNotNull(response);
        assertThat(response.getStatus()).isEqualTo(ResultCode.SUCCESS);

        verify(shoppingCartRepo, times(1)).getShoppingCartIdByUserId(userId);
        verify(shoppingCartItemsRepo, times(1))
                .findShoppingCartItemsEntitiesByShoppingCartEntity_ShoppingCartIdAndProductsEntity_ProductId
                        (shoppingCartId, productId);
        verify(shoppingCartItemsRepo, times(1)).save(any(ShoppingCartItemsEntity.class));
    }

    @Test
    void addShoppingCartItem_cartId_isNull_shouldThrow() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();

        when(shoppingCartRepo.getShoppingCartIdByUserId(userId)).thenReturn(null);

        AddShoppingCartItemReq addShoppingCartItemReq = new AddShoppingCartItemReq();
        addShoppingCartItemReq.setProductId(UUID.randomUUID().toString());
        addShoppingCartItemReq.setQuantity(1);

        ShopDataNotFoundException exception = assertThrows(ShopDataNotFoundException.class, ()-> {
           shoppingCartService.addShoppingCartItem(userId,addShoppingCartItemReq);
        });

        assertEquals(ResultCode.DATA_NOT_FOUND, exception.getStatus());
            verify(shoppingCartRepo, times(1)).getShoppingCartIdByUserId(any(UUID.class));
    }

    @Test
    void deleteShoppingCartItem_success() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID shoppingCartId = UUID.randomUUID();
        UUID shoppingCartItemId = UUID.randomUUID();

        DeleteShoppingCartItemReq req = new DeleteShoppingCartItemReq(
                shoppingCartId.toString(),shoppingCartItemId.toString()
        );
        req.setShoppingCartId(shoppingCartId.toString());
        req.setShoppingCartItemId(shoppingCartItemId.toString());

        when(shoppingCartRepo.getShoppingCartIdByUserId(userId)).thenReturn(shoppingCartId);
        when(shoppingCartItemsRepo.existsByShoppingCartItemId(shoppingCartItemId)).thenReturn(true);

        GenericResponse response = shoppingCartService.deleteShoppingCartItem(userId, req);

        assertNotNull(response);
        assertThat(response.getStatus()).isEqualTo(ResultCode.SUCCESS);

        verify(shoppingCartRepo, times(1)).getShoppingCartIdByUserId(userId);
        verify(shoppingCartItemsRepo, times(1)).existsByShoppingCartItemId(shoppingCartItemId);
        verify(shoppingCartItemsRepo, times(1)).deleteById(shoppingCartItemId);
        verifyNoMoreInteractions(shoppingCartRepo, shoppingCartItemsRepo);
    }

    @Test
    void deleteShoppingCartItem_itemNotExist_shouldThrow() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID shoppingCartId = UUID.randomUUID();
        UUID shoppingCartItemId = UUID.randomUUID();

        DeleteShoppingCartItemReq req = new DeleteShoppingCartItemReq(
                shoppingCartId.toString(), shoppingCartItemId.toString()
        );

        when(shoppingCartItemsRepo.existsByShoppingCartItemId(shoppingCartItemId)).thenReturn(false);

        ShopDataNotFoundException ex = assertThrows(
                ShopDataNotFoundException.class,
                () -> shoppingCartService.deleteShoppingCartItem(userId, req)
        );

        assertEquals(ResultCode.DATA_NOT_FOUND, ex.getStatus());
        verify(shoppingCartItemsRepo, times(1)).existsByShoppingCartItemId(shoppingCartItemId);

        verify(shoppingCartRepo, times(1)).getShoppingCartIdByUserId(userId);
        verifyNoMoreInteractions(shoppingCartRepo, shoppingCartItemsRepo);
    }

    @Test
    void deleteShoppingCartItem_cartIdIsNull_shouldThrow() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID shoppingCartItemId = UUID.randomUUID();
        UUID shoppingCartId = UUID.randomUUID();

        DeleteShoppingCartItemReq req = new DeleteShoppingCartItemReq(
                shoppingCartId.toString(),shoppingCartItemId.toString()
        );
        req.setShoppingCartId(UUID.randomUUID().toString());
        req.setShoppingCartItemId(shoppingCartItemId.toString());

        when(shoppingCartItemsRepo.existsByShoppingCartItemId(shoppingCartItemId)).thenReturn(true);
        when(shoppingCartRepo.getShoppingCartIdByUserId(userId)).thenReturn(null);

        ShopDataNotFoundException ex = assertThrows(
                ShopDataNotFoundException.class,
                () -> shoppingCartService.deleteShoppingCartItem(userId, req)
        );

        assertEquals(ResultCode.DATA_NOT_FOUND, ex.getStatus());
        verify(shoppingCartItemsRepo, times(1)).existsByShoppingCartItemId(shoppingCartItemId);
        verify(shoppingCartRepo, times(1)).getShoppingCartIdByUserId(userId);
        verifyNoMoreInteractions(shoppingCartRepo, shoppingCartItemsRepo);
    }

    @Test
    void deleteShoppingCartItem_cartIdNotMatch_shouldThrowForbidden() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID realShoppingCartId = UUID.randomUUID();
        UUID otherCartId = UUID.randomUUID();
        UUID shoppingCartItemId = UUID.randomUUID();
        UUID shoppingCartId = UUID.randomUUID();

        DeleteShoppingCartItemReq req = new DeleteShoppingCartItemReq(
                shoppingCartId.toString(),shoppingCartItemId.toString()
        );
        req.setShoppingCartId(otherCartId.toString());
        req.setShoppingCartItemId(shoppingCartItemId.toString());

        when(shoppingCartItemsRepo.existsByShoppingCartItemId(shoppingCartItemId)).thenReturn(true);
        when(shoppingCartRepo.getShoppingCartIdByUserId(userId)).thenReturn(realShoppingCartId);

        ShopForbiddenException ex = assertThrows(
                ShopForbiddenException.class,
                () -> shoppingCartService.deleteShoppingCartItem(userId, req)
        );

        assertEquals(ResultCode.FORBIDDEN, ex.getStatus());
        verify(shoppingCartItemsRepo, times(1)).existsByShoppingCartItemId(shoppingCartItemId);
        verify(shoppingCartRepo, times(1)).getShoppingCartIdByUserId(userId);
        verifyNoMoreInteractions(shoppingCartRepo, shoppingCartItemsRepo);
    }

}
