package com.senior.candleShopProject.common.utils;

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
}
