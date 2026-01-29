package com.senior.candleShopProject.common.utils;

import com.senior.candleShopProject.common.OrderStatus;
import lombok.Data;

@Data
public class Constants {
    public static final String LINE_TOKEN_VERIFY_URL = "https://api.line.me/oauth2/v2.1/verify/";

//    Role
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_SELLER = "SELLER";
    public static final String ROLE_CUSTOMER = "CUST";
}
