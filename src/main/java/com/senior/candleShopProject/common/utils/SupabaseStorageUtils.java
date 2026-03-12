package com.senior.candleShopProject.common.utils;

import com.senior.candleShopProject.common.SupabaseService.Dto.SignedFileUrlResp;
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
public class SupabaseStorageUtils {

    private final SupabaseStorageService supabaseStorageService;

    @Value("${supabase.expiration.seller.check-qr-payment}")
    private int sellerCheckQrPaymentExp;

    @Value("${supabase.expiration.seller.check-receipt}")
    private int sellerCheckReceiptExp;

    @Value("${supabase.expiration.customer.check-qr-payment}")
    private int customerCheckPaymentExp;

    @Value("${pdf.getFile.expiration}")
    private int getPdfFileExp;

    public SignedFileUrlResp getSignedPaymentProofImage(UUID customerId, UUID paymentId, String paymentProofPath, Instant createdAt) throws ShopServiceApiException {

        String yearPrefix = String.valueOf(createdAt.atZone(ZoneId.of(Constants.TIME_ZONE_BANGKOK)).getYear());

        String bucketName = Constants.SUPABASE_RECEIPT_BUCKET_NAME;
        String imagePath = yearPrefix + "/" + customerId + "/" + paymentId + "/" + paymentProofPath;

        ZonedDateTime expirationDate = getExpDateWithTimeZone(Constants.TIME_ZONE_BANGKOK, sellerCheckReceiptExp);

        String signedImageUrl = supabaseStorageService.getSignedFileUrl(bucketName, imagePath, sellerCheckReceiptExp);

        SignedFileUrlResp signedFileUrlResp = new SignedFileUrlResp();
        signedFileUrlResp.setSignedFileUrl(signedImageUrl);
        signedFileUrlResp.setExpiresAt(expirationDate);

        return signedFileUrlResp;
    }

    public SignedFileUrlResp getSignedQrPaymentImage(UUID sellerId, String qrPaymentImgPath) throws ShopServiceApiException {

        String bucketName = Constants.SUPABASE_QR_PAYMENT_BUCKET_NAME;
        String imagePath = sellerId + "/" + qrPaymentImgPath;

        ZonedDateTime expirationDate = getExpDateWithTimeZone(Constants.TIME_ZONE_BANGKOK, sellerCheckQrPaymentExp);

        String signedImageUrl = supabaseStorageService.getSignedFileUrl(bucketName, imagePath, sellerCheckQrPaymentExp);

        SignedFileUrlResp signedFileUrlResp = new SignedFileUrlResp();
        signedFileUrlResp.setSignedFileUrl(signedImageUrl);
        signedFileUrlResp.setExpiresAt(expirationDate);

        return signedFileUrlResp;
    }

    public void uploadPaymentProofImage(Instant createdAt, UUID customerId, PaymentsEntity newPaymentEntity,
                                        MultipartFile paymentProof, String runningNumber) throws IOException {
        String yearPrefix = String.valueOf(createdAt.atZone(ZoneId.of(Constants.TIME_ZONE_BANGKOK)).getYear());
        String imagePath = yearPrefix + "/" +
                customerId + "/"
                + newPaymentEntity.getPaymentId() + "/"
                + runningNumber
                + "." + Constants.CONTENT_TYPE_JPEG.split("/")[1];

        supabaseStorageService.uploadFile(
                Constants.SUPABASE_RECEIPT_BUCKET_NAME,
                imagePath,
                processImageData(paymentProof),
                Constants.CONTENT_TYPE_JPEG
        );
    }

    public void uploadQrPaymentImage(SellerEntity sellerEntity, MultipartFile imageData) throws IOException {
        String imagePath = sellerEntity.getSellerId() + "/" + sellerEntity.getQrPaymentImgPath();

        supabaseStorageService.uploadFile(
                Constants.SUPABASE_QR_PAYMENT_BUCKET_NAME,
                imagePath,
                processImageData(imageData),
                Constants.CONTENT_TYPE_JPEG
        );
    }

//    PDF
    public void uploadReceiptPDF(Instant createdAt,byte[] pdf, UUID customerId, UUID paymentId, String receiptNumber) throws IOException {
        String yearPrefix = String.valueOf(createdAt.atZone(ZoneId.of(Constants.TIME_ZONE_BANGKOK)).getYear());
        String pdfPath = yearPrefix + "/" +
                customerId + "/"
                + paymentId + "/"
                + receiptNumber + "." + Constants.CONTENT_TYPE_PDF.split("/")[1];

        supabaseStorageService.uploadFile(
                Constants.SUPABASE_RECEIPTS_BUCKET_NAME,
                pdfPath,
                pdf,
                Constants.CONTENT_TYPE_PDF
        );
    }

    public SignedFileUrlResp getSignedReceiptPDFUrl(Instant createdAt, UUID customerId, UUID paymentId, String receiptPath) throws ShopServiceApiException {
        String yearPrefix = String.valueOf(createdAt.atZone(ZoneId.of(Constants.TIME_ZONE_BANGKOK)).getYear());
        String pdfPath = yearPrefix + "/" +
                customerId + "/"
                + paymentId + "/"
                + receiptPath;

        ZonedDateTime expirationDate = getExpDateWithTimeZone(Constants.TIME_ZONE_BANGKOK, getPdfFileExp);

        String signedPdfUrl = supabaseStorageService.getSignedFileUrl(Constants.SUPABASE_RECEIPTS_BUCKET_NAME, pdfPath, getPdfFileExp);

        SignedFileUrlResp signedFileUrlResp = new SignedFileUrlResp();
        signedFileUrlResp.setSignedFileUrl(signedPdfUrl);
        signedFileUrlResp.setExpiresAt(expirationDate);

        return signedFileUrlResp;
    }
}