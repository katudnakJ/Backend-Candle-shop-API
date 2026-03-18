package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.UserCheckTemp;
import com.senior.candleShopProject.common.SupabaseService.SupabaseStorageService;
import com.senior.candleShopProject.common.exception.ShopBadRequestException;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.utils.PaginationUtil;
import com.senior.candleShopProject.common.utils.ProcessImageUtil;
import com.senior.candleShopProject.common.utils.SupabaseStorageUtils;
import com.senior.candleShopProject.common.utils.dto.PaginationBuildResp;
import com.senior.candleShopProject.datasource.repo.ProductImagesRepo;
import com.senior.candleShopProject.datasource.repo.ProductsRepo;
import com.senior.candleShopProject.datasource.domain.products.IProductHomeListItemResp;
import com.senior.candleShopProject.datasource.domain.products.IProductResp;
import com.senior.candleShopProject.datasource.entities.ProductsEntity;
import com.senior.candleShopProject.feature.product.controller.dto.request.CreateNewProductReq;
import com.senior.candleShopProject.feature.product.controller.dto.request.UpdateProductReq;
import com.senior.candleShopProject.feature.product.controller.dto.response.ProductImagesResp;
import com.senior.candleShopProject.feature.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestComponent
public class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductsRepo productsRepo;

    @Mock
    private ProductImagesRepo productImagesRepo;

    @Mock
    private UserCheckTemp userCheckTemp;

    @Mock
    private SupabaseStorageService supabaseStorageService;

    @Mock
    private SupabaseStorageUtils supabaseStorageUtils;

    @Mock
    private ProcessImageUtil processImageUtil;

    @Mock
    private PaginationUtil paginationUtil;

    @BeforeEach
    void initTests() {MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetProductDetailById_Success() throws ShopServiceApiException {
       UUID productId = UUID.randomUUID();

       IProductResp productResp = mock(IProductResp.class);
       when(productsRepo.getProductById(productId)).thenReturn(productResp);

       ProductImagesResp productImagesResp = mock(ProductImagesResp.class);

       when(productImagesRepo.getProductImagesByProductId(productId)).thenReturn(List.of(productImagesResp));


       GenericResponse response = productService.getProductDetailById(productId);

       assertNotNull(response);
       assertEquals(response.getStatus(), ResultCode.SUCCESS);

       verify(productsRepo, times(1)).getProductById(productId);
       verify(productImagesRepo, times(1)).getProductImagesByProductId(productId);
    }

    @Test
    void testGetProductDetailById_productNotFound_Throw() throws ShopServiceApiException {
        UUID productId = UUID.randomUUID();

        when(productsRepo.getProductById(productId)).thenReturn(null);

        ShopDataNotFoundException exception = assertThrows(ShopDataNotFoundException.class, () -> {
            productService.getProductDetailById(productId);
        });


        verify(productsRepo, times(1)).getProductById(productId);
        verify(productImagesRepo, times(0)).getProductImagesByProductId(productId);
    }

    @Test
    void testGetProductHomeListItem_Success() throws ShopServiceApiException {
        int page = 0;
        int size = 10;

        PaginationBuildResp paginationBuildResp = mock(PaginationBuildResp.class);

        when(paginationUtil.buildPaginationResp(anyInt(), anyInt(), anyLong())).thenReturn(paginationBuildResp);

        IProductHomeListItemResp featuredProductResp = mock(IProductHomeListItemResp.class);
        when(productsRepo.getProductHomeListItemByFeature(true)).thenReturn(List.of(featuredProductResp));
        when(productsRepo.getAllProductHomeList(size, 0)).thenReturn(List.of(featuredProductResp));

        GenericResponse response = productService.getProductHomeListItem(page,size);

        assertNotNull(response);

        verify(productsRepo, times(1)).getProductHomeListItemByFeature(anyBoolean());
        verify(productsRepo, times(1)).getAllProductHomeList(size, 0);
    }

    @Test
    void testGetProductHomeListItem_DataNotFound() throws ShopServiceApiException {

        when(productsRepo.getAllProductHomeList(0,10)).thenReturn(Collections.emptyList());
        when(productsRepo.getProductHomeListItemByFeature(anyBoolean())).thenReturn(Collections.emptyList());

        ShopDataNotFoundException ex = assertThrows(ShopDataNotFoundException.class, () -> {
            productService.getProductHomeListItem(0,10);
        });
    }

    @Test
    void testCreateNewProduct_Success() throws ShopServiceApiException, IOException {
        UUID userId = UUID.randomUUID();
        UUID newProductId = UUID.randomUUID();

        CreateNewProductReq req = new CreateNewProductReq();
        req.setProductName("Candle A");
        req.setPrice(BigDecimal.valueOf(199.0));
        req.setWeight(100.0);
        req.setDescription("Scented candle");
        req.setActive(true);
        req.setFeatured(false);

        byte[] content = new byte[] { (byte)0xFF, (byte)0xD8, (byte)0xFF };
        MockMultipartFile img1 = new MockMultipartFile("images", "test1.jpg", "image/jpeg", content);
        MockMultipartFile img2 = new MockMultipartFile("images", "test2.jpg", "image/jpeg", content);
        List<MultipartFile> images = List.of(img1, img2);
        int primaryIndex = 0;

        when(userCheckTemp.getSellerIdByUserId(userId)).thenReturn(UUID.randomUUID());

        ProductsEntity saved = new ProductsEntity();
        saved.setProductId(newProductId);

        when(productsRepo.save(any(ProductsEntity.class))).thenReturn(saved);

        try (MockedStatic<ProcessImageUtil> mockedProcess = mockStatic(ProcessImageUtil.class)) {
            mockedProcess.when(() -> ProcessImageUtil.processImageData(any(MultipartFile.class)))
                    .thenReturn(new byte[] { 1, 2, 3 });

            GenericResponse resp = productService.createNewProduct(userId, req, images, primaryIndex);

            assertNotNull(resp);
            assertNotNull(resp.getData());

            verify(userCheckTemp, times(1)).getSellerIdByUserId(userId);
            verify(productsRepo, times(1)).save(any(ProductsEntity.class));
            verify(productImagesRepo, times(1)).saveAll(anyList());
            verify(supabaseStorageService, times(2)).uploadFile(anyString(), anyString(), any(byte[].class), anyString());
        }
    }

    @Test
    void testCreateNewProduct_BadRequest_Throw() {
        UUID userId = UUID.randomUUID();

        ShopBadRequestException ex = assertThrows(ShopBadRequestException.class, () -> {
            productService.createNewProduct(userId, null, List.of(), -1);
        });

        verifyNoInteractions(productsRepo);
        verifyNoInteractions(productImagesRepo);
    }

    @Test
    void testUpdateProduct_Success() throws ShopServiceApiException, IOException {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        UpdateProductReq req = new UpdateProductReq();
        req.setProductName("Candle B");
        req.setPrice(BigDecimal.valueOf(199.0));
        req.setWeight(120.0);
        req.setDescription("Updated desc");
        req.setActive(true);
        req.setFeatured(true);
        req.setPrimaryIndex(1);
        req.setDeleteImageIds(new ArrayList<>());

        byte[] content = new byte[] { (byte)0xFF, (byte)0xD8, (byte)0xFF };
        MockMultipartFile img1 = new MockMultipartFile("images", "test1.jpg", "image/jpeg", content);
        MockMultipartFile img2 = new MockMultipartFile("images", "test2.jpg", "image/jpeg", content);
        List<MultipartFile> newImages = List.of(img1, img2);

        ProductsEntity existing = new ProductsEntity();
        existing.setProductId(productId);

        when(productsRepo.existsById(productId)).thenReturn(true);
        when(productsRepo.findProductsEntityByProductId(productId)).thenReturn(existing);

        ProductImagesResp existImg = new ProductImagesResp();
        existImg.setProductImgId(UUID.randomUUID());
        existImg.setProductImgPath("old.jpeg");
        existImg.setIsPrimary(true);

        when(productImagesRepo.getProductImagesByProductId(productId)).thenReturn(List.of(existImg));
        when(userCheckTemp.getSellerIdByUserId(userId)).thenReturn(UUID.randomUUID());
        when(productsRepo.save(any(ProductsEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        try (MockedStatic<ProcessImageUtil> mockedProcess = mockStatic(ProcessImageUtil.class)) {
            mockedProcess.when(() -> ProcessImageUtil.processImageData(any(MultipartFile.class)))
                    .thenReturn(new byte[] { 1, 2, 3 });

            GenericResponse resp = productService.updateProduct(userId, productId, req, newImages);

            assertNotNull(resp);

            verify(productsRepo, times(1)).findProductsEntityByProductId(productId);
            verify(productsRepo, times(1)).save(any(ProductsEntity.class));
        }
    }

    @Test
    void testUpdateProduct_ExistImagesNotFound_Throw() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        UpdateProductReq req = new UpdateProductReq();
        req.setProductName("Candle B");
        req.setPrice(BigDecimal.valueOf(199.0));
        req.setWeight(120.0);
        req.setDescription("Updated desc");
        req.setActive(true);
        req.setFeatured(true);
        req.setPrimaryIndex(1);
        req.setExistIntoPrimary(null);
        req.setDeleteImageIds(null);

        MultipartFile img1 = mock(MultipartFile.class);
        when(img1.isEmpty()).thenReturn(false);
        when(img1.getSize()).thenReturn(1024L);
        when(img1.getContentType()).thenReturn("image/jpeg");

        when(userCheckTemp.getSellerIdByUserId(userId)).thenReturn(UUID.randomUUID());

        ProductsEntity existing = new ProductsEntity();
        existing.setProductId(productId);
        when(productsRepo.findById(productId)).thenReturn(java.util.Optional.of(existing));

        when(productImagesRepo.getProductImagesByProductId(productId)).thenReturn(Collections.emptyList());

        ShopDataNotFoundException ex = assertThrows(ShopDataNotFoundException.class, () -> {
            productService.updateProduct(userId, productId, req, List.of(img1));
        });

        verify(productImagesRepo, never()).deleteProductImagesEntitiesByProductsEntity_ProductId(any());
    }

    @Test
    void testDeleteProduct_Success() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(userCheckTemp.getSellerIdByUserId(userId)).thenReturn(UUID.randomUUID());

        ProductsEntity existing = new ProductsEntity();
        existing.setProductId(productId);
        when(productsRepo.findById(productId)).thenReturn(java.util.Optional.of(existing));

        ProductImagesResp img = new ProductImagesResp();
        img.setProductImgPath("img.jpeg");
        when(productImagesRepo.getProductImagesByProductId(productId)).thenReturn(List.of(img));

        GenericResponse resp = productService.deleteProduct(userId, productId);

        assertNotNull(resp);
        assertNull(resp.getData());

        verify(productsRepo, times(1)).delete(existing);
        verify(supabaseStorageService, times(1)).deleteFiles(anyString(), anySet());
    }

    @Test
    void testDeleteProduct_ProductNotFound_Throw() {
        UUID userId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        when(userCheckTemp.getSellerIdByUserId(userId)).thenReturn(UUID.randomUUID());
        when(productsRepo.findById(productId)).thenReturn(java.util.Optional.empty());

        ShopDataNotFoundException ex = assertThrows(ShopDataNotFoundException.class, () -> {
            productService.deleteProduct(userId, productId);
        });

        verify(productsRepo, never()).delete(any());
    }
}
