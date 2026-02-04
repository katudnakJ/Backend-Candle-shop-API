package com.senior.candleShopProject.common;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum OrderStatus {
    ORDER_PAYMENT_PENDING("PAYMENT PENDING","PD","รอยืนยันชำระเงิน"),
    ORDER_PAYMENT_REJECTED("PAYMENT REJECTED","RJ","การชำระเงินถูกปฏิเสธ"),
    ORDER_TO_SHIP("TO SHIP","TS","ที่ต้องจัดส่ง"),
    ORDER_TO_RECIEVE("TO RECEIVE","TR","ที่ต้องได้รับ"),
    ORDER_COMPLETED("COMPLETED","CP","คำสั่งซื้อเสร็จสมบูรณ์"),;

    private final String orderStatus;
    private final String orderStatusCode;
    private final String message;

    OrderStatus(String orderStatus,String orderStatusCode, String message) {
        this.orderStatus = orderStatus;
        this.orderStatusCode = orderStatusCode;
        this.message = message;
    }

    public static boolean isValidStatus(String status) {
        return Arrays.stream(values())
                .anyMatch(v -> v.getOrderStatus().equalsIgnoreCase(status));
    }

}
