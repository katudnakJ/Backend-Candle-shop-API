package com.senior.candleShopProject.feature.user.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.SupabaseService.Dto.SignedFileUrlResp;
import com.senior.candleShopProject.common.SupabaseService.SupabaseStorageService;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.SupabaseStorageUtils;
import com.senior.candleShopProject.datasource.domain.users.ISellerResp;
import com.senior.candleShopProject.datasource.domain.users.IUsersResp;
import com.senior.candleShopProject.datasource.entities.SellerEntity;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.datasource.repo.SellerRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerService {

    private final SupabaseStorageService supabaseStorageService;
    private final SupabaseStorageUtils supabaseStorageUtils;

    private final OrdersRepo ordersRepo;
    private final UsersRepo usersRepo;
    private final SellerRepo sellerRepo;

    public GenericResponse getSellerOrderCountByStatus(String status) throws ShopServiceApiException {

        if(!OrderStatus.isValidStatus(status))
            throw new ShopInvalidParamException(ResultCode.INVALID_PARAMS, "Invalid order status.");

        Integer orderCount = ordersRepo.getCountOrdersWithStatus(status);

        GenericResponse response = new GenericResponse();
        response.setData(orderCount);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    public GenericResponse getQrCodePayment (UUID userId) throws ShopServiceApiException {

        IUsersResp usersResp = usersRepo.getUserProfile(userId);

        if(usersResp == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "ไม่พบผู้ใช้งานนี้ในระบบ","User not found.");

        GenericResponse response = new GenericResponse();

            ISellerResp seller = sellerRepo.getQrPaymentImagePath(userId);

            if(seller.getQrPaymentImgPath() == null || seller.getQrPaymentImgPath().isEmpty()) {
                response.setData(null);
                response.setStatus(ResultCode.SUCCESS);
                return response;

            }
            SignedFileUrlResp result = supabaseStorageUtils.getSignedQrPaymentImage(
                    seller.getSellerId(),
                    seller.getQrPaymentImgPath()
            );
            response.setData(result);
            response.setStatus(ResultCode.SUCCESS);
            return response;
    }

    @Transactional
    public GenericResponse addQrCodePayment(UUID userId, MultipartFile imageData, boolean isOwner) throws ShopServiceApiException, IOException {

        if (!isOwner)
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่ได้รับอนุญาตให้เข้าถึงหน้านี้", "User don't have permission.");

        IUsersResp usersResp = usersRepo.getUserProfile(userId);

        if (usersResp == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "ไม่พบผู้ใช้งานนี้ในระบบ","User not found.");

        if(!usersResp.getIsSeller())
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่ได้รับอนุญาตให้เข้าถึงหน้านี้", "User don't have permission.");

        SellerEntity sellerEntity = sellerRepo.getSellerEntitiesByUsersEntity_UserId(userId);

        if (sellerEntity == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Seller not found.");

        if (sellerEntity.getQrPaymentImgPath() != null)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "มี QR Code ในระบบแล้ว","QR code payment already exists.");

        String type = Constants.CONTENT_TYPE_JPEG.split("/")[1];
        UUID genQrPaymentUUID = UUID.randomUUID();
        sellerEntity.setQrPaymentImgPath(genQrPaymentUUID+ "." + type);

        SellerEntity newSeller = sellerRepo.save(sellerEntity);

        supabaseStorageUtils.uploadQrPaymentImage(newSeller, imageData);

        GenericResponse response = new GenericResponse();
        response.setData(null);
        response.setStatus(ResultCode.CREATED);
        return response;
    }

    @Transactional
    public GenericResponse syncQrCodePayment(UUID userId, MultipartFile imageData, boolean isOwner) throws ShopServiceApiException, IOException {

        if (!isOwner)
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่ได้รับอนุญาตให้เข้าถึงหน้านี้", "User don't have permission.");

        IUsersResp usersResp = usersRepo.getUserProfile(userId);

        if (usersResp == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "ไม่พบผู้ใช้งานนี้ในระบบ","User not found.");

        if(!usersResp.getIsSeller())
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่ได้รับอนุญาตให้เข้าถึงหน้านี้", "User don't have permission.");


        SellerEntity sellerEntity = sellerRepo.getSellerEntitiesByUsersEntity_UserId(userId);

        if (sellerEntity == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Seller not found.");

        supabaseStorageUtils.uploadQrPaymentImage(
                sellerEntity,
                imageData);

        GenericResponse response = new GenericResponse();
        response.setData(null);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }
}
