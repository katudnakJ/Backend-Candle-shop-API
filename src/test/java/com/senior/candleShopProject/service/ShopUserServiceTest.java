package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import com.senior.candleShopProject.feature.user.service.ShopUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.TestComponent;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestComponent
public class ShopUserServiceTest {
    @InjectMocks
    private ShopUserService shopUserService;

    @Mock
    private UsersRepo usersRepo;

    @Mock
    private OrdersRepo ordersRepo;

    @BeforeEach
    void initTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetUserProfile_Seller_Success() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();

        IUsersResp userResp = mock(IUsersResp.class);
        when(usersRepo.getUserProfile(userId)).thenReturn(userResp);
        when(userResp.getIsSeller()).thenReturn(true);

        when(ordersRepo.getCountOrdersWithStatusPD()).thenReturn(5);

        GenericResponse response = shopUserService.getUserProfile(userId);

        assertNotNull(response);
        assertEquals(response.getStatus(), ResultCode.SUCCESS);

        verify(usersRepo, times(1)).getUserProfile(userId);
        verify(ordersRepo, times(1)).getCountOrdersWithStatusPD();
    }

    @Test
    public void testGetUserProfile_User_DataNotFound() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();

        IUsersResp userResp = mock(IUsersResp.class);
        when(usersRepo.getUserProfile(userId)).thenReturn(null);

        ShopDataNotFoundException ex = assertThrows(ShopDataNotFoundException.class,
                () -> shopUserService.getUserProfile(userId));

        assertEquals(ResultCode.DATA_NOT_FOUND,ex.getStatus());
    }

    @Test
    public void testGetUserProfile_Customer_Success() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();

        IUsersResp userResp = mock(IUsersResp.class);
        when(usersRepo.getUserProfile(userId)).thenReturn(userResp);
        when(userResp.getIsSeller()).thenReturn(true);

        when(ordersRepo.getCountOrdersWithStatusPD()).thenReturn(5);

        GenericResponse response = shopUserService.getUserProfile(userId);

        assertNotNull(response);
        assertEquals(response.getStatus(), ResultCode.SUCCESS);

        verify(usersRepo, times(1)).getUserProfile(userId);
        verify(ordersRepo, times(1)).getCountOrdersWithStatusPD();
    }

}
