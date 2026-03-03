package com.senior.candleShopProject.common.utils;


import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopBadRequestException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageValidationUtils {

    private static final long maxFileSizeInBytes = 2 * 1024 * 1024; // 2MB
    private static final Set<String> allowedExtensions =  Set.of("image/jpg", "image/png", "image/jpeg");

    public static void validateImage(MultipartFile imageFile) throws ShopServiceApiException {
        if(imageFile == null || imageFile.isEmpty())
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST,"File is empty.");

        if(imageFile.getSize() > maxFileSizeInBytes)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST,"File size exceeds the maximum limit of 2MB.");

        if(!allowedExtensions.contains(imageFile.getContentType()))
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST,"Invalid file type. Only JPG and PNG images are allowed.");
    }

    public  static void validateImages(List<MultipartFile> images) throws ShopServiceApiException {
        if (images == null || images.isEmpty())
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "No files uploaded.");

        for (MultipartFile image : images) {
            validateImage(image);
        }
    }

}
