package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.LineService.LineMessageService;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.SupabaseService.Dto.SignedFileUrlResp;
import com.senior.candleShopProject.common.UserCheckTemp;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.PaginationUtil;
import com.senior.candleShopProject.common.utils.SupabaseStorageUtils;
import com.senior.candleShopProject.common.utils.dto.PaginationBuildResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderByStatusResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderItemListResp;
import com.senior.candleShopProject.datasource.domain.orders.IReceiptInformationResp;
import com.senior.candleShopProject.datasource.domain.products.ProductByOrderIdResp;
import com.senior.candleShopProject.datasource.entities.*;
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
import java.time.ZonedDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestComponent
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock private PaginationUtil paginationUtil;
    @Mock private UserCheckTemp userCheckTemp;
    @Mock private SupabaseStorageUtils supabaseStorageUtils;
    @Mock private LineMessageService lineMessageService;

    @Mock private OrdersRepo ordersRepo;
    @Mock private OrderItemsRepo orderItemsRepo;
    @Mock private PaymentsRepo paymentsRepo;
    @Mock private ProductsRepo productsRepo;
    @Mock private UsersRepo usersRepo;


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getOrderByStatus_StatusNull_ThrowsBadRequest() {
        UUID userId = UUID.randomUUID();
        String userRole = "CUST";

        ShopBadRequestException ex = assertThrows(ShopBadRequestException.class, () ->
                orderService.getOrderByStatus(userId, userRole, null, 10, 0)
        );
    }

    @Test
    void getOrderByStatus_Success() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        String status = OrderStatus.ORDER_TO_SHIP.getStatusCode();
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        int page = 0;
        int size = 10;
        Instant mockNow = Instant.now();
        String userRole = "CUST";

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
        when(orderRow.getOrderCreatedAt()).thenReturn(mockNow);

        when(ordersRepo.getOrderByStatus(eq(customerId), eq(status), eq(false), eq(size), eq(page * size)))
                .thenReturn(List.of(orderRow));

        IOrderItemListResp item = mock(IOrderItemListResp.class);
        when(item.getOrderId()).thenReturn(orderId);
        when(item.getOrderItemId()).thenReturn(UUID.randomUUID());
        when(item.getProductName()).thenReturn("Candle");
        when(item.getQuantity()).thenReturn(1);
        when(item.getPricePerUnit()).thenReturn(BigDecimal.valueOf(100));
        when(item.getSubTotal()).thenReturn(BigDecimal.valueOf(100));
        when(item.getProductImagePath()).thenReturn("/img.jpg");

        when(orderItemsRepo.getOrderItemByOrderIds(eq(List.of(orderId))))
                .thenReturn(List.of(item));

        PaginationBuildResp paginationBuildResp = mock(PaginationBuildResp.class);

        when(paginationUtil.buildPaginationResp(anyInt(), anyInt(), anyLong())).thenReturn(paginationBuildResp);
        GenericResponse resp = orderService.getOrderByStatus(userId, userRole, status, page, size);

        assertNotNull(resp);
        assertNotNull(resp.getData());

        verify(userCheckTemp).checkExistsUser(userId);
        verify(userCheckTemp).getCustomerIdByUserId(userId);
        verify(ordersRepo).getOrderByStatus(customerId, status, false, size, page * size);
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

        verify(userCheckTemp).checkExistsUser(userId);
        verify(userCheckTemp).isOwnerOfOrder(userId, orderId);
        verify(ordersRepo).getOrderDetailByOrderId(orderId);
        verifyNoMoreInteractions(orderItemsRepo);
    }

    @Test
    void confirmPayment_PDtoTS_Success() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String userRole = "SELLER";
        boolean isSeller = true;

        doNothing().when(userCheckTemp).checkExistsUser(userId);

        OrdersEntity orderEntity = new OrdersEntity();
        orderEntity.setOrderId(orderId);
        when(ordersRepo.findById(orderId)).thenReturn(Optional.of(orderEntity));

        PaymentsEntity payment = new PaymentsEntity();
        payment.setPaymentStatus(OrderStatus.ORDER_PAYMENT_PENDING.getStatusCode());
        when(paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId)).thenReturn(payment);

        when(userCheckTemp.getSellerIdByUserId(userId)).thenReturn(UUID.randomUUID());

        when(ordersRepo.save(any(OrdersEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        PaginationBuildResp paginationBuildResp = mock(PaginationBuildResp.class);
        when(paginationUtil.buildPaginationResp(anyInt(), anyInt(), anyLong())).thenReturn(paginationBuildResp);
        when(productsRepo.getProductsByOrderId(eq(isSeller), eq(orderId))).thenReturn(List.of(mock(ProductByOrderIdResp.class)));

        GenericResponse resp = orderService.confirmPayment(userRole, userId, orderId);

        assertNotNull(resp);
        verify(ordersRepo).save(any(OrdersEntity.class));
    }

    @Test
    void confirmPayment_InvalidTransition_ThrowsConflict() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String userRole = "SELLER";

        doNothing().when(userCheckTemp).checkExistsUser(userId);

        OrdersEntity orderEntity = new OrdersEntity();
        orderEntity.setOrderId(orderId);
        when(ordersRepo.findById(orderId)).thenReturn(Optional.of(orderEntity));

        PaymentsEntity payment = new PaymentsEntity();
        payment.setPaymentStatus(OrderStatus.ORDER_PAYMENT_APPROVED.getStatusCode());
        when(paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId)).thenReturn(payment);

        ShopConflictException ex = assertThrows(ShopConflictException.class, () ->
                orderService.confirmPayment(userRole, userId, orderId)
        );

        verify(ordersRepo, never()).save(any());
    }

    @Test
    void trackOrder_EmptyTracking_ThrowsBadRequest() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String userRole = "SELLER";
        TrackOrderReq req = new TrackOrderReq();
        req.setTrackingNumber(new ArrayList<>());

        doNothing().when(userCheckTemp).checkExistsUser(userId);

        ShopBadRequestException ex = assertThrows(ShopBadRequestException.class, () ->
                orderService.trackOrder(userId, orderId, req)
        );

        verifyNoInteractions(ordersRepo);
    }

    @Test
    void trackOrder_Success() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String userRole = "SELLER";
        TrackOrderReq req = new TrackOrderReq();
        req.setTrackingNumber(List.of("TN1", "TN2"));

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

        UsersEntity user = mock(UsersEntity.class);
        when(user.getLineId()).thenReturn("MOCK_LINE_ID_12345");

        when(usersRepo.findByCustomersEntity_OrdersEntities_OrderId(orderId)).thenReturn(user);

        GenericResponse resp = orderService.trackOrder(userId, orderId, req);

        assertNotNull(resp);
        verify(ordersRepo).save(any(OrdersEntity.class));
    }

    @Test
    void getPaymentSlipByOrderId_Success() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        when(userCheckTemp.getCustomerIdByUserId(userId)).thenReturn(customerId);
        when(userCheckTemp.isOwnerOfOrder(userId, orderId)).thenReturn(true);

        PaymentsEntity payment = new PaymentsEntity();
        payment.setPaymentId(UUID.randomUUID());
        payment.setPaymentProofPath("proof.jpg");
        when(paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId)).thenReturn(payment);

        SignedFileUrlResp signed = mock(SignedFileUrlResp.class);
        when(supabaseStorageUtils.getSignedPaymentProofImage(eq(customerId), any(), anyString(), any())).thenReturn(signed);

        GenericResponse resp = orderService.getPaymentSlipByOrderId(userId, orderId);

        assertNotNull(resp);
        assertEquals(com.senior.candleShopProject.common.ResultCode.SUCCESS, resp.getStatus());
        verify(paymentsRepo).findPaymentsEntitiesByOrdersEntity_OrderId(orderId);
    }

    @Test
    void getPaymentSlipByOrderId_PaymentNotFound_Throws() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        when(userCheckTemp.getCustomerIdByUserId(userId)).thenReturn(customerId);
        when(userCheckTemp.isOwnerOfOrder(userId, orderId)).thenReturn(true);

        when(paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId)).thenReturn(null);

        ShopDataNotFoundException ex = assertThrows(ShopDataNotFoundException.class, () ->
                orderService.getPaymentSlipByOrderId(userId, orderId)
        );

        verify(paymentsRepo).findPaymentsEntitiesByOrdersEntity_OrderId(orderId);
    }

    @Test
    void confirmReceipt_Success() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        when(userCheckTemp.getCustomerIdByUserId(userId)).thenReturn(customerId);

        OrdersEntity order = new OrdersEntity();
        order.setOrderId(orderId);
        CustomersEntity cust = new CustomersEntity();
        cust.setCustomerId(customerId);
        order.setCustomersEntity(cust);
        order.setOrderStatus(OrderStatus.ORDER_TO_RECIEVE.getStatusCode());

        when(ordersRepo.findById(orderId)).thenReturn(Optional.of(order));
        when(ordersRepo.save(any(OrdersEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        GenericResponse resp = orderService.confirmReceipt(userId, orderId);

        assertNotNull(resp);
        assertEquals(com.senior.candleShopProject.common.ResultCode.SUCCESS, resp.getStatus());
        verify(ordersRepo).save(any(OrdersEntity.class));
    }

    @Test
    void confirmReceipt_Forbidden_Throws() throws ShopServiceApiException {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        when(userCheckTemp.getCustomerIdByUserId(userId)).thenReturn(customerId);

        OrdersEntity order = new OrdersEntity();
        order.setOrderId(orderId);
        CustomersEntity cust = new CustomersEntity();
        cust.setCustomerId(UUID.randomUUID()); // different id
        order.setCustomersEntity(cust);

        when(ordersRepo.findById(orderId)).thenReturn(Optional.of(order));

        ShopForbiddenException ex = assertThrows(ShopForbiddenException.class, () ->
                orderService.confirmReceipt(userId, orderId)
        );

        verify(ordersRepo, never()).save(any());
    }

    @Test
    void generateReceiptToPDF_Success() throws Exception {
        String userRole = "SELLER";
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PaymentsEntity payment = new PaymentsEntity();
        payment.setPaymentStatus(OrderStatus.ORDER_PAYMENT_APPROVED.getStatusCode());
        payment.setReceiptPath("receipt.pdf");
        payment.setPaymentId(UUID.randomUUID());
        payment.setCreatedAt(Instant.now());

        when(paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId)).thenReturn(payment);

        IReceiptInformationResp receiptInfo = mock(IReceiptInformationResp.class);
        when(receiptInfo.getOrderStatus()).thenReturn(OrderStatus.ORDER_TO_SHIP.getStatusCode());
        when(ordersRepo.getReceiptInformationByOrderId(true, userId, orderId)).thenReturn(receiptInfo);

        SignedFileUrlResp signed = mock(SignedFileUrlResp.class);

        when(signed.getExpiresAt()).thenReturn(ZonedDateTime.now());
        when(signed.getSignedFileUrl()).thenReturn("https://test.com/receipt.pdf"); // เผื่อใช้ด้วย

        when(supabaseStorageUtils.getSignedReceiptPDFUrl(any(), any(), any(), anyString()))
                .thenReturn(signed);

        GenericResponse resp = orderService.generateReceiptToPDF(userRole, userId, orderId);

        assertNotNull(resp);
        assertEquals(com.senior.candleShopProject.common.ResultCode.SUCCESS, resp.getStatus());
    }

    @Test
    void generateReceiptToPDF_InvalidState_Throws() throws Exception {
        String userRole = "CUST";
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        PaymentsEntity payment = new PaymentsEntity();
        payment.setPaymentStatus(OrderStatus.ORDER_PAYMENT_PENDING.getStatusCode());
        when(paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId)).thenReturn(payment);

        ShopConflictException ex = assertThrows(ShopConflictException.class, () ->
                orderService.generateReceiptToPDF(userRole, userId, orderId)
        );

        verify(ordersRepo, never()).getReceiptInformationByOrderId(anyBoolean(), any(), any());
    }
}
