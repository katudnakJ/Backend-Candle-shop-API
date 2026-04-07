package com.senior.candleShopProject.common.utils;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProcessImageUtil {

//    make image to jpeg, size 800x800, quality 1.0, and return byte array
    public static byte[] processImageData(MultipartFile imageData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Thumbnails.of(imageData.getInputStream())
                .size(800, 800)
                .outputFormat("jpeg")
                .outputQuality(1.0)
                .toOutputStream(outputStream);

        return outputStream.toByteArray();
    }
}
