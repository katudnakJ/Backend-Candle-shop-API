package com.senior.candleShopProject.feature.order.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.UserCheckTemp;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.CustomizeResponseUtil;
import com.senior.candleShopProject.datasource.domain.orders.IOrderByStatusResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderDetailByStatusResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderItemListResp;
import com.senior.candleShopProject.datasource.entities.OrdersEntity;
import com.senior.candleShopProject.datasource.entities.PaymentsEntity;
import com.senior.candleShopProject.datasource.entities.ShipmentEntity;
import com.senior.candleShopProject.datasource.repo.*;
import com.senior.candleShopProject.feature.order.controller.dto.request.RejectPaymentReq;
import com.senior.candleShopProject.feature.order.controller.dto.request.TrackOrderReq;
import com.senior.candleShopProject.feature.order.controller.dto.response.dto.OrderByStatusResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.OrderDetailsResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.OrderItemsListResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.dto.OrderStatusChangeResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserCheckTemp userCheckTemp;

    private final OrdersRepo ordersRepo;
    private final CarriersRepo carriersRepo;
    private final OrderItemsRepo orderItemsRepo;
    private final PaymentsRepo paymentsRepo;
    private final ShipmentRepo shipmentRepo;

    public GenericResponse getAllCarriers() {
        GenericResponse response = new GenericResponse();
        response.setData(carriersRepo.findAll());
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    public GenericResponse getOrderByStatus(UUID userId, String status) throws ShopServiceApiException {

//        if (status == null || status.isEmpty())
//            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Status is required.");

        userCheckTemp.checkExistsUser(userId);
        boolean validStatus = OrderStatus.isValidStatus(status);

        UUID customerId = userCheckTemp.getCustomerIdByUserId(userId);

        if (!validStatus)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Invalid order status.");

        List<IOrderByStatusResp> order = ordersRepo.getOrderByCustIdStatus(customerId, status,(userCheckTemp.getSellerIdByUserId(userId) != null));
        List<UUID> orderIds = order.stream().map(IOrderByStatusResp::getOrderId).toList();
        List<IOrderItemListResp> orderItems = orderItemsRepo.getOrderItemByOrderIds(orderIds);

        if (orderItems == null || orderItems.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND,"Order not found.");

        List<OrderByStatusResp> ordersResponse = mapToOrderByStatusResp(order ,orderItems);

        Map<String, Object> bodyResponse = CustomizeResponseUtil.ReturnBodyWithCount(
                ordersResponse.size(),
                ordersResponse,
                "orders"
        );

        GenericResponse response = new GenericResponse();
        response.setData(bodyResponse);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    public GenericResponse getOrderDetailsByOrderId(UUID userId, UUID orderId) throws ShopServiceApiException {

//        if (orderId == null)
//            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Order ID is required.");

        userCheckTemp.checkExistsUser(userId);
        userCheckTemp.isOwnerOfOrder(userId, orderId);

        IOrderDetailByStatusResp orderDetails = ordersRepo.getOrderDetailByOrderId(orderId);

        if(orderDetails == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Order not found.");

        List<IOrderItemListResp> orderItemsListResp = orderItemsRepo.getOrderItemByOrderIds(List.of(orderId));

        if (orderItemsListResp.isEmpty())
            throw new ShopConflictException(ResultCode.CONFLICT,"Order must contain at least one item.");

        OrderDetailsResp orderDetailsResp = new OrderDetailsResp();
        orderDetailsResp.setOrderDetail(orderDetails);
        orderDetailsResp.setOrderItems(orderItemsListResp);

        GenericResponse response = new GenericResponse();
        response.setData(orderDetailsResp);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    public GenericResponse confirmPayment(UUID userId, UUID orderId) throws  ShopServiceApiException {

//        if (orderId == null)
//            throw new ShopBadRequestException(ResultCode.BAD_REQUEST,"Order ID is required.");

        userCheckTemp.checkExistsUser(userId);
        Optional<OrdersEntity> order = ordersRepo.findById(orderId);
        if (order.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND,"Order not found.");

        OrdersEntity orderEntity = order.get();
        OrderStatus newStatus = OrderStatus.ORDER_TO_SHIP;
        OrderStatus.validToChangeStatus(orderEntity.getOrderStatus(), newStatus.getStatusCode());

        UUID sellerId = userCheckTemp.getSellerIdByUserId(userId);

        if ( sellerId == null )
            throw new ShopForbiddenException(ResultCode.FORBIDDEN,"You don't have permission.");

        Instant timeNow = Instant.now();
        PaymentsEntity payment = paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId);
        payment.setPaymentStatus(OrderStatus.ORDER_PAYMENT_APPROVED.getStatusCode());
        payment.setStatusChangedAt(timeNow);
        payment.setApproveAt(timeNow);

        orderEntity.setOrderStatus(newStatus.getStatusCode());
        orderEntity.setStatusChangedAt(timeNow);

        OrdersEntity newOrder = ordersRepo.save(orderEntity);

        OrderStatusChangeResp orderStatusChangeResp = setOrderStatusChangeResp(newOrder, newStatus);

        GenericResponse response = new GenericResponse();
        response.setData(orderStatusChangeResp);
        response.setStatus(ResultCode.SUCCESS);
        return response;

    }

    public GenericResponse rejectPayment(UUID userId, RejectPaymentReq rejectPaymentReq ) throws  ShopServiceApiException {

//        if (rejectPaymentReq.getOrderId() == null || rejectPaymentReq.getReason().isEmpty())
//                throw new ShopBadRequestException(ResultCode.BAD_REQUEST,"Order ID and reason are required.");

        userCheckTemp.checkExistsUser(userId);
        UUID sellerId = userCheckTemp.getSellerIdByUserId(userId);

        if( sellerId == null )
            throw new ShopForbiddenException(ResultCode.FORBIDDEN,"You don't have permission.");

        Optional<OrdersEntity> order = ordersRepo.findById(rejectPaymentReq.getOrderId());
            if (order.isEmpty())
                throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND,"Order not found.");

            PaymentsEntity payment = paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(rejectPaymentReq.getOrderId());

            if( payment == null )
                throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND,"Payment not found.");

            OrdersEntity orderEntity = order.get();
            OrderStatus newStatus = OrderStatus.ORDER_PAYMENT_REJECTED;
            Instant timeNow = Instant.now();

            payment.setPaymentStatus(newStatus.getStatusCode());
            payment.setStatusChangedAt(timeNow);
            payment.setRejectionReason(rejectPaymentReq.getReason());

            orderEntity.setOrderStatus(newStatus.getStatusCode());
            orderEntity.setStatusChangedAt(timeNow);
            orderEntity.setPaymentsEntity(payment);

            OrdersEntity newOrder = ordersRepo.save(orderEntity);

            OrderStatusChangeResp orderStatusChangeResp = setOrderStatusChangeResp(newOrder, newStatus);

            GenericResponse response = new GenericResponse();
            response.setData(orderStatusChangeResp);
            response.setStatus(ResultCode.SUCCESS);
            return response;
    }

    public GenericResponse trackOrder(UUID userId, TrackOrderReq trackOrderReq) throws ShopServiceApiException {
        userCheckTemp.checkExistsUser(userId);

//        if (trackOrderReq.getOrderId() == null || trackOrderReq.getTrackNo().isEmpty())
//            throw new ShopBadRequestException(ResultCode.BAD_REQUEST,"Tracking number is required.");

        UUID sellerId = userCheckTemp.getSellerIdByUserId(userId);

        if (sellerId == null)
            throw new ShopForbiddenException(ResultCode.FORBIDDEN,"You don't have permission.");

        Optional<OrdersEntity> order = ordersRepo.findById(trackOrderReq.getOrderId());
        if (order.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND,"Order not found.");

        OrderStatus newStatus = OrderStatus.ORDER_TO_RECIEVE;
        OrderStatus.validToChangeStatus(order.get().getOrderStatus(), newStatus.getStatusCode());

        String trackNumbers = String.join(",", trackOrderReq.getTrackingNumber());
        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setDeliveryMethod(Constants.SHIPPING_METHOD_STANDARD);
        shipment.setTrackingNumber(trackNumbers);

        Instant timeNow = Instant.now();

        OrdersEntity orderEntity = order.get();
        orderEntity.setOrderStatus(OrderStatus.ORDER_TO_RECIEVE.getStatusCode());
        orderEntity.setStatusChangedAt(timeNow);
        orderEntity.setOrderStatus(newStatus.getStatusCode());
        orderEntity.setShipmentEntity(shipment);

        OrdersEntity newOrder = ordersRepo.save(orderEntity);

        OrderStatusChangeResp orderStatusChangeResp = setOrderStatusChangeResp(newOrder, newStatus);

        GenericResponse response = new GenericResponse();
        response.setData(orderStatusChangeResp);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    private List<OrderByStatusResp> mapToOrderByStatusResp(List <IOrderByStatusResp> order, List<IOrderItemListResp> orderItems) {

        List<OrderByStatusResp> responseData = new ArrayList<>();

//        Group order items by orderId to optimize the mapping process
        Map<UUID, List<IOrderItemListResp>> orderItemsMap = orderItems.stream()
                .collect(Collectors.groupingBy(IOrderItemListResp::getOrderId));

//         set order items to each order response
        order.forEach(orderItem -> {
            List<IOrderItemListResp> itemsMap = orderItemsMap.get(orderItem.getOrderId());
            List<OrderItemsListResp> orderItemsListResp = mapToOrderItemsListResp(itemsMap);

            OrderByStatusResp orderByStatusResp = new OrderByStatusResp();
            orderByStatusResp.setOrderId(orderItem.getOrderId());
            orderByStatusResp.setTotalQuantity(orderItem.getTotalQuantity());
            orderByStatusResp.setTotalAmount(orderItem.getTotalAmount());
            orderByStatusResp.setNetAmount(orderItem.getNetAmount());
            orderByStatusResp.setOrderStatus(
                    OrderStatus.getStatusRespByStatusCode(orderItem.getOrderStatus())
            );
            orderByStatusResp.setOrderNo(orderItem.getOrderNo());
            orderByStatusResp.setAddressLabel(orderItem.getAddressLabel());
            orderByStatusResp.setTrackingNo(orderItem.getTrackingNo());
            orderByStatusResp.setRejectionReason(orderItem.getRejectionReason());
            orderByStatusResp.setOrderItems(orderItemsListResp);
            responseData.add(orderByStatusResp);
        });

        return responseData;
    }

    private List<OrderItemsListResp> mapToOrderItemsListResp(List<IOrderItemListResp> itemsMap) {
        return itemsMap.stream().map(item -> {;
            OrderItemsListResp items = new OrderItemsListResp();
            items.setOrderItemId(item.getOrderItemId());
            items.setProductName(item.getProductName());
            items.setQuantity(item.getQuantity());
            items.setPricePerUnit(item.getPricePerUnit());
            items.setSubTotal(item.getSubTotal());
            items.setProductImagePath(item.getProductImgPath());
            return items;
        }).toList();
    }

    private OrderStatusChangeResp setOrderStatusChangeResp(OrdersEntity orderEntity, OrderStatus newStatus) {
        OrderStatusChangeResp orderStatusChangeResp = new OrderStatusChangeResp();
        orderStatusChangeResp.setOrderId(orderEntity.getOrderId());
        orderStatusChangeResp.setNewStatus(newStatus);
        orderStatusChangeResp.setStatusChangedAt(orderEntity.getStatusChangedAt());
        return orderStatusChangeResp;
    }
}
