package com.senior.candleShopProject.common.interceptor;

import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopUnAuthorizedException;
import com.senior.candleShopProject.common.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);
    private final JwtUtils jwtUtils;

    public AuthInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
        public boolean preHandle (HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer "))
                throw new ShopUnAuthorizedException(ResultCode.TOKEN_INVALID, "กรุณา Login ก่อนใช้งาน");

            String token = authHeader.split("Bearer ")[1];

        if(jwtUtils.isTokenExpired(token)){
            log.info("token: {}", "token is expired");
            throw new ShopUnAuthorizedException(ResultCode.TOKEN_INVALID, "เซสชันหมดอายุ หรือไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
        }

            if(!jwtUtils.validateToken(token)){
                log.info("token: {}", "token is invalid");
                throw new ShopUnAuthorizedException(ResultCode.TOKEN_INVALID, "เซสชันหมดอายุ หรือไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
            }

            String userId = jwtUtils.getUserIdFromToken(token);

            if (userId == null){
                log.info("token: {}", "userId is null");
                throw new ShopUnAuthorizedException(ResultCode.TOKEN_INVALID, "เซสชันหมดอายุ หรือไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
            }
            request.setAttribute("userId", userId);
            return true;
        }
}
