package com.senior.candleShopProject.common.utils;

import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopBadRequestException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.utils.dto.RangeOfMonthResp;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class ReportUtils {
        public static RangeOfMonthResp getRangeOfMonthUTC(int month, int year) throws ShopServiceApiException {
            if ( month < 1 || month > 12 )
                throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Month must be between 1 and 12.");

            if ( year < 2026|| year > ZonedDateTime.now().getYear() )
                throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Year must be between 2026 and current year.");

            ZonedDateTime dateTimeNextMonthFirstDay = ZonedDateTime.of(
                    year , month, 1, 0, 0, 0, 0,
                    ZoneId.of(Constants.TIME_ZONE_BANGKOK)
            ).plusMonths(1);

            Instant queryInstantEnd = dateTimeNextMonthFirstDay.toInstant();
            Instant queryInstantStart = dateTimeNextMonthFirstDay.minusMonths(1).toInstant();

            RangeOfMonthResp resp = new RangeOfMonthResp();
            resp.setStartDate(queryInstantStart);
            resp.setEndDate(queryInstantEnd);

            return resp;
        }

        
}
