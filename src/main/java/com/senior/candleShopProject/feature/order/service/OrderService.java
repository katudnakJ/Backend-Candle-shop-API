package com.senior.candleShopProject.feature.order.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.UserCheckTemp;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.CustomizeResponseUtil;
import com.senior.candleShopProject.datasource.domain.orders.IOrderByStatusResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderDetailByOrderIdResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderItemListResp;
import com.senior.candleShopProject.datasource.entities.OrdersEntity;
import com.senior.candleShopProject.datasource.entities.PaymentsEntity;
import com.senior.candleShopProject.datasource.entities.SellerEntity;
import com.senior.candleShopProject.datasource.entities.ShipmentEntity;
import com.senior.candleShopProject.datasource.repo.*;
import com.senior.candleShopProject.feature.order.controller.dto.request.RejectPaymentReq;
import com.senior.candleShopProject.feature.order.controller.dto.request.TrackOrderReq;
import com.senior.candleShopProject.feature.order.controller.dto.response.OrderDetailByOrderIdResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.dto.OrderByStatusResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.dto.OrderDetailsResp;
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

    public GenericResponse getAllCarriers() {
        GenericResponse response = new GenericResponse();
        response.setData(carriersRepo.findAll());
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    public GenericResponse getOrderByStatus(UUID userId, String status) throws ShopServiceApiException {

        if (status == null || status.isEmpty())
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Status is required.");

        userCheckTemp.checkExistsUser(userId);
        boolean validStatus = OrderStatus.isValidStatus(status);

        UUID customerId = userCheckTemp.getCustomerIdByUserId(userId);

        if (!validStatus)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Invalid order status.");

        List<IOrderByStatusResp> order = ordersRepo.getOrderByCustIdStatus(customerId, status,(userCheckTemp.getSellerIdByUserId(userId) != null));
        if (order == null || order.isEmpty()){
            GenericResponse response = new GenericResponse();
            response.setData(null);
            response.setStatus(ResultCode.SUCCESS);
            return response;
        }

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

        userCheckTemp.checkExistsUser(userId);
        userCheckTemp.isOwnerOfOrder(userId, orderId);

        IOrderDetailByOrderIdResp orderDetails = ordersRepo.getOrderDetailByOrderId(orderId);

        if(orderDetails == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Order not found.");

        List<IOrderItemListResp> orderItemsListResp = orderItemsRepo.getOrderItemByOrderIds(List.of(orderId));

        if (orderItemsListResp.isEmpty())
            throw new ShopConflictException(ResultCode.CONFLICT,"Order must contain at least one item.");

        OrderDetailsResp orderDetailsResp = mapToOrderDetailsResp(orderDetails, orderItemsListResp);

        GenericResponse response = new GenericResponse();
        response.setData(orderDetailsResp);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    public GenericResponse confirmPayment(UUID userId, UUID orderId) throws  ShopServiceApiException {

        userCheckTemp.checkExistsUser(userId);
        Optional<OrdersEntity> order = ordersRepo.findById(orderId);
        if (order.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND,"Order not found.");

        OrdersEntity orderEntity = order.get();
        OrderStatus newStatus = OrderStatus.ORDER_TO_SHIP;

        PaymentsEntity payment = paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId);

        boolean isValidStatus = OrderStatus.validToChangeStatus(payment.getPaymentStatus(), newStatus.getStatusCode());

        if (!isValidStatus)
            throw new ShopConflictException(ResultCode.CONFLICT,"Only orders with 'Payment Pending' status can be confirmed.");

        UUID sellerId = userCheckTemp.getSellerIdByUserId(userId);

        if ( sellerId == null )
            throw new ShopForbiddenException(ResultCode.FORBIDDEN,"You don't have permission.");

        Instant timeNow = Instant.now();
        SellerEntity seller = new SellerEntity();
        seller.setSellerId(sellerId);

        payment.setPaymentStatus(OrderStatus.ORDER_PAYMENT_APPROVED.getStatusCode());
        payment.setStatusChangedAt(timeNow);
        payment.setApproveAt(timeNow);
        payment.setSellerEntity(seller);

        return getGenericResponse(orderEntity, newStatus, timeNow, payment);

    }

    public GenericResponse rejectPayment(UUID userId,UUID orderId, RejectPaymentReq rejectPaymentReq ) throws  ShopServiceApiException {

        userCheckTemp.checkExistsUser(userId);
        UUID sellerId = userCheckTemp.getSellerIdByUserId(userId);

        if( sellerId == null )
            throw new ShopForbiddenException(ResultCode.FORBIDDEN,"You don't have permission.");

        Optional<OrdersEntity> order = ordersRepo.findById(orderId);
            if (order.isEmpty())
                throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND,"Order not found.");

            PaymentsEntity payment = paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId);

            if( payment == null )
                throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND,"Payment not found.");

            OrdersEntity orderEntity = order.get();
            OrderStatus newStatus = OrderStatus.ORDER_PAYMENT_REJECTED;

        boolean isValidStatus = OrderStatus.validToChangeStatus(payment.getPaymentStatus(), newStatus.getStatusCode());

        if (!isValidStatus)
            throw new ShopConflictException(ResultCode.CONFLICT,"Only orders with 'Payment Pending' status can be confirmed.");

        Instant timeNow = Instant.now();

        payment.setPaymentStatus(newStatus.getStatusCode());
        payment.setStatusChangedAt(timeNow);
        payment.setRejectionReason(rejectPaymentReq.getReason());

        return getGenericResponse(orderEntity, newStatus, timeNow, payment);
    }

    public GenericResponse trackOrder(UUID userId, UUID orderId, TrackOrderReq trackOrderReq) throws ShopServiceApiException {
        userCheckTemp.checkExistsUser(userId);

        if (orderId == null || trackOrderReq.getTrackingNumber().isEmpty())
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST,"Tracking number is required.");

        UUID sellerId = userCheckTemp.getSellerIdByUserId(userId);

        if (sellerId == null)
            throw new ShopForbiddenException(ResultCode.FORBIDDEN,"You don't have permission.");

        Optional<OrdersEntity> order = ordersRepo.findById(orderId);
        if (order.isEmpty())
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND,"Order not found.");

        OrderStatus newStatus = OrderStatus.ORDER_TO_RECIEVE;

        boolean isValidStatus = OrderStatus.validToChangeStatus(order.get().getOrderStatus(), newStatus.getStatusCode());

        if (!isValidStatus)
            throw new ShopConflictException(ResultCode.CONFLICT,"Only orders with 'TO SHIP' status can be confirmed.");

        Instant timeNow = Instant.now();

        OrdersEntity orderEntity = order.get();
        orderEntity.setOrderStatus(OrderStatus.ORDER_TO_RECIEVE.getStatusCode());
        orderEntity.setStatusChangedAt(timeNow);
        orderEntity.setOrderStatus(newStatus.getStatusCode());

        String trackNumbers = String.join(",", trackOrderReq.getTrackingNumber());
        ShipmentEntity shipment = new ShipmentEntity();
        shipment.setOrdersEntity(orderEntity);
        shipment.setDeliveryMethod(Constants.SHIPPING_METHOD_STANDARD);
        shipment.setTrackingNumber(trackNumbers);

        orderEntity.setShipmentEntity(shipment);
        OrdersEntity newOrder = ordersRepo.save(orderEntity);

        OrderStatusChangeResp orderStatusChangeResp = setOrderStatusChangeResp(newOrder, newStatus);

        GenericResponse response = new GenericResponse();
        response.setData(orderStatusChangeResp);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    private GenericResponse getGenericResponse(OrdersEntity orderEntity, OrderStatus newStatus, Instant timeNow, PaymentsEntity payment) {

        if (!Objects.equals(newStatus.getStatusCode(), OrderStatus.ORDER_PAYMENT_REJECTED.getStatusCode()))
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

    private List<OrderByStatusResp> mapToOrderByStatusResp(List <IOrderByStatusResp> order, List<IOrderItemListResp> orderItems) {

        List<OrderByStatusResp> responseData = new ArrayList<>();

//        Group order items by orderId to optimize the mapping process
        Map<UUID, List<IOrderItemListResp>> orderItemsMap = orderItems.stream()
                .collect(Collectors.groupingBy(IOrderItemListResp::getOrderId));

//         set order items to each order response
            order.forEach(orders -> {
            List<IOrderItemListResp> itemsMap = orderItemsMap.getOrDefault(orders.getOrderId(),Collections.emptyList());
            List<OrderItemsListResp> orderItemsListResp = mapToOrderItemsListResp(itemsMap);

            List<String> trackingNumbers = getTrackingNumbers(orders.getTrackingNumber());

            OrderByStatusResp orderByStatusResp = new OrderByStatusResp();
            orderByStatusResp.setOrderId(orders.getOrderId());
            orderByStatusResp.setTotalQuantity(orders.getTotalQuantity());
            orderByStatusResp.setTotalAmount(orders.getTotalAmount());
            orderByStatusResp.setNetAmount(orders.getNetAmount());
            orderByStatusResp.setOrderStatus(orders.getOrderStatus());
            orderByStatusResp.setOrderNo(orders.getOrderNo());
            orderByStatusResp.setAddressLabel(orders.getAddressLabel());
            orderByStatusResp.setTrackingNo(trackingNumbers);
            orderByStatusResp.setDeliveryMethod(orders.getDeliveryMethod());
            orderByStatusResp.setRejectionReason(orders.getRejectionReason());
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

    private OrderDetailsResp mapToOrderDetailsResp(IOrderDetailByOrderIdResp orderDetails, List<IOrderItemListResp> orderItemsListResp) {

        List<String> trackingNumbers = getTrackingNumbers(orderDetails.getTrackingNumber());

        OrderDetailByOrderIdResp orderDetailByStatus = new OrderDetailByOrderIdResp();
        orderDetailByStatus.setOrderId(orderDetails.getOrderId());
        orderDetailByStatus.setTotalQuantity(orderDetails.getTotalQuantity());
        orderDetailByStatus.setTotalAmount(orderDetails.getTotalAmount());
        orderDetailByStatus.setNetAmount(orderDetails.getNetAmount());
        orderDetailByStatus.setOrderStatus(orderDetails.getOrderStatus());
        orderDetailByStatus.setOrderNo(orderDetails.getOrderNo());
        orderDetailByStatus.setAddressLabel(orderDetails.getAddressLabel());
        orderDetailByStatus.setDeliveryAddress(orderDetails.getDeliveryAddress());
        orderDetailByStatus.setPostcode(orderDetails.getPostcode());
        orderDetailByStatus.setProvince(orderDetails.getProvince());
        orderDetailByStatus.setDistrict(orderDetails.getDistrict());
        orderDetailByStatus.setSubDistrict(orderDetails.getSubDistrict());
        orderDetailByStatus.setRecipientFirstName(orderDetails.getRecipientFirstName());
        orderDetailByStatus.setRecipientLastName(orderDetails.getRecipientLastName());
        orderDetailByStatus.setRecipientPhone(orderDetails.getRecipientPhone());
        orderDetailByStatus.setPaymentCreatedAt(orderDetails.getPaymentCreatedAt());
        orderDetailByStatus.setPaymentApproveAt(orderDetails.getPaymentApproveAt());
        orderDetailByStatus.setOrderCreatedAt(orderDetails.getOrderCreatedAt());
        orderDetailByStatus.setCompletedAt(orderDetails.getCompletedAt());
        orderDetailByStatus.setTrackingNumber(trackingNumbers.stream().toList());
        orderDetailByStatus.setDeliveryMethod(orderDetails.getDeliveryMethod());
        orderDetailByStatus.setRejectionReason(orderDetails.getRejectionReason());

        List<OrderItemsListResp> orderItemsList = mapToOrderItemsListResp(orderItemsListResp);

        OrderDetailsResp orderDetailsResp = new OrderDetailsResp();
        orderDetailsResp.setOrderDetail(orderDetailByStatus);
        orderDetailsResp.setOrderItems(orderItemsList);
        return orderDetailsResp;
    }

    private OrderStatusChangeResp setOrderStatusChangeResp(OrdersEntity orderEntity, OrderStatus newStatus) {
        OrderStatusChangeResp orderStatusChangeResp = new OrderStatusChangeResp();
        orderStatusChangeResp.setOrderId(orderEntity.getOrderId());
        orderStatusChangeResp.setNewStatus(newStatus.getStatusCode());
        orderStatusChangeResp.setStatusChangedAt(orderEntity.getStatusChangedAt());
        return orderStatusChangeResp;
    }

    private List<String> getTrackingNumbers(String trackingNumber) {
        if (trackingNumber == null || trackingNumber.isEmpty())
            return Collections.emptyList();

        return Arrays.stream(trackingNumber.split(","))
                .map(String::trim)
                .toList();
    }
}
