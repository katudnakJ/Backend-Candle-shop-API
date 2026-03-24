package com.senior.candleShopProject.common.utils;

import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

@NoArgsConstructor
public class LocalDateTimeUtils {
    public static ZonedDateTime getDateNowWithTimeZone(String toTimeZone) {
        Instant dateUtcNow = Instant.now();
        return dateUtcNow.atZone(ZoneId.of(toTimeZone));
    }

    public static ZonedDateTime getExpDateWithTimeZone(String toTimeZone,int expiresInSeconds) {
        Instant dateUtcNow = Instant.now()
                .plusSeconds(expiresInSeconds);
        return dateUtcNow.atZone(ZoneId.of(toTimeZone));
    }

    public static ZonedDateTime convertToTimeZone(ZonedDateTime dateTime, String toTimeZone) {
        return dateTime.withZoneSameInstant(ZoneId.of(toTimeZone));
    }

    public static ZonedDateTime convertInstantToTimeZone(Instant dateTime, String toTimeZone) {
        return dateTime.atZone(ZoneId.of(toTimeZone));
    }
}
