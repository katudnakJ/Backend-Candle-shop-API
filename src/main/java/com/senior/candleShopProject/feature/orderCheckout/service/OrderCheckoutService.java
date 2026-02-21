package com.senior.candleShopProject.feature.orderCheckout.service;


import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.ResultCode;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.senior.candleShopProject.common.utils.RunningNumberGenerator.generateOrderRunningNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCheckoutService {

    private final CustomersRepo customersRepo;
    private final ShoppingCartItemsRepo shoppingCartItemsRepo;
    private final OrdersRepo ordersRepo;
    private final OrderItemsRepo orderItemsRepo;

    @Transactional
    public GenericResponse checkoutOrder (UUID userId, MultipartFile paymentProof, OrderCheckoutReq orderCheckoutReq) throws ShopServiceApiException {

        Optional<CustomersEntity> customerOpt = customersRepo.findCustomersEntitiesByUsersEntity_UserId(userId);

        if(customerOpt.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User not found.");

        UUID shoppingCartId = UUID.fromString(orderCheckoutReq.getShoppingCartId());

        if (!shoppingCartItemsRepo.existsByShoppingCartEntity_ShoppingCartId(shoppingCartId))
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "You don't have permission to perform this action.");

        String genOrderNo = generateOrderRunningNumber(Constants.PREFIX_ORDER_NO);

        List<UUID> shoppingCartItemIds = orderCheckoutReq.getShoppingCartItemIds()
                .stream()
                .map(UUID::fromString)
                .toList();
        OrdersReq ordersReq = orderCheckoutReq.getOrdersReq();

        CustomersEntity customersEntity = new CustomersEntity();
        customersEntity.setCustomer_id(customerOpt.get().getCustomer_id());

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
        ordersEntity.setTotalAmountPurchase(ordersEntity.getTotalAmountPurchase());
        ordersEntity.setTotalQuantityAmount(ordersEntity.getTotalQuantityAmount());
        ordersEntity.setCustomersEntity(customersEntity);

        OrdersEntity newOrderEntity = ordersRepo.save(ordersEntity);

        List<OrderItemsEntity> orderItemsEntity = mapListToOrderItemsEntity(newOrderEntity.getOrderId(), cartItemsForOrderItemsRests);

        orderItemsRepo.saveAll(orderItemsEntity);

//        save payment


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
            orderItems.setPriceAtPurchase(items.getPrice());
            orderItems.setQuantity(items.getQuantity());
            orderItems.setOrdersEntity(ordersEntity);
            orderItems.setProductsEntity(productsEntity);


            orderItemsEntity.add(orderItems);
        }

        return orderItemsEntity;
    }
}
