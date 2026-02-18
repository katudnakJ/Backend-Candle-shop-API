package com.senior.candleShopProject.service.auth;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.LineService.LineLoginService;
import com.senior.candleShopProject.common.LineService.dto.LineProfileResp;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.exception.ShopUnAuthorizedException;
import com.senior.candleShopProject.common.utils.CookieUtils;
import com.senior.candleShopProject.common.utils.JwtUtils;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
import com.senior.candleShopProject.datasource.entities.UsersEntity;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import com.senior.candleShopProject.feature.auth.service.LoginService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletResponse;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class LoginServiceTest {

    @InjectMocks
    private LoginService loginService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UsersRepo usersRepo;

    @Mock
    private LineLoginService lineLoginService;

    @Mock
    private IUsersResp userProfile;

    private MockHttpServletResponse servResp;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        servResp = new MockHttpServletResponse();

        Field field = CookieUtils.class.getDeclaredField("cookieExpirationTime");
        field.setAccessible(true);
        field.set(null, 1800);
    }

    @Test
    void userLogin_Success_ExistingUser() throws Exception {
        String authHeader = "valid_token";
        String lineUserId = "line-123";
        UUID userId = UUID.randomUUID();
        String role = "CUSTOMER";
        String rawToken = "jwt_token";

        LineProfileResp lineProfile = new LineProfileResp();
        lineProfile.setUserId(lineUserId);

        when(lineLoginService.getLineProfile(eq(authHeader))).thenReturn(lineProfile);
        when(usersRepo.getUserProfileByLineId(eq(lineUserId))).thenReturn(userProfile);
        when(userProfile.getUserId()).thenReturn(userId);
        when(userProfile.getUserRole()).thenReturn(role);
        when(jwtUtils.generateToken(userId, role)).thenReturn(rawToken);

        GenericResponse resp = loginService.userLogin(authHeader, servResp);

        assertNotNull(resp);
        assertEquals(ResultCode.SUCCESS, resp.getStatus());
        assertNotNull(resp.getData());
        verify(usersRepo, times(2)).getUserProfileByLineId(lineUserId);
        verify(jwtUtils).generateToken(userId, role);

        Cookie cookie = servResp.getCookie("access_token");
        assertNotNull(cookie);
        assertEquals(rawToken, cookie.getValue());
    }

    @Test
    void userLogin_Success_NewUser() throws Exception {
        String authHeader = "valid_token";
        String lineUserId = "line-456";
        UUID userId = UUID.randomUUID();
        String role = "CUSTOMER";
        String rawToken = "jwt_token";

        LineProfileResp lineProfile = new LineProfileResp();
        lineProfile.setUserId(lineUserId);

        // First call returns null (new user), second returns userProfile
        when(lineLoginService.getLineProfile(eq(authHeader))).thenReturn(lineProfile);
        when(usersRepo.getUserProfileByLineId(eq(lineUserId)))
                .thenReturn(null)
                .thenReturn(userProfile);
        when(userProfile.getUserId()).thenReturn(userId);
        when(userProfile.getUserRole()).thenReturn(role);
        when(jwtUtils.generateToken(userId, role)).thenReturn(rawToken);

        GenericResponse resp = loginService.userLogin(authHeader, servResp);

        assertNotNull(resp);
        assertEquals(ResultCode.SUCCESS, resp.getStatus());
        assertNotNull(resp.getData());
        verify(usersRepo).save(any(UsersEntity.class));

        Cookie cookie = servResp.getCookie("access_token");
        assertNotNull(cookie);
        assertEquals(rawToken, cookie.getValue());
    }

    @Test
    void userLogin_Fail_LineProfileError() throws Exception {
        String authHeader = "invalid_token";
        when(lineLoginService.getLineProfile(eq(authHeader)))
                .thenThrow(new RuntimeException("Line error"));

        ShopUnAuthorizedException ex = assertThrows(
                ShopUnAuthorizedException.class,
                () -> loginService.userLogin(authHeader, servResp)
        );
        assertEquals(ResultCode.UNAUTHORIZED, ex.getStatus());
    }

    @Test
    void userLogin_Fail_LineUserIdEmpty() throws ShopServiceApiException {
        String authHeader = "valid_token";

        LineProfileResp lineProfile = new LineProfileResp();
        lineProfile.setUserId(""); // หรือจะ set เป็น null ก็ได้แล้วแต่เคสที่อยากเช็ก

        when(lineLoginService.getLineProfile(eq(authHeader))).thenReturn(lineProfile);

        ShopServiceApiException ex = assertThrows(
                ShopServiceApiException.class,
                () -> loginService.userLogin(authHeader, servResp)
        );

        assertEquals(ResultCode.INTERNAL_SERVER_ERROR, ex.getStatus());
    }
}
