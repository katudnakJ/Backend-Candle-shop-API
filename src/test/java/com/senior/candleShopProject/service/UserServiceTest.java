package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
import com.senior.candleShopProject.datasource.repo.AddressesRepo;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.datasource.repo.SellerRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import com.senior.candleShopProject.feature.user.controller.dto.response.UserCustomerProfileResp;
import com.senior.candleShopProject.feature.user.controller.dto.response.UserSellerProfileResp;
import com.senior.candleShopProject.feature.user.service.UserService;
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
public class UserServiceTest {
    @InjectMocks
    private UserService userService;

    @Mock
    private UsersRepo usersRepo;

    @Mock
    private OrdersRepo ordersRepo;

    @Mock
    private SellerRepo sellerRepo;

    @Mock
    private AddressesRepo addressesRepo;

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

        when(sellerRepo.getBankQrPaymentImgPathByUserId(userId)).thenReturn(anyString());
        when(addressesRepo.findAddressesByUsersId(userId)).thenReturn(anyList());

        GenericResponse response = userService.getUserProfile(userId);

        assertNotNull(response);
        assertEquals(ResultCode.SUCCESS, response.getStatus());
        assertInstanceOf(UserSellerProfileResp.class, response.getData());

        verify(usersRepo, times(1)).getUserProfile(userId);
    }

    @Test
    public void testGetUserProfile_User_DataNotFound() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();

        IUsersResp userResp = mock(IUsersResp.class);
        when(usersRepo.getUserProfile(userId)).thenReturn(null);

        ShopDataNotFoundException ex = assertThrows(ShopDataNotFoundException.class,
                () -> userService.getUserProfile(userId));

        assertEquals(ResultCode.DATA_NOT_FOUND,ex.getStatus());
    }

    @Test
    public void testGetUserProfile_Customer_Success() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();

        IUsersResp userResp = mock(IUsersResp.class);
        when(usersRepo.getUserProfile(userId)).thenReturn(userResp);
        when(userResp.getIsSeller()).thenReturn(false);

        when(addressesRepo.findAddressesByUsersId(userId)).thenReturn(anyList());

        GenericResponse response = userService.getUserProfile(userId);

        assertNotNull(response);
        assertEquals(ResultCode.SUCCESS, response.getStatus());
        assertInstanceOf(UserCustomerProfileResp.class, response.getData());

        verify(usersRepo, times(1)).getUserProfile(userId);
    }

}
