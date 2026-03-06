package com.senior.candleShopProject.common.utils;

import com.senior.candleShopProject.common.SupabaseService.Dto.SignedImageUrlResp;
import com.senior.candleShopProject.common.SupabaseService.SupabaseStorageService;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.UUID;

import static com.senior.candleShopProject.common.utils.LocalDateTimeUtils.getExpDateWithTimeZone;

@Getter
@Setter
@Component
@RequiredArgsConstructor
public class GetImagePathUtils {

    private final SupabaseStorageService supabaseStorageService;

    @Value("${supabase.expiration.seller.check-qr-payment}")
    private int sellerCheckQrPaymentExp;

    @Value("${supabase.expiration.seller.check-receipt}")
    private int sellerCheckReceiptExp;

    @Value("${supabase.expiration.customer.check-qr-payment}")
    private int customerCheckPaymentExp;

    public SignedImageUrlResp getSignedPaymentProofImage(UUID customerId, UUID paymentId, String paymentProofPath) throws ShopServiceApiException {

        String bucketName = Constants.SUPABASE_RECEIPT_BUCKET_NAME;
        String imagePath = customerId + "/" + paymentId + "/" + paymentProofPath;

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
}