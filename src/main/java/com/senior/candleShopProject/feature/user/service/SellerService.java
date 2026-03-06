package com.senior.candleShopProject.feature.user.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.SupabaseService.Dto.SignedImageUrlResp;
import com.senior.candleShopProject.common.SupabaseService.SupabaseStorageService;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.GetImagePathUtils;
import com.senior.candleShopProject.datasource.domain.users.ISellerResp;
import com.senior.candleShopProject.datasource.domain.users.IUsersResp;
import com.senior.candleShopProject.datasource.entities.SellerEntity;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.datasource.repo.SellerRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static com.senior.candleShopProject.common.utils.LocalDateTimeUtils.getExpDateWithTimeZone;
import static com.senior.candleShopProject.common.utils.ProcessImageUtil.processImageData;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerService {

    private final SupabaseStorageService supabaseStorageService;
    private final GetImagePathUtils getImagePathUtils;

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
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User not found.");

        if(!usersResp.getIsSeller())
            throw new ShopForbiddenException(ResultCode.INVALID_PARAMS, "You don't have permission.");

        ISellerResp seller = sellerRepo.getSellerByUserId(userId);

        SignedImageUrlResp result = getImagePathUtils.getSignedQrPaymentImage(
                seller.getSellerId(),
                seller.getQrPaymentImgPath()
        );

        GenericResponse response = new GenericResponse();
        response.setData(result);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    @Transactional
    public GenericResponse addQrCodePayment(UUID userId, MultipartFile imageData) throws ShopServiceApiException, IOException {
        IUsersResp usersResp = usersRepo.getUserProfile(userId);

        if (usersResp == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User not found.");

        if(!usersResp.getIsSeller())
            throw new ShopForbiddenException(ResultCode.INVALID_PARAMS, "You don't have permission.");

        SellerEntity sellerEntity = sellerRepo.getSellerEntitiesByUsersEntity_UserId(userId);

        if (sellerEntity == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Seller not found.");

        if (sellerEntity.getQrPaymentImgPath() != null)
            throw new ShopConflictException(ResultCode.CONFLICT, "QR code payment already exists.");

        String type = Constants.CONTENT_TYPE_JPEG.split("/")[1];
        String genQrPaymentUUID = UUID.randomUUID().toString();
        sellerEntity.setQrPaymentImgPath(genQrPaymentUUID+ "." + type);

        sellerRepo.save(sellerEntity);

        String imagePath = sellerEntity.getSellerId() + "/" + genQrPaymentUUID;

        supabaseStorageService.uploadImage(
                Constants.SUPABASE_QR_PAYMENT_BUCKET_NAME,
                imagePath,
                processImageData(imageData),
                Constants.CONTENT_TYPE_JPEG
        );

        GenericResponse response = new GenericResponse();
        response.setData(null);
        response.setStatus(ResultCode.CREATED);
        return response;
    }

    @Transactional
    public GenericResponse syncQrCodePayment(UUID userId, MultipartFile imageData) throws ShopServiceApiException, IOException {
        IUsersResp usersResp = usersRepo.getUserProfile(userId);

        if (usersResp == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User not found.");

        if(!usersResp.getIsSeller())
            throw new ShopForbiddenException(ResultCode.INVALID_PARAMS, "You don't have permission.");


        SellerEntity sellerEntity = sellerRepo.getSellerEntitiesByUsersEntity_UserId(userId);

        if (sellerEntity == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Seller not found.");

        String imagePath = sellerEntity.getSellerId() + "/" + sellerEntity.getQrPaymentImgPath();

        supabaseStorageService.uploadImage(
                Constants.SUPABASE_QR_PAYMENT_BUCKET_NAME,
                imagePath,
                processImageData(imageData),
                Constants.CONTENT_TYPE_JPEG
        );

        GenericResponse response = new GenericResponse();
        response.setData(null);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }
}
