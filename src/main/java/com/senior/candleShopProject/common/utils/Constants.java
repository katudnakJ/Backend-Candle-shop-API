package com.senior.candleShopProject.common.utils;

import lombok.Data;

@Data
public class Constants {
    public static final String LINE_BASE_URL = "https://api.line.me";
    public static final String LINE_TOKEN_VERIFY_URL = "/oauth2/v2.1/verify";
    public static final String LINE_TOKEN_GET_PROFILE_URL = "/v2/profile";

//    Role
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SELLER = "SELLER";
    public static final String ROLE_CUSTOMER = "CUST";

//    Database
    public static final String DB_SEQUENCE_ORDER_NAME = "order_no_seq";

//    Running Number Prefix
    public static final String PREFIX_ORDER_NO = "ORD";
    public static final String PREFIX_RECEIPT_NO = "REC";

}
