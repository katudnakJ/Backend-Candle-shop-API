package com.senior.candleShopProject.common.filter;

import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopUnAuthorizedException;
import com.senior.candleShopProject.common.filter.dto.UserPrincipalDto;
import com.senior.candleShopProject.common.utils.CookieUtils;
import com.senior.candleShopProject.common.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);
    private final JwtUtils jwtUtils;

    public AuthFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //      CORS Preflight Request Handling
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        try{

            String token = CookieUtils.getAccessTokenByCookie(request);

            if(token == null){
                log.warn("token : {}"," token is null");
                unauthorized(response);
            }

            if(jwtUtils.isTokenExpired(token)){
                log.warn("token: {}", "token is expired");
                throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "ไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
            }

            if(!jwtUtils.validateToken(token)){
                log.warn("token: {}", "token is invalid");
                throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "ไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
            }

            String userId = jwtUtils.getUserIdFromToken(token);

            if (userId == null){
                log.warn("token: {}", "userId is null");
                throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "ไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
            }

            String userRole = jwtUtils.getUserRoleFromToken(token);
            if(userRole == null){
                log.warn("token: {}", "userRole is null");
                throw new ShopUnAuthorizedException(ResultCode.UNAUTHORIZED, "ไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");
            }

            boolean isOwner = jwtUtils.getIsOwnerFromToken(token);

            String role = userRole.startsWith("ROLE_") ? userRole : "ROLE_" + userRole;

            var principal = new UserPrincipalDto(userId, role);

            var auth = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            request.setAttribute("userId", userId);
            request.setAttribute("userRole", userRole);
            request.setAttribute("isOwner", isOwner);

            filterChain.doFilter(request, response);

        }catch (Exception e){
            log.error("Unauthorized: exception={}, path={}", e.getMessage(), request.getRequestURI());
            unauthorized(response);
        }
    }

    private void unauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("""
            {
              "code": "UNAUTHORIZED",
              "message": "ไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง"
            }
        """);
    }
}
