package com.senior.candleShopProject.feature.orderCheckout.service;


import com.senior.candleShopProject.common.*;
import com.senior.candleShopProject.common.exception.ShopConflictException;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopForbiddenException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.RunningNumberGenerator;
import com.senior.candleShopProject.common.utils.SupabaseImageUtils;
import com.senior.candleShopProject.datasource.domain.shoppingCart.ICartItemsForOrderItemsResp;
import com.senior.candleShopProject.datasource.entities.*;
import com.senior.candleShopProject.datasource.repo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.senior.candleShopProject.common.utils.ShippingUtils.calculateShippingCost;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCheckoutService {

    private final RunningNumberGenerator runningNumberGenerator;

    private final UserCheckTemp userCheckTemp;

    private final SupabaseImageUtils supabaseImageUtils;

    private final CustomersRepo customersRepo;
    private final ShoppingCartItemsRepo shoppingCartItemsRepo;
    private final OrdersRepo ordersRepo;
    private final OrderItemsRepo orderItemsRepo;
    private final ShoppingCartRepo shoppingCartRepo;
    private final PaymentsRepo paymentsRepo;
    private final OrderShippingAddressRepo orderShippingAddressRepo;
    private final AddressesRepo addressesRepo;

    @Transactional
    public GenericResponse checkoutOrder (UUID userId,
                                          MultipartFile paymentProof,
                                          List<String> cartItem,
                                          UUID addressId) throws ShopServiceApiException, IOException {

        Optional<CustomersEntity> customerOpt = customersRepo.findCustomersEntitiesByUsersEntity_UserId(userId);

        if(customerOpt.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User not found.");

        UUID shoppingCartId = shoppingCartRepo.getShoppingCartIdByUserId(userId);

        if (!shoppingCartItemsRepo.existsByShoppingCartEntity_ShoppingCartId(shoppingCartId))
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "You don't have permission to perform this action.");



//        abstract OrderCheckoutReq
        List<UUID> shoppingCartItemIds = cartItem
                .stream()
                .map(UUID::fromString)
                .toList();

//        find shopping cart items by list of shopping cart item id
        List<ICartItemsForOrderItemsResp> cartItemsForOrderItemsRests = shoppingCartItemsRepo
                .getCartItemsForOrderItemsByCartIdAndCartItemIdList(shoppingCartId,shoppingCartItemIds);

        if (cartItemsForOrderItemsRests.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Shopping cart items not found.");

        if (shoppingCartItemIds.size() != cartItemsForOrderItemsRests.size())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Some shopping cart items is missing.");

        int totalQuantity = getTotalQuantityFromCartItems(cartItemsForOrderItemsRests);
        BigDecimal totalAmount = getTotalAmountFromCartItems(cartItemsForOrderItemsRests);
        UUID customerId = customerOpt.get().getCustomerId();

        CustomersEntity customersEntity = new CustomersEntity();
        customersEntity.setCustomerId(customerId);

//      Save order & order items
        OrdersEntity ordersEntity = new OrdersEntity();

        AddressesEntity addressesEntity = addressesRepo.findById(addressId)
                .orElseThrow(() -> new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Address not found."));
        OrderShippingAddressEntity orderShippingAddressEntity = getOrderShippingAddressEntity(addressesEntity);
        orderShippingAddressEntity.setOrdersEntity(ordersEntity);

        ordersEntity.setOrderNo(runningNumberGenerator.generateOrderRunningNumber(Constants.PREFIX_ORDER_NO));
        ordersEntity.setTotalQuantity(totalQuantity);
        ordersEntity.setTotalAmount(totalAmount);
        ordersEntity.setNetAmount(totalAmount.add(calculateShippingCost(totalQuantity)));
        ordersEntity.setOrderStatus(OrderStatus
                .ORDER_PAYMENT_PENDING.getStatusCode()
        );
        ordersEntity.setOrderCreatedAt(Instant.now());
        ordersEntity.setCustomersEntity(customersEntity);
        ordersEntity.setOrderShippingAddressEntity(orderShippingAddressEntity);

        OrdersEntity newOrderEntity = ordersRepo.save(ordersEntity);

        List<OrderItemsEntity> orderItemsEntity = mapListToOrderItemsEntity(newOrderEntity.getOrderId(), cartItemsForOrderItemsRests);

        orderItemsRepo.saveAll(orderItemsEntity);

        shoppingCartItemsRepo.deleteAllById(shoppingCartItemIds);

//        save payment
        Status resultCode = upsertPaymentEntity(customerId,newOrderEntity.getOrderId(),paymentProof, newOrderEntity.getOrderCreatedAt());

        GenericResponse response = new GenericResponse();
        response.setData(null);
        response.setStatus(resultCode);
        return response;
    }

    public GenericResponse retryPayment (UUID userId, UUID orderId, MultipartFile paymentProof) throws ShopServiceApiException, IOException {

        UUID customerId = userCheckTemp.getCustomerIdByUserId(userId);

        if (customerId == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User not found.");

        Optional<OrdersEntity> orderOpt = ordersRepo.findById(orderId);

        if(orderOpt.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Order not found.");

        if (!orderOpt.get().getCustomersEntity().getCustomerId().equals(customerId))
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "You don't have permission to perform this action.");


        Status resultCode = upsertPaymentEntity(customerId,orderId,paymentProof, orderOpt.get().getOrderCreatedAt());

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

    private Status upsertPaymentEntity(UUID customerId,UUID orderId,MultipartFile paymentProof, Instant createdAt) throws ShopServiceApiException, IOException {
//        true if new checkout, false if update existing payment proof
        boolean isNewCheckout = !paymentsRepo.existsByOrdersEntity_OrderId(orderId);

        Status resultCode;
        String runningNumber;
        PaymentsEntity paymentsEntity;

        if (isNewCheckout) {
            runningNumber = runningNumberGenerator.generateOrderRunningNumber(Constants.PREFIX_RECEIPT_NO);

            OrdersEntity ordersEntity = new OrdersEntity();
            ordersEntity.setOrderId(orderId);

            paymentsEntity = new PaymentsEntity();

            paymentsEntity.setPaymentId(UUID.randomUUID());
            paymentsEntity.setReceiptNumber(runningNumber);
            paymentsEntity.setPaymentProofPath(
                    runningNumber + "." + Constants.CONTENT_TYPE_JPEG.split("/")[1]
            );
            paymentsEntity.setOrdersEntity(ordersEntity);
            paymentsEntity.setCreatedAt(Instant.now());

            resultCode = ResultCode.CREATED;

        }else{
            paymentsEntity = paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId);

            if(!paymentsEntity.getPaymentStatus().equalsIgnoreCase(OrderStatus.ORDER_PAYMENT_REJECTED.getStatusCode()))
                throw new ShopConflictException(ResultCode.CONFLICT, "Status of payment is not rejected. You can't resubmit payment proof.");

            runningNumber = paymentsEntity.getReceiptNumber();
            paymentsEntity.setResubmitAt(Instant.now());
            paymentsEntity.setStatusChangedAt(Instant.now());
            paymentsEntity.setRejectionReason(null);
            resultCode = ResultCode.SUCCESS;
        }

        paymentsEntity.setPaymentStatus(
                OrderStatus.ORDER_PAYMENT_PENDING.getStatusCode()
        );

        PaymentsEntity newPaymentEntity = paymentsRepo.save(paymentsEntity);

//        image path : /YYYY/customer_id/payment_id/receipt_number **YYYY = CE-Year / ปีคริสต์ศักราช
        supabaseImageUtils.uploadPaymentProofImage(
                createdAt,
                customerId,
                newPaymentEntity,
                paymentProof,
                runningNumber
        );

        return resultCode;
    }

    private int getTotalQuantityFromCartItems(List<ICartItemsForOrderItemsResp> cartItemsForOrderItemsRests){
        int totalQuantity = 0;
        for (ICartItemsForOrderItemsResp item : cartItemsForOrderItemsRests) {
            totalQuantity += item.getQuantity();
        }
        return totalQuantity;
    }

    private BigDecimal getTotalAmountFromCartItems(List<ICartItemsForOrderItemsResp> cartItemsForOrderItemsRests){
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ICartItemsForOrderItemsResp item : cartItemsForOrderItemsRests) {
            totalAmount = totalAmount.add(
                    item.getPricePerUnit().multiply(
                            new BigDecimal(item.getQuantity())
                    )
            );
        }
        return totalAmount;
    }

    private OrderShippingAddressEntity getOrderShippingAddressEntity(AddressesEntity addressesEntity) {
        OrderShippingAddressEntity orderShippingAddressEntity = new OrderShippingAddressEntity();
        OrdersEntity ordersEntity = new OrdersEntity();
        orderShippingAddressEntity.setOrdersEntity(ordersEntity);
        orderShippingAddressEntity.setDeliveryAddress(addressesEntity.getDeliveryAddress());
        orderShippingAddressEntity.setPostcode(addressesEntity.getPostcode());
        orderShippingAddressEntity.setProvince(addressesEntity.getProvince());
        orderShippingAddressEntity.setDistrict(addressesEntity.getDistrict());
        orderShippingAddressEntity.setSubDistrict(addressesEntity.getSubDistrict());
        orderShippingAddressEntity.setAddressLabel(addressesEntity.getAddressLabel());
        orderShippingAddressEntity.setRecipientFirstName(addressesEntity.getRecipientFirstName());
        orderShippingAddressEntity.setRecipientLastName(addressesEntity.getRecipientLastName());
        orderShippingAddressEntity.setRecipientPhone(addressesEntity.getRecipientPhone());

        return orderShippingAddressEntity;
    }
}
