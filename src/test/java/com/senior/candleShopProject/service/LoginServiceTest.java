package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopInvalidParamException;
import com.senior.candleShopProject.common.utils.JwtUtils;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class LoginServiceTest {
    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UsersRepo usersRepo;

    @Mock
    private IUsersResp userProfile;

    @InjectMocks
    private LoginService loginService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void userLogin_Success_ExistingUser() throws Exception {
        // Given
        String lineToken = "valid_token";
        UUID userId = UUID.randomUUID();
        String userRole = "CUSTOMER";
        String expectedToken = "jwt_token";

        when(userProfile.getUserId()).thenReturn(userId);
        when(userProfile.getUserRole()).thenReturn(userRole);
        when(usersRepo.getUserProfileByLineId(anyString())).thenReturn(userProfile);
        when(jwtUtils.generateToken(userId, userRole)).thenReturn(expectedToken);

        // When
        GenericResponse response = loginService.userLogin(lineToken);

        // Then
        assertNotNull(response);
        assertEquals(ResultCode.SUCCESS, response.getStatus());

        UserLoginResponse data = (UserLoginResponse) response.getData();
        assertEquals(expectedToken, data.getToken());
    }

    @Test
    void userLogin_Success_NewUser() throws Exception {
        // Given
        String lineToken = "valid_token";
        UUID userId = UUID.randomUUID();
        String userRole = "CUSTOMER";
        String expectedToken = "jwt_token";

        when(usersRepo.getUserProfileByLineId(anyString()))
                .thenReturn(null)
                .thenReturn(userProfile);
        when(usersRepo.save(any())).thenReturn(null);
        when(userProfile.getUserId()).thenReturn(userId);
        when(userProfile.getUserRole()).thenReturn(userRole);
        when(jwtUtils.generateToken(userId, userRole)).thenReturn(expectedToken);

        // When
        GenericResponse response = loginService.userLogin(lineToken);

        // Then
        assertNotNull(response);
        assertEquals(ResultCode.SUCCESS, response.getStatus());

        UserLoginResponse data = (UserLoginResponse) response.getData();
        assertEquals(expectedToken, data.getToken());
    }

    @Test
    void userLogin_ThrowsException_EmptyToken() {
        // Given
        String emptyToken = "";

        // When & Then
        ShopInvalidParamException exception = assertThrows(
                ShopInvalidParamException.class,
                () -> loginService.userLogin(emptyToken)
        );

        assertEquals(ResultCode.INVALID_PARAMS, exception.getStatus());
        assertEquals("Line token is required.", exception.getMessage());
    }

    @Test
    void userLogin_ThrowsException_NullToken() {
        // Given
        String nullToken = null;

        // When & Then
        ShopInvalidParamException exception = assertThrows(
                ShopInvalidParamException.class,
                () -> loginService.userLogin(nullToken)
        );

        assertEquals(ResultCode.INVALID_PARAMS, exception.getStatus());
    }
}
