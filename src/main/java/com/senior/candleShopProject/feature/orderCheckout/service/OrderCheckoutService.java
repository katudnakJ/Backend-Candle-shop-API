package com.senior.candleShopProject.feature.orderCheckout.service;


import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.Status;
import com.senior.candleShopProject.common.SupabaseService.SupabaseStorageService;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopForbiddenException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.MultipartFileConverter;
import com.senior.candleShopProject.common.utils.RunningNumberGenerator;
import com.senior.candleShopProject.datasource.domain.IAllItemsShoppingCartResp;
import com.senior.candleShopProject.datasource.domain.ICartItemsForOrderItemsResp;
import com.senior.candleShopProject.datasource.domain.IUsersResp;
import com.senior.candleShopProject.datasource.entities.*;
import com.senior.candleShopProject.datasource.repo.*;
import com.senior.candleShopProject.feature.orderCheckout.controller.dto.request.OrderCheckoutReq;
import com.senior.candleShopProject.feature.orderCheckout.controller.dto.request.OrdersReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.senior.candleShopProject.common.utils.ProcessImageUtil.processImageData;
import static com.senior.candleShopProject.common.utils.RunningNumberGenerator.generateOrderRunningNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCheckoutService {

    private final CustomersRepo customersRepo;
    private final ShoppingCartItemsRepo shoppingCartItemsRepo;
    private final OrdersRepo ordersRepo;
    private final OrderItemsRepo orderItemsRepo;
    private final ShoppingCartRepo shoppingCartRepo;
    private final PaymentsRepo paymentsRepo;
    private final SupabaseStorageService supabaseStorageService;

    @Transactional
    public GenericResponse checkoutOrder (UUID userId, MultipartFile paymentProof, OrderCheckoutReq orderCheckoutReq) throws ShopServiceApiException, IOException {

        Optional<CustomersEntity> customerOpt = customersRepo.findCustomersEntitiesByUsersEntity_UserId(userId);

        if(customerOpt.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User not found.");

        UUID shoppingCartId = shoppingCartRepo.getShoppingCartIdByUserId(userId);

        if (!shoppingCartItemsRepo.existsByShoppingCartEntity_ShoppingCartId(shoppingCartId))
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "You don't have permission to perform this action.");

        String genOrderNo = generateOrderRunningNumber(Constants.PREFIX_ORDER_NO);
        UUID customerId = customerOpt.get().getCustomerId();

        List<UUID> shoppingCartItemIds = orderCheckoutReq.getShoppingCartItemIds()
                .stream()
                .map(UUID::fromString)
                .toList();
        OrdersReq ordersReq = orderCheckoutReq.getOrdersReq();

        CustomersEntity customersEntity = new CustomersEntity();
        customersEntity.setCustomerId(customerId);

//        find shopping cart items by list of shopping cart item id
        List<ICartItemsForOrderItemsResp> cartItemsForOrderItemsRests = shoppingCartItemsRepo
                .getCartItemsForOrderItemsByCartIdAndCartItemIdList(shoppingCartId,shoppingCartItemIds);

        if (cartItemsForOrderItemsRests.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Shopping cart items not found.");

        if (shoppingCartItemIds.size() != cartItemsForOrderItemsRests.size())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Some shopping cart items is missing.");

//      Save order & order items
        OrdersEntity ordersEntity = new OrdersEntity();
        ordersEntity.setOrderNo(genOrderNo);
        ordersEntity.setTotalQuantity(ordersReq.getTotalQuantity());
        ordersEntity.setTotalAmount(ordersReq.getTotalAmount());
        ordersEntity.setNetAmount(ordersReq.getNetAmount());
        ordersEntity.setOrderStatus(OrderStatus
                .ORDER_PAYMENT_PENDING.getOrderStatusCode()
        );
        ordersEntity.setOrderCreatedDate(Instant.now());
        ordersEntity.setCustomersEntity(customersEntity);

        OrdersEntity newOrderEntity = ordersRepo.save(ordersEntity);

        List<OrderItemsEntity> orderItemsEntity = mapListToOrderItemsEntity(newOrderEntity.getOrderId(), cartItemsForOrderItemsRests);

        orderItemsRepo.saveAll(orderItemsEntity);
        shoppingCartItemsRepo.deleteAllById(shoppingCartItemIds);

//        save payment
        Status resultCode = upsertPaymentEntity(customerId,newOrderEntity.getOrderId(),paymentProof);

        GenericResponse response = new GenericResponse();
        response.setData(null);
        response.setStatus(resultCode);
        return response;
    }

    public GenericResponse retryPayment (UUID userId, UUID orderId, MultipartFile paymentProof) throws ShopServiceApiException, IOException {
        Optional<CustomersEntity> customerOpt = customersRepo.findCustomersEntitiesByUsersEntity_UserId(userId);

        if(customerOpt.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User not found.");

        UUID customerId = customerOpt.get().getCustomerId();

        if (!ordersRepo.existsById(orderId))
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "You don't have permission to perform this action.");

        Status resultCode = upsertPaymentEntity(customerId,orderId,paymentProof);

        GenericResponse response = new GenericResponse();
        response.setData(null);
        response.setStatus(resultCode);
        return response;
    }

    private List<OrderItemsEntity> mapListToOrderItemsEntity(UUID newOrderId,List<ICartItemsForOrderItemsResp> cartItem) {;
        List<OrderItemsEntity> orderItemsEntity = new ArrayList<>();
        for (ICartItemsForOrderItemsResp items : cartItem) {
            OrderItemsEntity orderItems = new OrderItemsEntity();

            OrdersEntity ordersEntity = new OrdersEntity();
            ordersEntity.setOrderId(newOrderId);

            ProductsEntity productsEntity = new ProductsEntity();
            productsEntity.setProductId(items.getProductId());

            orderItems.setProductNameAtPurchase(items.getProductName());
            orderItems.setPricePerUnitAtPurchase(items.getPricePerUnit());
            orderItems.setQuantity(items.getQuantity());
            orderItems.setSubtotalAtPurchase(
                    items.getPricePerUnit().multiply(
                            new java.math.BigDecimal(items.getQuantity())
                    )
            );
            orderItems.setOrdersEntity(ordersEntity);
            orderItems.setProductsEntity(productsEntity);


            orderItemsEntity.add(orderItems);
        }

        return orderItemsEntity;
    }

    private Status upsertPaymentEntity(UUID customerId,UUID orderId,MultipartFile paymentProof) throws ShopServiceApiException, IOException {
//        true if new checkout, false if update existing payment proof
        boolean isNewCheckout = !paymentsRepo.existsByOrdersEntity_OrderId(orderId);

        Status resultCode;
        String runningNumber;
        PaymentsEntity paymentsEntity;

        if (isNewCheckout) {
            runningNumber = generateOrderRunningNumber(Constants.PREFIX_RECEIPT_NO);

            OrdersEntity ordersEntity = new OrdersEntity();
            ordersEntity.setOrderId(orderId);

            paymentsEntity = new PaymentsEntity();

            paymentsEntity.setReceiptNumber(runningNumber);
            paymentsEntity.setPaymentProofPath(
                    runningNumber + "." + Constants.CONTENT_TYPE_JPEG.split("/")[1]
            );
            paymentsEntity.setOrdersEntity(ordersEntity);

            resultCode = ResultCode.CREATED;

        }else{
            paymentsEntity = paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId);
            runningNumber = paymentsEntity.getReceiptNumber();
            resultCode = ResultCode.SUCCESS;
        }

        paymentsEntity.setPaymentStatus(
                OrderStatus.ORDER_PAYMENT_PENDING.getOrderStatusCode()
        );
        paymentsEntity.setPaymentRequestDate(Instant.now());

        PaymentsEntity newPaymentEntity = paymentsRepo.save(paymentsEntity);

//        image path : /customer_id/payment_id/receipt_number
        String imagePath = customerId + "/"
                + newPaymentEntity.getPaymentId() + "/"
                + runningNumber
                + "." + Constants.CONTENT_TYPE_JPEG.split("/")[1];

        supabaseStorageService.uploadImage(
                Constants.SUPABASE_RECEIPT_BUCKET_URL,
                imagePath,
                processImageData(paymentProof),
                Constants.CONTENT_TYPE_JPEG
        );

        return resultCode;
    }
}
