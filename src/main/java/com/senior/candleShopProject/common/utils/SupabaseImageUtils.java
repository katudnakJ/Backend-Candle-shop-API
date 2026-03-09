package com.senior.candleShopProject.common.utils;

import com.senior.candleShopProject.common.SupabaseService.Dto.SignedImageUrlResp;
import com.senior.candleShopProject.common.SupabaseService.SupabaseStorageService;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.entities.PaymentsEntity;
import com.senior.candleShopProject.datasource.entities.SellerEntity;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static com.senior.candleShopProject.common.utils.LocalDateTimeUtils.*;
import static com.senior.candleShopProject.common.utils.ProcessImageUtil.processImageData;

@Getter
@Setter
@Component
@RequiredArgsConstructor
public class SupabaseImageUtils {

    private final SupabaseStorageService supabaseStorageService;

    @Value("${supabase.expiration.seller.check-qr-payment}")
    private int sellerCheckQrPaymentExp;

    @Value("${supabase.expiration.seller.check-receipt}")
    private int sellerCheckReceiptExp;

    @Value("${supabase.expiration.customer.check-qr-payment}")
    private int customerCheckPaymentExp;

    public SignedImageUrlResp getSignedPaymentProofImage(UUID customerId, UUID paymentId, String paymentProofPath, Instant createdAt) throws ShopServiceApiException {

        String yearPrefix = String.valueOf(createdAt.atZone(ZoneId.of(Constants.TIME_ZONE_BANGKOK)).getYear());

        String bucketName = Constants.SUPABASE_RECEIPT_BUCKET_NAME;
        String imagePath = yearPrefix + "/" + customerId + "/" + paymentId + "/" + paymentProofPath;

        ZonedDateTime expirationDate = getExpDateWithTimeZone(Constants.TIME_ZONE_BANGKOK, sellerCheckReceiptExp);

        String signedImageUrl = supabaseStorageService.getSignedImageUrl(bucketName, imagePath, sellerCheckReceiptExp);

        SignedImageUrlResp signedImageUrlResp = new SignedImageUrlResp();
        signedImageUrlResp.setSignedImageUrl(signedImageUrl);
        signedImageUrlResp.setExpiresAt(expirationDate);

        return signedImageUrlResp;
    }

    public SignedImageUrlResp getSignedQrPaymentImage(UUID sellerId, String qrPaymentImgPath) throws ShopServiceApiException {

        String bucketName = Constants.SUPABASE_QR_PAYMENT_BUCKET_NAME;
        String imagePath = sellerId + "/" + qrPaymentImgPath;

        ZonedDateTime expirationDate = getExpDateWithTimeZone(Constants.TIME_ZONE_BANGKOK, sellerCheckQrPaymentExp);

        String signedImageUrl = supabaseStorageService.getSignedImageUrl(bucketName, imagePath, sellerCheckQrPaymentExp);

        SignedImageUrlResp signedImageUrlResp = new SignedImageUrlResp();
        signedImageUrlResp.setSignedImageUrl(signedImageUrl);
        signedImageUrlResp.setExpiresAt(expirationDate);

        return signedImageUrlResp;
    }

    public void uploadPaymentProofImage(Instant createdAt, UUID customerId, PaymentsEntity newPaymentEntity,
                                        MultipartFile paymentProof, String runningNumber) throws IOException {
        String yearPrefix = String.valueOf(createdAt.atZone(ZoneId.of(Constants.TIME_ZONE_BANGKOK)).getYear());
        String imagePath = yearPrefix + "/" +
                customerId + "/"
                + newPaymentEntity.getPaymentId() + "/"
                + runningNumber
                + "." + Constants.CONTENT_TYPE_JPEG.split("/")[1];

        supabaseStorageService.uploadImage(
                Constants.SUPABASE_RECEIPT_BUCKET_NAME,
                imagePath,
                processImageData(paymentProof),
                Constants.CONTENT_TYPE_JPEG
        );
    }

    public void uploadQrPaymentImage(SellerEntity sellerEntity, MultipartFile imageData) throws IOException {
        String imagePath = sellerEntity.getSellerId() + "/" + sellerEntity.getQrPaymentImgPath();

        supabaseStorageService.uploadImage(
                Constants.SUPABASE_QR_PAYMENT_BUCKET_NAME,
                imagePath,
                processImageData(imageData),
                Constants.CONTENT_TYPE_JPEG
        );
    }
}