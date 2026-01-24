package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.domain.IProductHomeListItemResp;
import com.senior.candleShopProject.datasource.domain.ProductHomeListItemResp;
import com.senior.candleShopProject.datasource.repo.ProductImagesRepo;
import com.senior.candleShopProject.datasource.repo.ProductsRepo;
import com.senior.candleShopProject.datasource.domain.IProductImagesResp;
import com.senior.candleShopProject.datasource.domain.IProductResp;
import com.senior.candleShopProject.feature.product.service.ShopProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.TestComponent;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@TestComponent
public class ShopProductServiceTest {

    @InjectMocks
    private ShopProductService shopProductService;

    @Mock
    private ProductsRepo productsRepo;

    @Mock
    private ProductImagesRepo productImagesRepo;

    @BeforeEach
    void initTests() {MockitoAnnotations.openMocks(this);
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
    void testGetProductHomeListItem_Success() throws ShopServiceApiException {

        IProductHomeListItemResp featuredProductResp = mock(IProductHomeListItemResp.class);
        when(productsRepo.getProductHomeListItemResp(anyBoolean())).thenReturn(List.of(featuredProductResp));
        when(productsRepo.getCountProductHomeListItemResp(anyBoolean())).thenReturn(1);

        GenericResponse response = shopProductService.getProductHomeListItem();

        assertNotNull(response);
        assertEquals(ResultCode.SUCCESS, response.getStatus());

        verify(productsRepo, times(2)).getProductHomeListItemResp(anyBoolean());
        verify(productsRepo, times(2)).getCountProductHomeListItemResp(anyBoolean());
    }

    @Test
    void testGetProductHomeListItem_DataNotFound() throws ShopServiceApiException {
        when(productsRepo.getProductHomeListItemResp(anyBoolean())).thenReturn(Collections.emptyList());
        when(productsRepo.getCountProductHomeListItemResp(anyBoolean())).thenReturn(0);

        ShopDataNotFoundException ex = assertThrows(ShopDataNotFoundException.class, () -> {
            shopProductService.getProductHomeListItem();
        });
        assertEquals(ResultCode.DATA_NOT_FOUND, ex.getStatus());
    }

}
