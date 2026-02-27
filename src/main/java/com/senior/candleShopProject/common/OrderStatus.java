package com.senior.candleShopProject.common;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum OrderStatus {
    ORDER_PAYMENT_PENDING("PD","PAYMENT PENDING","รอยืนยันชำระเงิน"),
    ORDER_PAYMENT_REJECTED("RJ","PAYMENT REJECTED","การชำระเงินถูกปฏิเสธ"),
    ORDER_PAYMENT_APPROVED("AP","PAYMENT APPROVED","การชำระเงินได้รับการอนุมัติเรียบร้อย"),
    ORDER_TO_SHIP("TS","TO SHIP","ที่ต้องจัดส่ง"),
    ORDER_TO_RECIEVE("TR","TO RECEIVE","ที่ต้องได้รับ"),
    ORDER_COMPLETED("CP","COMPLETED","คำสั่งซื้อเสร็จสมบูรณ์"),;

    private final String status;
    private final String statusCode;
    private final String message;

    OrderStatus(String statusCode, String status, String message) {
        this.statusCode = statusCode;
        this.status = status;
        this.message = message;
    }

    public static boolean isValidStatus(String status) {
        return Arrays.stream(values())
                .anyMatch(v -> v.getStatusCode().equalsIgnoreCase(status));
    }

    public static boolean validToChangeStatus(String currentStatus, String newStatus) {
        if (currentStatus.equalsIgnoreCase( ORDER_PAYMENT_PENDING.getStatusCode()) )
            return (
                    newStatus.equalsIgnoreCase( ORDER_TO_SHIP.getStatusCode())
                    || newStatus.equalsIgnoreCase( ORDER_PAYMENT_REJECTED.getStatusCode())
            );
        else if (currentStatus.equalsIgnoreCase( ORDER_TO_SHIP.getStatusCode()) )
                return newStatus.equalsIgnoreCase( ORDER_TO_RECIEVE.getStatusCode());
        else if (currentStatus.equalsIgnoreCase( ORDER_TO_RECIEVE.getStatusCode()) )
                return newStatus.equalsIgnoreCase( ORDER_COMPLETED.getStatusCode());
        else if (currentStatus.equalsIgnoreCase( ORDER_PAYMENT_REJECTED.getStatusCode()) )
                return newStatus.equalsIgnoreCase( ORDER_PAYMENT_PENDING.getStatusCode());
        else return false;
    }

    public static OrderStatus getStatusRespByStatusCode(String orderStatus) {
        return Arrays.stream(values())
                .filter(v -> v.getStatusCode().equalsIgnoreCase(orderStatus))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid order status code: " + orderStatus));
    }
}
