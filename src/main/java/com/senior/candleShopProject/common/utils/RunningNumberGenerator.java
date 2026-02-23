package com.senior.candleShopProject.common.utils;

import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopBadRequestException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.datasource.repo.PaymentsRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class RunningNumberGenerator {

    private final OrdersRepo ordersRepo;
    private final PaymentsRepo paymentRepo;


//    format ORD20260217-001 With Thai Date
    public String generateOrderRunningNumber(String type) throws ShopServiceApiException {
        String prefix;
        Long orderNoSeq;
        if (type.equalsIgnoreCase(Constants.PREFIX_ORDER_NO)){
            prefix = Constants.PREFIX_ORDER_NO;
            orderNoSeq = ordersRepo.getNextOrderNo();
        }else if (type.equalsIgnoreCase(Constants.PREFIX_RECEIPT_NO)){
            prefix = Constants.PREFIX_RECEIPT_NO;
            orderNoSeq = paymentRepo.getNextReceiptNo();
        }else
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST,"Invalid type for running number generation");

        ZonedDateTime thaiDateTime = LocalDateTimeUtils.getDateNowWithTimeZone(Constants.TIME_ZONE_BANGKOK);


        return String.format("%s%d%02d%02d-%04d", prefix
                        ,thaiDateTime.getYear()
                        ,thaiDateTime.getMonthValue()
                        ,thaiDateTime.getDayOfMonth()
                        ,orderNoSeq
        );
    }
}
