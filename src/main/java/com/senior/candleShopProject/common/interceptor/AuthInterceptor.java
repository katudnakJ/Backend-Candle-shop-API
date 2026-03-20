package com.senior.candleShopProject.common.interceptor;

import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopUnAuthorizedException;
import com.senior.candleShopProject.common.utils.CookieUtils;
import com.senior.candleShopProject.common.utils.JwtUtils;
import io.netty.handler.codec.http.HttpMethod;
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

//      CORS Preflight Request Handling
        if(HttpMethod.OPTIONS.name().equals(request.getMethod())){
            return true;
        }

        String token = CookieUtils.getAccessTokenByCookie(request);

        if(token == null){
            log.info("token : {}"," token is null");
            throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "ไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
        }

        if(jwtUtils.isTokenExpired(token)){
            log.info("token: {}", "token is expired");
            throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "ไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
        }

            if(!jwtUtils.validateToken(token)){
                log.info("token: {}", "token is invalid");
                throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "ไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
            }

            String userId = jwtUtils.getUserIdFromToken(token);

            if (userId == null){
                log.info("token: {}", "userId is null");
                throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "ไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
            }

            String userRole = jwtUtils.getUserRoleFromToken(token);
            if(userRole == null){
                log.info("token: {}", "userRole is null");
                throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "ไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
            }

            boolean isOwner = jwtUtils.getIsOwnerFromToken(token);

            request.setAttribute("userId", userId);
            request.setAttribute("userRole", userRole);
            request.setAttribute("isOwner", isOwner);

            return true;
        }
}
