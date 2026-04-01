package com.senior.candleShopProject.common.utils;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Constants {
    public static final String LINE_BASE_URL = "https://api.line.me";
    public static final String LINE_TOKEN_VERIFY_URL = "/oauth2/v2.1/verify";
    public static final String LINE_TOKEN_GET_PROFILE_URL = "/v2/profile";

//    Role
    public static final String ROLE_DEVELOPER = "DEVELOPER";
    public static final String ROLE_SELLER = "SELLER";
    public static final String ROLE_CUSTOMER = "CUST";

//    Database
    public static final String DB_SEQUENCE_ORDER_NAME = "order_no_seq";
    public static final String DB_SEQUENCE_PAYMENT_NAME = "receipt_no_seq";

//    Running Number Prefix
    public static final String PREFIX_ORDER_NO = "ORD";
    public static final String PREFIX_RECEIPT_NO = "REC";

//    Supabase Storage Name
    public static final String SUPABASE_QR_PAYMENT_BUCKET_NAME = "qr-payment";
    public static final String SUPABASE_RECEIPT_BUCKET_NAME = "payment-proofs";
    public static final String SUPABASE_PRODUCT_BUCKET_NAME = "attachments";
    public static final String SUPABASE_RECEIPTS_BUCKET_NAME = "receipts";

    //    return key name
    public static final String RESPONSE_KEY_EXP_DATE = "expirationDate";
    public static final String RESPONSE_KEY_SIGNED_IMAGE = "signedImageUrl";

//  Date
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_ZONE_BANGKOK = "Asia/Bangkok";
    public static final String TIME_ZONE_UTC = "UTC";

//    Content type
    public static final String CONTENT_TYPE_JPEG = "image/jpeg";
    public static final String CONTENT_TYPE_PDF = "application/pdf";

//    Shipping
    public static final int SHIPPING_ITEMS_PER_BOX = 100;
    public static final BigDecimal SHIPPING_PRICE_NOT_MORE_THAN_10 = new BigDecimal("50.00");
    public static final BigDecimal SHIPPING_PRICE_MORE_THAN_10 = new BigDecimal("120.00");

//    Shipping Method
    public static final String SHIPPING_METHOD_STANDARD = "STANDARD";
    public static final String SHIPPING_METHOD_YIPPEE = "YIPPEE";

//    PDF
    public static final String PDF_FILE_NAME_PREFIX = "receipt_";
    public static final String PDF_SIGN_NAME = "Moji's Candle Shop";
    public static final String PDF_SIGN_LOCATION = "Thailand";
    public static final String PDF_SIGN_REASON = "Invoice Digital Signature";

//  Trend for report
    public static final String TREND_INCREASE = "INCREASE";
    public static final String TREND_DECREASE = "DECREASE";
    public static final String TREND_STABLE = "STABLE";
}
