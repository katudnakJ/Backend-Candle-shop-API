package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopInvalidParamException;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.feature.user.service.SellerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.TestComponent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestComponent
public class SellerServiceTest {
    @InjectMocks
    private SellerService sellerService;

    @Mock
    private OrdersRepo ordersRepo;

    @BeforeEach
    void initTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetSellerOrderCountByStatus_InvalidStatus_ThrowsException() {
        // Given
        String invalidStatus = "invalid_status";

        // When & Then
        assertThrows(ShopInvalidParamException.class, () -> {
            sellerService.getSellerOrderCountByStatus(invalidStatus);
        });

        verify(ordersRepo, never()).getCountOrdersWithStatus(any());
    }

}
