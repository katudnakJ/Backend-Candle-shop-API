package com.senior.candleShopProject.feature.order.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.OrderStatus;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.SupabaseService.Dto.SignedFileUrlResp;
import com.senior.candleShopProject.common.UserCheckTemp;
import com.senior.candleShopProject.common.exception.*;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.LocalDateTimeUtils;
import com.senior.candleShopProject.common.utils.PaginationUtil;
import com.senior.candleShopProject.common.utils.SupabaseStorageUtils;
import com.senior.candleShopProject.common.utils.dto.PaginationBuildResp;
import com.senior.candleShopProject.datasource.domain.orders.*;
import com.senior.candleShopProject.datasource.domain.products.ProductByOrderIdResp;
import com.senior.candleShopProject.datasource.entities.*;
import com.senior.candleShopProject.datasource.repo.*;
import com.senior.candleShopProject.feature.order.controller.dto.request.RejectPaymentReq;
import com.senior.candleShopProject.feature.order.controller.dto.request.TrackOrderReq;
import com.senior.candleShopProject.feature.order.controller.dto.response.OrderByStatusResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.dto.OrderDetailByOrderIdResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.PDFResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.dto.OrderByStatusMappingResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.OrderDetailsResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.dto.OrderItemsListResp;
import com.senior.candleShopProject.feature.order.controller.dto.response.OrderStatusChangeResp;
import com.senior.candleShopProject.feature.order.generator.PDFGenerators;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final UserCheckTemp userCheckTemp;
    private final SupabaseStorageUtils supabaseStorageUtils;
    private final PDFGenerators pdfGenerators;
    private final PaginationUtil paginationUtil;

    private final OrdersRepo ordersRepo;
    private final CarriersRepo carriersRepo;
    private final OrderItemsRepo orderItemsRepo;
    private final PaymentsRepo paymentsRepo;
    private final ProductsRepo productsRepo;

    //    Business logic for order management
    public GenericResponse getAllCarriers() {
        GenericResponse response = new GenericResponse();
        response.setData(carriersRepo.findAll());
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    public GenericResponse getOrderByStatus(UUID userId,String userRole, String status, int page, int size) throws ShopServiceApiException {

        if (status == null || status.isEmpty())
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Status is required.");

        userCheckTemp.checkExistsUser(userId);
        boolean validStatus = OrderStatus.isValidStatus(status);

        UUID customerId = userCheckTemp.getCustomerIdByUserId(userId);

        if (!validStatus)
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Invalid order status.");


        List<IOrderByStatusResp> order ;
        Long totalCounts;

        if ( status.equalsIgnoreCase(OrderStatus.ORDER_PAYMENT_PENDING.getStatusCode())){
            if (userRole.equalsIgnoreCase(Constants.ROLE_SELLER)){
                order = ordersRepo.getOrderByStatusPDSeller(
                        size,
                        page * size
                );
                totalCounts = ordersRepo.countOrdersPDAndPaymentStatusNotRJ();
            }else{
                order = ordersRepo.getOrderByStatusPDCustomer(
                        customerId,
                        size,
                        page * size
                );
                totalCounts = ordersRepo.countByOrderStatusAndCustomersEntity_CustomerId(status, customerId);
            }
        }else{
            order = ordersRepo.getOrderByStatus(
                    customerId,
                    status,
                    (userRole.equalsIgnoreCase(Constants.ROLE_SELLER)),
                    size,
                    page * size
            );
            totalCounts = ordersRepo.countByOrderStatus(status);
        }

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

        List<OrderByStatusMappingResp> ordersResponse = mapToOrderByStatusResp(order ,orderItems);

        PaginationBuildResp pagination = paginationUtil.buildPaginationResp(page, size, totalCounts);

        OrderByStatusResp bodyResponse = new OrderByStatusResp();
        bodyResponse.setOrders(ordersResponse);
        bodyResponse.setPage(page);
        bodyResponse.setSize(size);
        bodyResponse.setStartAt(pagination.getStartAt());
        bodyResponse.setEndAt(pagination.getEndAt());
        bodyResponse.setTotalOrders(pagination.getTotalItems());
        bodyResponse.setHasNext(pagination.isHasNext());

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
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "คำสั่งซื้อนี้ไม่มีสินค้า","Order must contain at least one item.");

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
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่มีสิทธิ์ในการเข้าถึง","User don't have permission.");

        //            Increase total sold of the product when confirm payment
        List<ProductByOrderIdResp> productIds = productsRepo.getProductsByOrderId(orderId);
        List<ProductsEntity> productsEntities = new ArrayList<>();
        productIds.forEach(product -> {
            ProductsEntity productEntity = new ProductsEntity();
            productEntity.setProductId(product.getProductId());
            productEntity.setProductName(product.getProductName());
            productEntity.setPrice(product.getPrice());
            productEntity.setWeight(product.getWeight());
            productEntity.setDescription(product.getDescription());
            productEntity.setSlug(product.getSlug());
            productEntity.setActive(product.isActive());
            productEntity.setFeatured(product.isFeatured());
            productEntity.setProductCreatedDate(product.getProductCreatedDate());
            productEntity.setProductUpdatedDate(product.getProductUpdatedDate());
            productEntity.setTotalSold(product.getTotalSold() + product.getItemSoldQuantity());
            productsEntities.add(productEntity);
        });
        productsRepo.saveAll(productsEntities);

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
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่มีสิทธิ์ในการเข้าถึง", "User don't have permission.");

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

    public GenericResponse trackOrder(UUID userId,String userRole, UUID orderId, TrackOrderReq trackOrderReq) throws ShopServiceApiException {
        userCheckTemp.checkExistsUser(userId);

        if (orderId == null || trackOrderReq.getTrackingNumber().isEmpty())
            throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "กรุณาใส่หมายเลขพัสดุอย่างน้อย 1 หมายเลข","Tracking number is required.");


        if (!userRole.equalsIgnoreCase(Constants.ROLE_SELLER))
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่มีสิทธิ์ในการเข้าถึง", "User don't have permission.");

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

    public GenericResponse getPaymentSlipByOrderId(UUID userId, UUID orderId) throws ShopServiceApiException {

        UUID customerId = userCheckTemp.getCustomerIdByUserId(userId);

        if (customerId == null)
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่มีสิทธิ์ในการเข้าถึง", "User don't have permission.");

        if (!userCheckTemp.isOwnerOfOrder(userId, orderId))
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่มีสิทธิ์ในการเข้าถึง", "User don't have permission.");

        PaymentsEntity payment = paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId);

        if (payment == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND,"Payment not found.");

        SignedFileUrlResp result = supabaseStorageUtils.getSignedPaymentProofImage(
                customerId,
                payment.getPaymentId(),
                payment.getPaymentProofPath(),
                payment.getCreatedAt()
        );

        GenericResponse response = new GenericResponse();
        response.setData(result);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

//    Customer confirm when receive the order and change order status to CP.
    @Transactional
    public GenericResponse confirmReceipt(UUID userId, UUID orderId) throws ShopServiceApiException {

        UUID customerId = userCheckTemp.getCustomerIdByUserId(userId);

        OrdersEntity order = ordersRepo.findById(orderId)
                .orElseThrow(() -> new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Order not found."));

        if (customerId == null ||  order.getCustomersEntity().getCustomerId() != customerId)
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่มีสิทธิ์ในการเข้าถึง", "User don't have permission.");

        if (
                !OrderStatus.validToChangeStatus(order.getOrderStatus(), OrderStatus.ORDER_COMPLETED.getStatusCode())
        ) throw new ShopBadRequestException(ResultCode.BAD_REQUEST, "Only orders with 'TO RECEIVE' status can be confirmed.");

        Instant timeNow = Instant.now();
        order.setOrderStatus(OrderStatus.ORDER_COMPLETED.getStatusCode());
        order.setStatusChangedAt(timeNow);
        order.setCompletedAt(timeNow);

        OrdersEntity newOrder = ordersRepo.save(order);

        GenericResponse response = new GenericResponse();
        response.setData(setOrderStatusChangeResp(newOrder, OrderStatus.ORDER_COMPLETED));
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    @Transactional
    public GenericResponse generateReceiptToPDF (String userRole, UUID userId, UUID orderId) throws ShopServiceApiException, IOException {

        PaymentsEntity paymentsEntity = paymentsRepo.findPaymentsEntitiesByOrdersEntity_OrderId(orderId);

        if (paymentsEntity == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Order not found.");

        if (!OrderStatus.ORDER_PAYMENT_APPROVED.getStatusCode().equalsIgnoreCase(paymentsEntity.getPaymentStatus()))
            throw new ShopConflictException(ResultCode.CONFLICT, "Only payment with 'APPROVED' status can generate receipt PDF.");

        boolean isSeller = userRole.equalsIgnoreCase(Constants.ROLE_SELLER);

        if (!isSeller && !userCheckTemp.isOwnerOfOrder(userId, orderId))
            throw new ShopForbiddenException(ResultCode.FORBIDDEN, "คุณไม่มีสิทธิ์ในการเข้าถึงออเดอร์นี้", "User don't have permission.");

        IReceiptInformationResp receiptInfo = ordersRepo.getReceiptInformationByOrderId(isSeller, userId, orderId);

        if (receiptInfo == null)
            throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Order not found.");

        if (!OrderStatus.isValidStatusForGeneratePDF(receiptInfo.getOrderStatus()))
            throw new ShopConflictException(ResultCode.CONFLICT, "Only orders with 'TO SHIP', 'TO RECEIVE' or 'COMPLETED' status can generate receipt PDF.");

        if (paymentsEntity.getReceiptPath() == null || paymentsEntity.getReceiptPath().isEmpty()) {
//            no receipt generated for this order then generate
            List<IReceiptOrderItemResp> orderItemRespList = orderItemsRepo.getOrderItemsForReceipt(orderId);

            if (orderItemRespList == null || orderItemRespList.isEmpty())
                throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "Order not found.");

            paymentsEntity.setReceiptPath(
                    receiptInfo.getPaymentReceiptNumber() + "." + Constants.CONTENT_TYPE_PDF.split("/")[1]
            );

            PaymentsEntity newPayment = paymentsRepo.save(paymentsEntity);

            Instant timeNow = Instant.now();

            byte[] pdf;
            try {
               pdf = pdfGenerators.generateReceiptPDFWithSignature(receiptInfo, orderItemRespList);
            }catch (Exception e){
                log.error("Error generating receipt PDF for order {}: {}", orderId, e.getMessage());
                throw new ShopServiceApiException(ResultCode.INTERNAL_SERVER_ERROR, "เกิดข้อผิดพลาดระหว่างการดาวโหลดใบเสร็จ", "Failed to generate receipt PDF.");
            }
            System.out.println("PDF size: " + pdf.length);
            try {
                supabaseStorageUtils.uploadReceiptPDF(
                        timeNow, // use current time for PDF
                        pdf,
                        receiptInfo.getCustomerId(),
                        receiptInfo.getPaymentId(),
                        receiptInfo.getPaymentReceiptNumber()
                );
            }catch (Exception e){
                throw new ShopServiceApiException(ResultCode.INTERNAL_SERVER_ERROR, null, "Failed to upload receipt PDF.");
            }
            return  getSignedPdfUrlResponse(newPayment, receiptInfo.getCustomerId());
        }
        return getSignedPdfUrlResponse(paymentsEntity, receiptInfo.getCustomerId());

    }

    //    Extracted method for more readable code and to separate the mapping logic from the main service method

    private List<OrderByStatusMappingResp> mapToOrderByStatusResp(List <IOrderByStatusResp> order, List<IOrderItemListResp> orderItems) {

        List<OrderByStatusMappingResp> responseData = new ArrayList<>();

//        Group order items by orderId to optimize the mapping process
        Map<UUID, List<IOrderItemListResp>> orderItemsMap = orderItems.stream()
                .collect(Collectors.groupingBy(IOrderItemListResp::getOrderId));

//         set order items to each order response
            order.forEach(orders -> {
            List<IOrderItemListResp> itemsMap = orderItemsMap.getOrDefault(orders.getOrderId(),Collections.emptyList());
            List<OrderItemsListResp> orderItemsListResp = mapToOrderItemsListResp(itemsMap);

            List<String> trackingNumbers = getTrackingNumbers(orders.getTrackingNumber());

            OrderByStatusMappingResp orderByStatusMappingResp = new OrderByStatusMappingResp();
            orderByStatusMappingResp.setOrderId(orders.getOrderId());
            orderByStatusMappingResp.setTotalQuantity(orders.getTotalQuantity());
            orderByStatusMappingResp.setTotalAmount(orders.getTotalAmount());
            orderByStatusMappingResp.setNetAmount(orders.getNetAmount());
            orderByStatusMappingResp.setOrderStatus(orders.getOrderStatus());
            orderByStatusMappingResp.setPaymentStatus(orders.getPaymentStatus());
            orderByStatusMappingResp.setOrderCreatedAt(
                    LocalDateTimeUtils.convertInstantToTimeZone(orders.getOrderCreatedAt(), Constants.TIME_ZONE_BANGKOK).toInstant()
            );
            orderByStatusMappingResp.setOrderNo(orders.getOrderNumber());
            orderByStatusMappingResp.setAddressLabel(orders.getAddressLabel());
            orderByStatusMappingResp.setTrackingNo(trackingNumbers);
            orderByStatusMappingResp.setDeliveryMethod(orders.getDeliveryMethod());
            orderByStatusMappingResp.setRejectionReason(orders.getRejectionReason());
            orderByStatusMappingResp.setOrderItems(orderItemsListResp);
            responseData.add(orderByStatusMappingResp);
        });

        return responseData;
    }

    private List<OrderItemsListResp> mapToOrderItemsListResp(List<IOrderItemListResp> itemsMap) {
        return itemsMap.stream().map(item -> {
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
        orderDetailByStatus.setOrderNo(orderDetails.getOrderNumber());
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

    private GenericResponse getSignedPdfUrlResponse(PaymentsEntity paymentsEntity, UUID customerId) throws ShopServiceApiException {

        SignedFileUrlResp signedPdfUrl = supabaseStorageUtils.getSignedReceiptPDFUrl(
                paymentsEntity.getCreatedAt(),
                customerId,
                paymentsEntity.getPaymentId(),
                paymentsEntity.getReceiptPath()
        );

        PDFResp pdfResp = new PDFResp();
        pdfResp.setPdfSignedUrl(signedPdfUrl);
        pdfResp.setPdfName("receipt_" + paymentsEntity.getReceiptPath());
        pdfResp.setExpiresAt(signedPdfUrl.getExpiresAt().toString());

        GenericResponse response = new GenericResponse();
        response.setData(pdfResp);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }
}
