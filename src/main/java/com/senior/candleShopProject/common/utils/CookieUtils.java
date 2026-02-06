package com.senior.candleShopProject.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {

    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";

    @Value("${JWT_ACCESS_EXPIRATION_TIME}")
    private static Integer cookieExpirationTime;

    public static void addAccessTokenToCookie(HttpServletResponse response, String accessToken) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(cookieExpirationTime);

        response.addCookie(cookie);
    }

    public static String getAccessTokenByCookie(HttpServletRequest request) {
        if (request.getCookies() != null){
            for (Cookie cookie : request.getCookies()) {
                if(ACCESS_TOKEN_COOKIE_NAME.equals(cookie.getName()))
                    return cookie.getValue();
            }
        }
        return null;
    }

    public static void clearAccessTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);
    }
}
