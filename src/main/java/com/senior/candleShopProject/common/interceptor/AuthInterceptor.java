package com.senior.candleShopProject.common.interceptor;

import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopUnAuthorizedException;
import com.senior.candleShopProject.common.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

        private final JwtUtils jwtUtils;

    public AuthInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
        public boolean preHandle (HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer "))
                throw new ShopUnAuthorizedException(ResultCode.TOKEN_HEADER, "กรุณา Login ก่อนใช้งาน");

            String token = authHeader.split("Bearer ")[1];

            if(!jwtUtils.validateToken(token))
                throw new ShopUnAuthorizedException(ResultCode.TOKEN_INVALID, "ไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่");

            if(jwtUtils.isTokenExpired(token))
                throw new ShopUnAuthorizedException(ResultCode.TOKEN_EXPIRED, "เซสชันหมดอายุ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");

            String userId = jwtUtils.getUserIdFromToken(token);

            if (userId == null)
                throw new ShopUnAuthorizedException(ResultCode.TOKEN_INVALID, "ไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่");

            request.setAttribute("userId", userId);
            return true;
        }
}
