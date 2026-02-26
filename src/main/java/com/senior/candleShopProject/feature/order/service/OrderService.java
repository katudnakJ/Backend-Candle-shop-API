package com.senior.candleShopProject.feature.order.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.UserCheckTemp;
import com.senior.candleShopProject.common.exception.ShopBadRequestException;
import com.senior.candleShopProject.common.exception.ShopConflictException;
import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.utils.CustomizeResponseUtil;
import com.senior.candleShopProject.datasource.domain.orders.IOrderByStatusResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderDetailByStatusResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderItemListResp;
import com.senior.candleShopProject.datasource.entities.CustomersEntity;
import com.senior.candleShopProject.datasource.repo.CarriersRepo;
import com.senior.candleShopProject.datasource.repo.CustomersRepo;
import com.senior.candleShopProject.datasource.repo.OrderItemsRepo;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.feature.order.controller.dto.response.OrderByStatusResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.OrderDetailsResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.OrderItemsListResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserCheckTemp userCheckTemp;

    private final OrdersRepo ordersRepo;
    private final CarriersRepo carriersRepo;
    private final CustomersRepo customersRepo;
    private final OrderItemsRepo orderItemsRepo;

    public GenericResponse getAllCarriers() {
        GenericResponse response = new GenericResponse();
        response.setData(carriersRepo.findAll());
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    public GenericResponse getOrderByStatus(UUID userId, String status) throws ShopServiceApiException {
        userCheckTemp.checkExistsUser(userId);
        boolean validStatus = OrderStatus.isValidStatus(status);

        UUID customerId = userCheckTemp.getCustomerId(userId);

        if (!validStatus)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Invalid order status.");

        List<IOrderByStatusResp> order = ordersRepo.getOrderByCustIdStatus(customerId, status,(userCheckTemp.getSellerId(userId) != null));
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
            orderByStatusResp.setOrderStatus(orderItem.getOrderStatus());
            orderByStatusResp.setOrderNo(orderItem.getOrderNo());
            orderByStatusResp.setAddressLabel(orderItem.getAddressLabel());
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
}
