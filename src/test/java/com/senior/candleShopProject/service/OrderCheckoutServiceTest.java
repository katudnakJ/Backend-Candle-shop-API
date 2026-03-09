package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.SupabaseService.SupabaseStorageService;
import com.senior.candleShopProject.common.UserCheckTemp;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.exception.ShopConflictException;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.ProcessImageUtil;
import com.senior.candleShopProject.common.utils.RunningNumberGenerator;
import com.senior.candleShopProject.common.utils.SupabaseImageUtils;
import com.senior.candleShopProject.datasource.domain.shoppingCart.ICartItemsForOrderItemsResp;
import com.senior.candleShopProject.datasource.entities.AddressesEntity;
import com.senior.candleShopProject.datasource.entities.CustomersEntity;
import com.senior.candleShopProject.datasource.entities.OrdersEntity;
import com.senior.candleShopProject.datasource.entities.PaymentsEntity;
import com.senior.candleShopProject.datasource.repo.*;
import com.senior.candleShopProject.feature.orderCheckout.service.OrderCheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@TestComponent
public class OrderCheckoutServiceTest {

    @InjectMocks
    private OrderCheckoutService orderCheckoutService;

    @Mock private SupabaseStorageService supabaseStorageService;
    @Mock private RunningNumberGenerator runningNumberGenerator;

    @Mock private CustomersRepo customersRepo;
    @Mock private ShoppingCartItemsRepo shoppingCartItemsRepo;
    @Mock private OrdersRepo ordersRepo;
    @Mock private OrderItemsRepo orderItemsRepo;
    @Mock private ShoppingCartRepo shoppingCartRepo;
    @Mock private PaymentsRepo paymentsRepo;
    @Mock private AddressesRepo addressesRepo;

    @Mock private MultipartFile paymentProof;

    @Mock
    private UserCheckTemp userCheckTemp;

    @Mock
    SupabaseImageUtils supabaseImageUtils;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void checkoutOrder_Success() throws ShopServiceApiException, IOException {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID shoppingCartId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        CustomersEntity customer = new CustomersEntity();
        customer.setCustomerId(customerId);
        when(customersRepo.findCustomersEntitiesByUsersEntity_UserId(userId))
                .thenReturn(Optional.of(customer));

        AddressesEntity addresses = mock(AddressesEntity.class);
        when(addressesRepo.findById(addressId)).thenReturn(Optional.of(addresses));

        when(shoppingCartRepo.getShoppingCartIdByUserId(userId)).thenReturn(shoppingCartId);
        when(shoppingCartItemsRepo.existsByShoppingCartEntity_ShoppingCartId(shoppingCartId)).thenReturn(true);

        ICartItemsForOrderItemsResp item1 = mock(ICartItemsForOrderItemsResp.class);
        when(item1.getProductId()).thenReturn(UUID.randomUUID());
        when(item1.getProductName()).thenReturn("Candle A");
        when(item1.getPricePerUnit()).thenReturn(new BigDecimal("100.00"));
        when(item1.getQuantity()).thenReturn(2);
        List<ICartItemsForOrderItemsResp> cartItems = List.of(item1);

        List<String> cartItemIds = List.of(UUID.randomUUID().toString());
        List<UUID> cartItemUUIDs = cartItemIds.stream().map(UUID::fromString).toList();
        when(shoppingCartItemsRepo.getCartItemsForOrderItemsByCartIdAndCartItemIdList(shoppingCartId, cartItemUUIDs))
                .thenReturn(cartItems);

        OrdersEntity savedOrder = new OrdersEntity();
        savedOrder.setOrderId(orderId);
        savedOrder.setOrderCreatedAt(Instant.now());
        when(ordersRepo.save(any(OrdersEntity.class))).thenReturn(savedOrder);

        when(runningNumberGenerator.generateOrderRunningNumber(eq(Constants.PREFIX_ORDER_NO)))
                .thenReturn("ORD-001");
        when(runningNumberGenerator.generateOrderRunningNumber(eq(Constants.PREFIX_RECEIPT_NO)))
                .thenReturn("RCPT-001");

        when(paymentsRepo.existsByOrdersEntity_OrderId(orderId)).thenReturn(false);
        PaymentsEntity savedPayment = new PaymentsEntity();
        savedPayment.setPaymentId(paymentId);
        savedPayment.setReceiptNumber("RCPT-001");
        when(paymentsRepo.save(any(PaymentsEntity.class))).thenReturn(savedPayment);

        doNothing().when(shoppingCartItemsRepo).deleteAllById(cartItemUUIDs);

        Optional<OrdersEntity> orderOpt = Optional.of(new OrdersEntity());
        orderOpt.get().setCustomersEntity(customer);

        when(userCheckTemp.getCustomerIdByUserId(userId)).thenReturn(customerId);
        when(ordersRepo.findById(orderId)).thenReturn(orderOpt);

        when(paymentProof.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[] { (byte)0xFF, (byte)0xD8, (byte)0xFF }));
        try (MockedStatic<ProcessImageUtil> mocked = mockStatic(ProcessImageUtil.class)) {
            mocked.when(() -> ProcessImageUtil.processImageData(any(MultipartFile.class)))
                    .thenReturn(new byte[] {1,2,3});

            doNothing().when(supabaseStorageService).uploadImage(anyString(), anyString(), any(byte[].class), anyString());

            GenericResponse resp = orderCheckoutService.checkoutOrder(userId, paymentProof, cartItemIds,addressId);

            assertNotNull(resp);
            assertEquals(ResultCode.CREATED, resp.getStatus());

            verify(shoppingCartRepo, times(1)).getShoppingCartIdByUserId(userId);
            verify(shoppingCartItemsRepo, times(1)).existsByShoppingCartEntity_ShoppingCartId(shoppingCartId);
            verify(ordersRepo, times(1)).save(any(OrdersEntity.class));
            verify(orderItemsRepo, times(1)).saveAll(anyList());
            verify(shoppingCartItemsRepo, times(1)).deleteAllById(cartItemUUIDs);
            verify(paymentsRepo, times(1)).save(any(PaymentsEntity.class));
            verify(supabaseImageUtils, times(1)).uploadPaymentProofImage(any(), any(), any(), any(), anyString());
        }
    }

    @Test
    void checkoutOrder_UserNotFound_Throws() {
        UUID userId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        List<String> cartItemIds = List.of(UUID.randomUUID().toString());

        when(customersRepo.findCustomersEntitiesByUsersEntity_UserId(userId))
                .thenReturn(Optional.empty());

        ShopDataNotFoundException ex = assertThrows(ShopDataNotFoundException.class, () ->
                orderCheckoutService.checkoutOrder(userId, paymentProof, cartItemIds,addressId)
        );

        assertEquals(ResultCode.DATA_NOT_FOUND, ex.getStatus());
        verify(customersRepo, times(1)).findCustomersEntitiesByUsersEntity_UserId(userId);
        verifyNoInteractions(shoppingCartRepo, shoppingCartItemsRepo, ordersRepo, orderItemsRepo, paymentsRepo, supabaseStorageService);
    }

    @Test
    void retryPayment_Success_WhenExistingPaymentRejected() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        CustomersEntity customer = new CustomersEntity();
        customer.setCustomerId(customerId);
        when(customersRepo.findCustomersEntitiesByUsersEntity_UserId(userId))
                .thenReturn(Optional.of(customer));

        when(ordersRepo.existsById(orderId)).thenReturn(true);

        when(paymentsRepo.existsByOrdersEntity_OrderId(orderId)).thenReturn(true);
        PaymentsEntity existing = new PaymentsEntity();
        existing.setPaymentId(paymentId);
        existing.setReceiptNumber("RCPT-123");
        existing.setPaymentStatus(OrderStatus.ORDER_PAYMENT_REJECTED.getStatusCode());
        when(paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId)).thenReturn(existing);

        PaymentsEntity saved = new PaymentsEntity();
        saved.setPaymentId(paymentId);
        saved.setReceiptNumber("RCPT-123");
        when(paymentsRepo.save(any(PaymentsEntity.class))).thenReturn(saved);

        Optional<OrdersEntity> orderOpt = Optional.of(new OrdersEntity());
        orderOpt.get().setCustomersEntity(customer);

        when(userCheckTemp.getCustomerIdByUserId(userId)).thenReturn(customerId);
        when(ordersRepo.findById(orderId)).thenReturn(orderOpt);

        when(paymentProof.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1,2,3}));
        try (MockedStatic<ProcessImageUtil> mocked = mockStatic(ProcessImageUtil.class)) {
            mocked.when(() -> ProcessImageUtil.processImageData(any(MultipartFile.class)))
                    .thenReturn(new byte[]{9,8,7});
            doNothing().when(supabaseStorageService).uploadImage(anyString(), anyString(), any(byte[].class), anyString());

            GenericResponse resp = orderCheckoutService.retryPayment(userId, orderId, paymentProof);
            assertNotNull(resp);
            assertEquals(ResultCode.SUCCESS, resp.getStatus());

            verify(ordersRepo, times(1)).findById(orderId);
            verify(paymentsRepo, times(1)).existsByOrdersEntity_OrderId(orderId);
            verify(paymentsRepo, times(1)).findPaymentsEntitiesByOrdersEntity_OrderId(orderId);
            verify(paymentsRepo, times(1)).save(any(PaymentsEntity.class));
            verify(supabaseImageUtils, times(1)).uploadPaymentProofImage(any(), any(), any(), any(), anyString());
        }
    }

    @Test
    void retryPayment_Conflict_WhenExistingPaymentNotRejected() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        CustomersEntity customer = new CustomersEntity();
        customer.setCustomerId(customerId);
        when(customersRepo.findCustomersEntitiesByUsersEntity_UserId(userId))
                .thenReturn(Optional.of(customer));
        when(ordersRepo.existsById(orderId)).thenReturn(true);

        when(paymentsRepo.existsByOrdersEntity_OrderId(orderId)).thenReturn(true);
        PaymentsEntity existing = new PaymentsEntity();
        existing.setReceiptNumber("RCPT-123");
        existing.setPaymentStatus(OrderStatus.ORDER_PAYMENT_PENDING.getStatusCode());
        when(paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId)).thenReturn(existing);

        Optional<OrdersEntity> orderOpt = Optional.of(new OrdersEntity());
        orderOpt.get().setCustomersEntity(customer);
        when(userCheckTemp.getCustomerIdByUserId(userId)).thenReturn(customerId);
        when(ordersRepo.findById(orderId)).thenReturn(orderOpt);

        assertThrows(ShopConflictException.class, () ->
                orderCheckoutService.retryPayment(userId, orderId, paymentProof)
        );

        verify(userCheckTemp, times(1)).getCustomerIdByUserId(userId);
        verify(ordersRepo, times(1)).findById(orderId);
        verify(paymentsRepo, times(1)).existsByOrdersEntity_OrderId(orderId);
        verify(paymentsRepo, times(1)).findPaymentsEntitiesByOrdersEntity_OrderId(orderId);
        verify(paymentsRepo, times(0)).save(any());
        verifyNoInteractions(supabaseStorageService);
    }
}
