package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.UserCheckTemp;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.datasource.domain.orders.IOrderByStatusResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderItemListResp;
import com.senior.candleShopProject.datasource.entities.OrdersEntity;
import com.senior.candleShopProject.datasource.entities.PaymentsEntity;
import com.senior.candleShopProject.datasource.entities.ShipmentEntity;
import com.senior.candleShopProject.datasource.repo.*;
import com.senior.candleShopProject.feature.order.controller.dto.request.TrackOrderReq;
import com.senior.candleShopProject.feature.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.TestComponent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestComponent
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock private UserCheckTemp userCheckTemp;
    @Mock private OrdersRepo ordersRepo;
    @Mock private OrderItemsRepo orderItemsRepo;
    @Mock private PaymentsRepo paymentsRepo;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getOrderByStatus_StatusNull_ThrowsBadRequest() {
        UUID userId = UUID.randomUUID();
        ShopBadRequestException ex = assertThrows(ShopBadRequestException.class, () ->
                orderService.getOrderByStatus(userId, null, 0, 10)
        );
        assertEquals(ResultCode.BAD_REQUEST, ex.getStatus());
        verifyNoInteractions(userCheckTemp, ordersRepo, orderItemsRepo);
    }

    @Test
    void getOrderByStatus_Success() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        String status = OrderStatus.ORDER_TO_SHIP.getStatusCode();
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        int page = 0;
        int size = 10;

        doNothing().when(userCheckTemp).checkExistsUser(userId);
        when(userCheckTemp.getCustomerIdByUserId(userId)).thenReturn(customerId);
        when(userCheckTemp.getSellerIdByUserId(userId)).thenReturn(null);

        IOrderByStatusResp orderRow = mock(IOrderByStatusResp.class);
        when(orderRow.getOrderId()).thenReturn(orderId);
        when(orderRow.getTotalQuantity()).thenReturn(1);
        when(orderRow.getTotalAmount()).thenReturn(BigDecimal.valueOf(100));
        when(orderRow.getNetAmount()).thenReturn(BigDecimal.valueOf(100));
        when(orderRow.getOrderStatus()).thenReturn(status);
        when(orderRow.getOrderNumber()).thenReturn("ORD-001");
        when(orderRow.getAddressLabel()).thenReturn("Home");
        when(orderRow.getTrackingNumber()).thenReturn("TRK1");
        when(orderRow.getDeliveryMethod()).thenReturn(Constants.SHIPPING_METHOD_STANDARD);
        when(orderRow.getRejectionReason()).thenReturn(null);

        when(ordersRepo.getOrderByCustIdStatus(eq(customerId), eq(status), eq(false), eq(size), eq(page * size)))
                .thenReturn(List.of(orderRow));

        IOrderItemListResp item = mock(IOrderItemListResp.class);
        when(item.getOrderId()).thenReturn(orderId);
        when(item.getOrderItemId()).thenReturn(UUID.randomUUID());
        when(item.getProductName()).thenReturn("Candle");
        when(item.getQuantity()).thenReturn(1);
        when(item.getPricePerUnit()).thenReturn(BigDecimal.valueOf(100));
        when(item.getSubTotal()).thenReturn(BigDecimal.valueOf(100));
        when(item.getProductImgPath()).thenReturn("/img.jpg");

        when(orderItemsRepo.getOrderItemByOrderIds(eq(List.of(orderId))))
                .thenReturn(List.of(item));

        GenericResponse resp = orderService.getOrderByStatus(userId, status, page, size);

        assertNotNull(resp);
        assertEquals(ResultCode.SUCCESS, resp.getStatus());
        assertNotNull(resp.getData());

        verify(userCheckTemp).checkExistsUser(userId);
        verify(userCheckTemp).getCustomerIdByUserId(userId);
        verify(ordersRepo).getOrderByCustIdStatus(customerId, status, false, size, page * size);
        verify(orderItemsRepo).getOrderItemByOrderIds(List.of(orderId));
    }

    @Test
    void getOrderDetailsByOrderId_OrderNotFound_Throws() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        doNothing().when(userCheckTemp).checkExistsUser(userId);
        when(userCheckTemp.isOwnerOfOrder(orderId, userId)).thenReturn(Boolean.TRUE);
        when(ordersRepo.getOrderDetailByOrderId(orderId)).thenReturn(null);

        ShopDataNotFoundException ex = assertThrows(ShopDataNotFoundException.class, () ->
                orderService.getOrderDetailsByOrderId(userId, orderId)
        );
        assertEquals(ResultCode.DATA_NOT_FOUND, ex.getStatus());

        verify(userCheckTemp).checkExistsUser(userId);
        verify(userCheckTemp).isOwnerOfOrder(userId, orderId);
        verify(ordersRepo).getOrderDetailByOrderId(orderId);
        verifyNoMoreInteractions(orderItemsRepo);
    }

    @Test
    void confirmPayment_PDtoTS_Success() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        doNothing().when(userCheckTemp).checkExistsUser(userId);

        OrdersEntity orderEntity = new OrdersEntity();
        orderEntity.setOrderId(orderId);
        when(ordersRepo.findById(orderId)).thenReturn(Optional.of(orderEntity));

        PaymentsEntity payment = new PaymentsEntity();
        payment.setPaymentStatus(OrderStatus.ORDER_PAYMENT_PENDING.getStatusCode());
        when(paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId)).thenReturn(payment);

        when(userCheckTemp.getSellerIdByUserId(userId)).thenReturn(UUID.randomUUID());

        when(ordersRepo.save(any(OrdersEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        GenericResponse resp = orderService.confirmPayment(userId, orderId);

        assertNotNull(resp);
        assertEquals(ResultCode.SUCCESS, resp.getStatus());
        verify(ordersRepo).save(any(OrdersEntity.class));
    }

    @Test
    void confirmPayment_InvalidTransition_ThrowsConflict() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        doNothing().when(userCheckTemp).checkExistsUser(userId);

        OrdersEntity orderEntity = new OrdersEntity();
        orderEntity.setOrderId(orderId);
        when(ordersRepo.findById(orderId)).thenReturn(Optional.of(orderEntity));

        PaymentsEntity payment = new PaymentsEntity();
        payment.setPaymentStatus(OrderStatus.ORDER_PAYMENT_APPROVED.getStatusCode());
        when(paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId)).thenReturn(payment);

        ShopConflictException ex = assertThrows(ShopConflictException.class, () ->
                orderService.confirmPayment(userId, orderId)
        );
        assertEquals(ResultCode.CONFLICT, ex.getStatus());

        verify(ordersRepo, never()).save(any());
    }

    @Test
    void trackOrder_EmptyTracking_ThrowsBadRequest() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        TrackOrderReq req = new TrackOrderReq();
        req.setTrackingNumber(new ArrayList<>());

        doNothing().when(userCheckTemp).checkExistsUser(userId);

        ShopBadRequestException ex = assertThrows(ShopBadRequestException.class, () ->
                orderService.trackOrder(userId, orderId, req)
        );
        assertEquals(ResultCode.BAD_REQUEST, ex.getStatus());

        verifyNoInteractions(ordersRepo);
    }

    @Test
    void trackOrder_Success() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        TrackOrderReq req = new TrackOrderReq();
        req.setTrackingNumber(List.of("TN1", "TN2"));

        doNothing().when(userCheckTemp).checkExistsUser(userId);
        when(userCheckTemp.getSellerIdByUserId(userId)).thenReturn(UUID.randomUUID());

        OrdersEntity orderEntity = new OrdersEntity();
        orderEntity.setOrderId(orderId);
        orderEntity.setOrderStatus(OrderStatus.ORDER_TO_SHIP.getStatusCode());
        when(ordersRepo.findById(orderId)).thenReturn(Optional.of(orderEntity));

        when(ordersRepo.save(any(OrdersEntity.class))).thenAnswer(inv -> {
            OrdersEntity saved = inv.getArgument(0);

            ShipmentEntity sh = saved.getShipmentEntity();
            if (sh != null) sh.setOrdersEntity(saved);
            saved.setStatusChangedAt(Instant.now());
            return saved;
        });

        GenericResponse resp = orderService.trackOrder(userId, orderId, req);

        assertNotNull(resp);
        assertEquals(ResultCode.SUCCESS, resp.getStatus());
        verify(ordersRepo).save(any(OrdersEntity.class));
    }
}
