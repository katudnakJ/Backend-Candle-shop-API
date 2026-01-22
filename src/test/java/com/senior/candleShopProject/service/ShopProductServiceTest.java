package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.repo.ProductImagesRepo;
import com.senior.candleShopProject.datasource.repo.ProductsRepo;
import com.senior.candleShopProject.feature.product.controller.dto.domain.IProductImagesResp;
import com.senior.candleShopProject.feature.product.controller.dto.domain.IProductResp;
import com.senior.candleShopProject.feature.product.controller.dto.response.ProductDetailResp;
import com.senior.candleShopProject.feature.product.service.ShopProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.TestComponent;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@TestComponent
public class ShopProductServiceTest {

    @Mock
    private ShopProductService shopProductService;

    @Mock
    private ProductsRepo productsRepo;

    @Mock
    private ProductImagesRepo productImagesRepo;

    @BeforeEach
    void initTests() {MockitoAnnotations.openMocks(this);
        shopProductService = new ShopProductService(productsRepo, productImagesRepo);
    }

    @Test
    void testGetProductsById_Success() throws ShopServiceApiException {
       UUID productId = UUID.randomUUID();
       UUID productImgId = UUID.randomUUID();

       IProductResp productResp = mock(IProductResp.class);
       when(productsRepo.getProductById(productId)).thenReturn(productResp);

       IProductImagesResp iProductImagesResp = mock(IProductImagesResp.class);
       when(iProductImagesResp.getProductImgId()).thenReturn(productImgId);
       when(iProductImagesResp.getProductImgSlug()).thenReturn("image.jpg");
       when(iProductImagesResp.getIsPrimary()).thenReturn(true);

       when(productImagesRepo.getProductImagesByProductId(productId)).thenReturn(List.of(iProductImagesResp));


       GenericResponse response = shopProductService.getProductsById(productId);

       assertNotNull(response);
       assertEquals(response.getStatus(), ResultCode.SUCCESS);

       verify(productsRepo, times(1)).getProductById(productId);
       verify(productImagesRepo, times(1)).getProductImagesByProductId(productId);
    }

    @Test
    void testGetProductsById_productNotFound_Throw() throws ShopServiceApiException {
        UUID productId = UUID.randomUUID();

        when(productsRepo.getProductById(productId)).thenReturn(null);

        ShopDataNotFoundException exception = assertThrows(ShopDataNotFoundException.class, () -> {
            shopProductService.getProductsById(productId);
        });

        assertEquals(ResultCode.DATA_NOT_FOUND, exception.getStatus());

        verify(productsRepo, times(1)).getProductById(productId);
        verify(productImagesRepo, times(0)).getProductImagesByProductId(productId);
    }

    @Test
    void testGetProductById_imageNotFound_Throw() throws ShopServiceApiException {
        UUID productId = UUID.randomUUID();

        IProductResp productResp = mock(IProductResp.class);
        when(productsRepo.getProductById(productId)).thenReturn(productResp);

        IProductImagesResp iProductImagesResp = mock(IProductImagesResp.class);
        when(iProductImagesResp.getProductImgId()).thenReturn(null);

        ShopDataNotFoundException ex = assertThrows(ShopDataNotFoundException.class, () -> {
            shopProductService.getProductsById(productId);
        });

        assertEquals(ResultCode.DATA_NOT_FOUND, ex.getStatus());

        verify(productsRepo, times(1)).getProductById(productId);
        verify(productImagesRepo, times(1)).getProductImagesByProductId(productId);
    }

}
