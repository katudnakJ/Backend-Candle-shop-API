package com.senior.candleShopProject.common.utils;

import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopBadRequestException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class RunningNumberGenerator {

    private OrdersRepo ordersRepo;

//    format ORD20260217-0001 With Thai Date
    public String generateRunningNumber(String type) throws ShopServiceApiException {
        String prefix;

        if (type.equalsIgnoreCase(Constants.PREFIX_ORDER_NO))
            prefix = Constants.PREFIX_ORDER_NO;
        else if (type.equalsIgnoreCase(Constants.PREFIX_RECEIPT_NO))
            prefix = Constants.PREFIX_RECEIPT_NO;
        else
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST,"Invalid type for running number generation");

        ZonedDateTime thaiDateTime = LocalDateTimeUtils.getDateNowWithTimeZone(Constants.TIME_ZONE_BANGKOK);

        Long orderNoSeq = ordersRepo.getNextOrderNo();
        return String.format("%s%d%02d%02d-%04d", prefix
                        + thaiDateTime.getYear()
                        + thaiDateTime.getMonthValue()
                        + thaiDateTime.getDayOfMonth()
                        + "-"
                        + orderNoSeq
        );
    }
}
