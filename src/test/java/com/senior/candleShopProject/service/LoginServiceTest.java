package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.LineService.LineLoginService;
import com.senior.candleShopProject.common.LineService.dto.LineProfileResp;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopInvalidParamException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.exception.ShopUnAuthorizedException;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.JwtUtils;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
import com.senior.candleShopProject.datasource.entities.UsersEntity;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import com.senior.candleShopProject.feature.auth.controller.dto.UserLoginResponse;
import com.senior.candleShopProject.feature.auth.service.LoginService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class LoginServiceTest {

    @InjectMocks
    private LoginService loginService;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UsersRepo usersRepo;

    @Mock
    private IUsersResp userProfile;

    @Mock
    private LineLoginService lineLoginService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        GenericResponse resp = loginService.userLogin(authHeader);

        assertNotNull(resp);
        assertEquals(ResultCode.SUCCESS, resp.getStatus());
        // token is prefixed in service
        assertTrue(resp.getData() != null);
        String actual = ((UserLoginResponse) resp.getData()).getToken();
        assertEquals(Constants.TOKEN_PREFIX + rawToken, actual);
    }

    @Test
    void userLogin_Success_NewUser() throws Exception {
        String authHeader = "valid_token";
        String lineUserId = "line-456";
        UUID userId = UUID.randomUUID();
        String role = "CUSTOMER";
        String rawToken = "jwt_token_new";

        LineProfileResp lineProfile = new LineProfileResp();
        lineProfile.setUserId(lineUserId);

        // first call -> null (new user), second call -> userProfile (after save)
        when(lineLoginService.getLineProfile(eq(authHeader))).thenReturn(lineProfile);
        when(usersRepo.getUserProfileByLineId(eq(lineUserId))).thenReturn(null).thenReturn(userProfile);
        when(usersRepo.save(any(UsersEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userProfile.getUserId()).thenReturn(userId);
        when(userProfile.getUserRole()).thenReturn(role);
        when(jwtUtils.generateToken(userId, role)).thenReturn(rawToken);

        GenericResponse resp = loginService.userLogin(authHeader);

        assertNotNull(resp);
        assertEquals(ResultCode.SUCCESS, resp.getStatus());
        String actual = ((com.senior.candleShopProject.feature.auth.controller.dto.UserLoginResponse) resp.getData()).getToken();
        assertEquals(Constants.TOKEN_PREFIX + rawToken, actual);
    }

    @Test
    void userLogin_lineServiceThrows_unauthorized() throws ShopServiceApiException {
        String authHeader = "Bearer bad_token";

        when(lineLoginService.getLineProfile(eq(authHeader)))
                .thenThrow(new ShopUnAuthorizedException(ResultCode.TOKEN_INVALID));

        ShopUnAuthorizedException exception = assertThrows(
                ShopUnAuthorizedException.class, () ->
           lineLoginService.getLineProfile(authHeader)
        );

        assertEquals(ResultCode.TOKEN_INVALID, exception.getStatus());

    }

    @Test
    void userLogin_ThrowsException_InvalidHeaderFormat() throws ShopServiceApiException {
        // Given
        String invalidHeader = "InvalidFormat token123";

        when(lineLoginService.getLineProfile(eq(invalidHeader)))
                .thenThrow(new ShopUnAuthorizedException(ResultCode.TOKEN_INVALID));

        // When & Then
        ShopUnAuthorizedException exception = assertThrows(
                ShopUnAuthorizedException.class,
                () -> loginService.userLogin(invalidHeader)
        );

        assertEquals(ResultCode.UNAUTHORIZED, exception.getStatus());
    }

    @Test
    void userLogin_ThrowsException_MissingBearerPrefix() throws Exception {
        // Given
        String tokenWithoutBearer = "sometoken123";

        when(lineLoginService.getLineProfile(eq(tokenWithoutBearer)))
                .thenThrow(new ShopUnAuthorizedException(ResultCode.TOKEN_INVALID));

        // When & Then
        ShopUnAuthorizedException exception = assertThrows(
                ShopUnAuthorizedException.class,
                () -> loginService.userLogin(tokenWithoutBearer)
        );

        assertEquals(ResultCode.UNAUTHORIZED, exception.getStatus());
    }

}
