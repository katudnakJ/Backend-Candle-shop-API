package com.senior.candleShopProject.common.utils;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class CustomizeResponseUtil {
    public static <T> Map<String, Object> ReturnKeyValueWhenComplete(String key, T value) {
        Map<String, Object> result = new HashMap<>();
        result.put(key, value);
        return result;
    }

    public static <T> Map<String, Object> ReturnKeyValueWhenComplete(T value) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", value);
        return result;
    }

    public static Map<String, Object> ReturnBodyWithCount(Integer count, Object data, String dataObjectName) {
        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", count);
        result.put(dataObjectName, data);
        return result;
    }

    public static Map<String, Object> ReturnSignedImageWithExp(String signedImageUrl, ZonedDateTime expirationDate) {
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.RESPONSE_KEY_SIGNED_IMAGE, signedImageUrl);
        result.put(Constants.RESPONSE_KEY_EXP_DATE, expirationDate);
        return result;
    }
}
