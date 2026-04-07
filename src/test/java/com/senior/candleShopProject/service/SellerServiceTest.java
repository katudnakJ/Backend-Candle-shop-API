package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.SupabaseService.Dto.SignedFileUrlResp;
import com.senior.candleShopProject.common.SupabaseService.SupabaseStorageService;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.common.utils.SupabaseStorageUtils;
import com.senior.candleShopProject.datasource.domain.users.ISellerResp;
import com.senior.candleShopProject.datasource.domain.users.IUsersResp;
import com.senior.candleShopProject.datasource.entities.SellerEntity;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.datasource.repo.SellerRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import com.senior.candleShopProject.feature.user.service.SellerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestComponent
public class SellerServiceTest {

    @InjectMocks
    private SellerService sellerService;

    @Mock
    private OrdersRepo ordersRepo;

    @Mock
    private UsersRepo usersRepo;

    @Mock
    private SellerRepo sellerRepo;

    @Mock
    SupabaseStorageService supabaseStorageService;

    @Mock
    private SupabaseStorageUtils supabaseStorageUtils;

    @BeforeEach
    void initTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSellerOrderCountByStatus_InvalidStatus_ThrowsException() {
        // Given
        String invalidStatus = "invalid_status";

        // When & Then
        assertThrows(ShopInvalidParamException.class, () -> {
            sellerService.getSellerOrderCountByStatus(invalidStatus);
        });

        verify(ordersRepo, never()).getCountOrdersWithStatus(any());
    }

    @Test
    void testGetQrCodePayment_UserNotFound() {
        UUID userId = UUID.randomUUID();

        when(usersRepo.getUserProfile(userId)).thenReturn(null);

        assertThrows(ShopDataNotFoundException.class, () -> {
            sellerService.getQrCodePayment(userId);
        });
    }

    @Test
    void testGetQrCodePayment_SellerFound_ReturnsSignedUrl() throws Exception {

        UUID userId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        IUsersResp user = mock(IUsersResp.class);
        ISellerResp seller = mock(ISellerResp.class);

        SignedFileUrlResp result = mock(SignedFileUrlResp.class);

        when(usersRepo.getUserProfile(userId)).thenReturn(user);
        when(user.getIsSeller()).thenReturn(true);

        when(sellerRepo.getQrPaymentImagePath(userId)).thenReturn(seller);
        when(seller.getSellerId()).thenReturn(sellerId);
        when(seller.getQrPaymentImgPath()).thenReturn("qr.jpg");

        when(supabaseStorageUtils.getSignedQrPaymentImage(eq(sellerId), eq("qr.jpg"))).thenReturn(result);
        when(supabaseStorageUtils.getSignedQrPaymentImage(any(), any())).thenReturn(result);

        GenericResponse response = sellerService.getQrCodePayment(userId);

        assertNotNull(response.getData());

        verify(supabaseStorageUtils, times(1))
                .getSignedQrPaymentImage(sellerId, "qr.jpg");
    }

    @Test
    void testAddQrCodePayment_UserNotFound() {
        UUID userId = UUID.randomUUID();

        byte[] content = "fake-image-binary-content".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "imageData",
                "test-qr.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                content
        );

        when(usersRepo.getUserProfile(userId)).thenReturn(null);
        assertThrows(ShopDataNotFoundException.class, () -> {
            sellerService.addQrCodePayment(userId, multipartFile, true);
        });
    }

    @Test
    void testAddQrCodePayment_UserNotSeller() {
        UUID userId = UUID.randomUUID();
        IUsersResp user = mock(IUsersResp.class);

        byte[] content = "fake-image-binary-content".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "imageData",
                "test-qr.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                content
        );

        when(usersRepo.getUserProfile(userId)).thenReturn(user);
        when(user.getIsSeller()).thenReturn(false);

        assertThrows(ShopForbiddenException.class, () -> {
            sellerService.addQrCodePayment(userId, multipartFile, true);
        });
    }

    @Test
    void testAddQrCodePayment_SellerNotFound() {
        UUID userId = UUID.randomUUID();
        IUsersResp user = mock(IUsersResp.class);

        byte[] content = "fake-image-binary-content".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "imageData",
                "test-qr.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                content
        );

        when(usersRepo.getUserProfile(userId)).thenReturn(user);
        when(user.getIsSeller()).thenReturn(true);
        when(sellerRepo.getSellerEntitiesByUsersEntity_UserId(userId)).thenReturn(null);

        assertThrows(ShopDataNotFoundException.class, () -> {
            sellerService.addQrCodePayment(userId, multipartFile, true);
        });
    }

    @Test
    void testAddQrCodePayment_QrAlreadyExists() {
        UUID userId = UUID.randomUUID();

        IUsersResp user = mock(IUsersResp.class);
        SellerEntity sellerEntity = mock(SellerEntity.class);

        byte[] content = "fake-image-binary-content".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "imageData",
                "test-qr.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                content
        );

        when(usersRepo.getUserProfile(userId)).thenReturn(user);
        when(user.getIsSeller()).thenReturn(true);
        when(sellerRepo.getSellerEntitiesByUsersEntity_UserId(userId)).thenReturn(sellerEntity);
        when(sellerEntity.getQrPaymentImgPath()).thenReturn("exists.jpg");

        assertThrows(ShopBadRequestException.class, () -> {
            sellerService.addQrCodePayment(userId, multipartFile, true);
        });
    }

    @Test
    void testAddQrCodePayment_Success() throws ShopServiceApiException, IOException {
        UUID userId = UUID.randomUUID();

        IUsersResp user = mock(IUsersResp.class);
        SellerEntity sellerEntity = mock(SellerEntity.class);

        BufferedImage bufferedImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(bufferedImage, "jpg", baos);
        byte[] realImageContent = baos.toByteArray();

        MockMultipartFile multipartFile = new MockMultipartFile(
                "imageData",
                "test-qr.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                realImageContent
        );

        when(usersRepo.getUserProfile(userId)).thenReturn(user);
        when(user.getIsSeller()).thenReturn(true);
        when(sellerRepo.getSellerEntitiesByUsersEntity_UserId(userId)).thenReturn(sellerEntity);
        when(sellerEntity.getQrPaymentImgPath()).thenReturn(null);
        when(sellerEntity.getSellerId()).thenReturn(UUID.randomUUID());
        when(sellerRepo.save(any())).thenReturn(sellerEntity);

        doNothing().when(supabaseStorageService).uploadFile(any(), any(), any(), any());



        GenericResponse resp = sellerService.addQrCodePayment(userId, multipartFile, true);

        verify(supabaseStorageUtils, times(1)).uploadQrPaymentImage(sellerEntity, multipartFile);
    }

    @Test
    void testSyncQrCodePayment_UserNotFound() {
        UUID userId = UUID.randomUUID();

        byte[] content = "fake-image-binary-content".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "imageData",
                "test-qr.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                content
        );

        when(usersRepo.getUserProfile(userId)).thenReturn(null);
        assertThrows(ShopDataNotFoundException.class, () -> {
            sellerService.syncQrCodePayment(userId, multipartFile, true);
        });
    }

    @Test
    void testSyncQrCodePayment_UserNotSeller(){
        UUID userId = UUID.randomUUID();

        byte[] content = "fake-image-binary-content".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "imageData",
                "test-qr.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                content
        );

        IUsersResp user = mock(IUsersResp.class);

        when(usersRepo.getUserProfile(userId)).thenReturn(user);
        when(user.getIsSeller()).thenReturn(false);

        assertThrows(ShopForbiddenException.class, () -> {
            sellerService.syncQrCodePayment(userId, multipartFile, true);
        });
    }

    @Test
    void testSyncQrCodePayment_SellerNotFound(){
        UUID userId = UUID.randomUUID();

        byte[] content = "fake-image-binary-content".getBytes();
        MockMultipartFile multipartFile = new MockMultipartFile(
                "imageData",
                "test-qr.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                content
        );

        IUsersResp user = mock(IUsersResp.class);
        when(usersRepo.getUserProfile(userId)).thenReturn(user);
        when(user.getIsSeller()).thenReturn(true);
        when(sellerRepo.getSellerEntitiesByUsersEntity_UserId(userId)).thenReturn(null);

        assertThrows(ShopDataNotFoundException.class, () -> {
            sellerService.syncQrCodePayment(userId, multipartFile, true);
        });
    }

    @Test
    void testSyncQrCodePayment_Success() throws ShopServiceApiException, IOException {
        UUID userId = UUID.randomUUID();

        IUsersResp user = mock(IUsersResp.class);
        SellerEntity sellerEntity = mock(SellerEntity.class);

        BufferedImage bufferedImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        javax.imageio.ImageIO.write(bufferedImage, "jpg", baos);
        byte[] realImageContent = baos.toByteArray();

        MockMultipartFile multipartFile = new MockMultipartFile(
                "imageData",
                "test-qr.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                realImageContent
        );

        when(usersRepo.getUserProfile(userId)).thenReturn(user);
        when(user.getIsSeller()).thenReturn(true);
        when(sellerRepo.getSellerEntitiesByUsersEntity_UserId(userId)).thenReturn(sellerEntity);
        when(sellerEntity.getSellerId()).thenReturn(UUID.randomUUID());
        when(sellerEntity.getQrPaymentImgPath()).thenReturn("qr.jpg");
        doNothing().when(supabaseStorageService).uploadFile(any(), any(), any(), any());
        when(sellerRepo.findById(any())).thenReturn(Optional.of(sellerEntity));
        when(sellerRepo.save(any())).thenReturn(sellerEntity);

        GenericResponse resp = sellerService.syncQrCodePayment(userId, multipartFile, true);
    }
}
