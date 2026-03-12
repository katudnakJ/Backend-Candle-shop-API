package com.senior.candleShopProject.common.utils;

import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZonedDateTime;

@NoArgsConstructor
public class DateTimeFormatter {
        public static String formatDateTimeNowToString(String pattern, String dateTime) {
            return LocalDateTimeUtils.getDateNowWithTimeZone(dateTime).format(java.time.format.DateTimeFormatter.ofPattern(pattern));
        }
}
