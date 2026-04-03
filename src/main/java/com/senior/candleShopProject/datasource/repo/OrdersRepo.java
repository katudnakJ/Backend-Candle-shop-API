package com.senior.candleShopProject.datasource.repo;
import com.senior.candleShopProject.datasource.domain.orders.*;
import com.senior.candleShopProject.datasource.entities.OrdersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrdersRepo extends JpaRepository<OrdersEntity, UUID> {
    @Query(value = """
        select count(*) AS OrderPdCount
        from orders
        where order_status = :status;
       """, nativeQuery = true)
    Integer getCountOrdersWithStatus(@Param("status") String status);

    @Query(value = "SELECT NEXTVAL('order_no_seq')", nativeQuery = true)
    Long getNextOrderNo();

    @Query(value = """
            select o.order_id as orderId,
                  o.total_quantity as totalQuantity,
                  o.total_amount as totalAmount,
                  o.net_amount as netAmount,
                  o.order_status as orderStatus,
                  o.created_at as orderCreatedAt,
                  pm.payment_status as paymentStatus,
                  o.order_number as orderNumber,
                  osa.address_label as addressLabel,
                  sm.tracking_number as trackingNumber,
                  sm.delivery_method as deliveryMethod,
                  pm.rejection_reason as rejectionReason
            from orders o
            left join order_shipping_address osa
            on o.order_id = osa.order_id
            left join payments pm on pm.order_id = o.order_id
            left join shipment sm on sm.order_id = o.order_id
        where (:isSeller = TRUE or o.customer_id = :customerId)
        and o.order_status = :status
        ORDER BY o.created_at ASC
        LIMIT :limit OFFSET :offset;
       """, nativeQuery = true)
    List<IOrderByStatusResp> getOrderByStatus(@Param("customerId") UUID customerId,
                                              @Param("status") String status,
                                              @Param("isSeller") Boolean isSeller,
                                              @Param("limit") int limit,
                                              @Param("offset") int offset
    );

    @Query(value = """

            SELECT * FROM (
      select o.order_id as orderId,
            o.total_quantity as totalQuantity,
            o.total_amount as totalAmount,
            o.net_amount as netAmount,
            o.order_status as orderStatus,
            o.created_at as orderCreatedAt,
            pm.payment_status as paymentStatus,
            o.order_number as orderNumber,
            osa.address_label as addressLabel,
            sm.tracking_number as trackingNumber,
            sm.delivery_method as deliveryMethod,
            pm.rejection_reason as rejectionReason,
            1 as priority
      from orders o
      left join order_shipping_address osa
      on o.order_id = osa.order_id
      left join payments pm on pm.order_id = o.order_id
      left join shipment sm on sm.order_id = o.order_id
        where o.customer_id = :customerId
        and o.order_status = 'PD'
        and pm.payment_status = 'RJ'

        UNION ALL

        select o.order_id as orderId,
            o.total_quantity as totalQuantity,
            o.total_amount as totalAmount,
            o.net_amount as netAmount,
            o.order_status as orderStatus,
            o.created_at as orderCreatedAt,
            pm.payment_status as paymentStatus,
            o.order_number as orderNumber,
            osa.address_label as addressLabel,
            sm.tracking_number as trackingNumber,
            sm.delivery_method as deliveryMethod,
            pm.rejection_reason as rejectionReason,
            2 as priority
        from orders o
        left join order_shipping_address osa
        on o.order_id = osa.order_id
        left join payments pm on pm.order_id = o.order_id
        left join shipment sm on sm.order_id = o.order_id
        where o.customer_id = :customerId
        and o.order_status = 'PD'
        and pm.payment_status != 'RJ'
    )   AS CombinedOrdersStatusPD
        ORDER BY priority ASC,
        orderCreatedAt ASC
        LIMIT :limit OFFSET :offset;
""", nativeQuery = true)
    List<IOrderByStatusResp> getOrderByStatusPDCustomer(@Param("customerId") UUID customerId,
                                                        @Param("limit") int limit,
                                                        @Param("offset") int offset);

    @Query(value = """
            select o.order_id as orderId,
                  o.total_quantity as totalQuantity,
                  o.total_amount as totalAmount,
                  o.net_amount as netAmount,
                  o.order_status as orderStatus,
                  o.created_at as orderCreatedAt,
                  pm.payment_status as paymentStatus,
                  o.order_number as orderNumber,
                  osa.address_label as addressLabel,
                  sm.tracking_number as trackingNumber,
                  sm.delivery_method as deliveryMethod,
                  pm.rejection_reason as rejectionReason
            from orders o
            left join order_shipping_address osa
            on o.order_id = osa.order_id
            left join payments pm on pm.order_id = o.order_id
            left join shipment sm on sm.order_id = o.order_id
        where o.order_status = 'PD'
        and pm.payment_status != 'RJ'
        ORDER BY o.created_at ASC
        LIMIT :limit OFFSET :offset;
       """, nativeQuery = true)
    List<IOrderByStatusResp> getOrderByStatusPDSeller(@Param("limit") int limit,
                                                        @Param("offset") int offset);


    @Query(value = """
       select o.order_id as orderId,
             o.total_quantity as totalQuantity,
             o.total_amount as totalAmount,
             o.net_amount as netAmount,
             o.order_status as orderStatus,
             o.order_number as orderNumber,
             osa.address_label as addressLabel,
             osa.delivery_address as deliveryAddress,
             osa.postcode as postcode,
             osa.province as province,
             osa.district as district,
             osa.sub_district as subDistrict,
             osa.recipient_first_name as recipientFirstName,
             osa.recipient_last_name as recipientLastName,
             osa.recipient_phone as recipientPhone,
             pm.created_at as paymentCreatedAt,
             pm.approve_at as paymentApproveAt,
             o.created_at as orderCreatedAt,
             o.completed_at as orderCompletedAt,
             sm.tracking_number as trackingNumber,
             sm.delivery_method as deliveryMethod,
             pm.rejection_reason as rejectionReason
       from orders o
       left join order_shipping_address osa on o.order_id = osa.order_id
       left join payments pm on pm.order_id = o.order_id
       left join shipment sm on sm.order_id = o.order_id
       where o.order_id = :orderId;
       """, nativeQuery = true)
    IOrderDetailByOrderIdResp getOrderDetailByOrderId(@Param("orderId") UUID orderId);

    boolean existsByOrderIdAndCustomersEntity_UsersEntity_UserId(UUID orderId, UUID userId);

    @Query(value ="""
select p.payment_id as paymentId,
        p.receipt_number as paymentReceiptNumber,
        o.order_number as orderNumber,
        o.total_amount as orderTotalAmount,
        o.net_amount as orderNetAmount,
        o.order_status as orderStatus,
        sad.sellerFirstName,
        sad.sellerLastName,
        sad.sellerPhone,
        sad.sellerDeliveryAddress,
        sad.sellerPostcode,
        sad.sellerProvince,
        sad.sellerDistrict,
        sad.sellerSubDistrict,
        c.customer_id as customerId,
        osa.recipient_first_name as customerFirstName,
        osa.recipient_last_name as customerLastName,
        osa.recipient_phone as customerPhone,
        osa.delivery_address as customerDeliveryAddress,
        osa.postcode as customerPostcode,
        osa.province as customerProvince,
        osa.district as customerDistrict,
        osa.sub_district as customerSubDistrict
    from orders o
    join payments p on o.order_id = p.order_id
    join customers c on c.customer_id = o.customer_id
    join users u on u.user_id = c.user_id
    CROSS JOIN (
    SELECT
        ad.recipient_first_name AS sellerFirstName,
        ad.recipient_last_name AS sellerLastName,
        ad.recipient_phone AS sellerPhone,
        ad.delivery_address AS sellerDeliveryAddress,
        ad.postcode AS sellerPostcode,
        ad.province AS sellerProvince,
        ad.district AS sellerDistrict,
        ad.sub_district AS sellerSubDistrict
      FROM addresses ad
      JOIN seller s ON s.user_id = ad.user_id
      WHERE s.is_owner = true
      AND ad.is_default = true
      LIMIT 1
    ) sad
    left join order_shipping_address osa on osa.order_id = o.order_id
    where p.payment_status = 'AP'
    and o.order_status in ('TS','TR','CP')
    and (
      :isSeller = true
      or u.user_id = :userId
    )
    and o.order_id = :orderId;
""", nativeQuery = true)
    IReceiptInformationResp getReceiptInformationByOrderId(
            @Param("isSeller") boolean isSeller, @Param("userId") UUID userId, @Param("orderId") UUID orderId);

    Long countByOrderStatus(String orderStatus);


    @Query(value = """
       select count(*)
       from orders o
       left join payments p
       on o.order_id = p.order_id
       where p.payment_status != 'RJ'
       and o.order_status = 'PD';
       """, nativeQuery = true)
    Long countOrdersPDAndPaymentStatusNotRJ();

    Long countByOrderStatusAndCustomersEntity_CustomerId(String status, UUID customerId);

    @Query("SELECT NEW com.senior.candleShopProject.datasource.domain.orders.ReportOrderOfRangeResp(" +
            "CAST(SUM( CASE " +
            "       WHEN o.totalAmount < 0 THEN 0 " +
            "       ELSE o.totalAmount " +
            "    END ) as BIGDECIMAL ), " +
            "COUNT(o.orderId), " +
            "COUNT(CASE WHEN o.orderStatus = 'TS' THEN 1 END)," +
            "COUNT(CASE WHEN o.orderStatus = 'TR' THEN 1 END)," +
            "COUNT(CASE WHEN o.orderStatus = 'CP' THEN 1 END)) " +
            "FROM OrdersEntity o " +
            "JOIN PaymentsEntity p ON o.orderId = p.ordersEntity.orderId " +
            "AND p.paymentStatus = 'AP'" +
            "WHERE o.orderCreatedAt >= :startOfMonthFirstDay " +
            "AND o.orderCreatedAt < :nextMonthFirstDay " +
            "AND o.orderStatus IN ('TS', 'TR', 'CP') ")
    ReportOrderOfRangeResp findReportByRange(
            @Param("startOfMonthFirstDay") Instant startOfMonthFirstDay,
            @Param("nextMonthFirstDay") Instant nextMonthFirstDay
    );

    @Query("SELECT NEW com.senior.candleShopProject.datasource.domain.orders.ReportOfRangeLastMonthResp(" +
            "CAST(SUM( CASE " +
            "       WHEN o.totalAmount < 0 THEN 0 " +
            "       ELSE o.totalAmount " +
            "    END ) as BIGDECIMAL ), " +
            "COUNT(o.orderId)) " +
            "FROM OrdersEntity o " +
            "JOIN PaymentsEntity p ON o.orderId = p.ordersEntity.orderId " +
            "AND p.paymentStatus = 'AP'" +
            "WHERE o.orderCreatedAt >= :startOfMonthFirstDay " +
            "AND o.orderCreatedAt < :nextMonthFirstDay " +
            "AND o.orderStatus IN ('TS', 'TR', 'CP') ")
    ReportOfRangeLastMonthResp findReportByRangeLastMonth(
            @Param("startOfMonthFirstDay") Instant startOfMonthFirstDay,
            @Param("nextMonthFirstDay") Instant nextMonthFirstDay
    );

    @Query(value = """
       select count(*) as totalNewCustomerThisMonth
       from users u
       WHERE u.created_at >= :startOfMonthFirstDay
       AND u.created_at < :nextMonthFirstDay
    """, nativeQuery = true)
    Long countNewCustomerThisMonth(
            @Param("startOfMonthFirstDay") Instant startOfMonthFirstDay,
            @Param("nextMonthFirstDay") Instant nextMonthFirstDay
    );

    @Query(value = """
        select oi.product_id as productId,
            oi.product_name_at_purchase as productName,
            SUM(oi.quantity) as totalQuantitySales
        from orders o
        join order_items oi on o.order_id = oi.order_id
        where o.created_at < :nextMonthFirstDay
        and o.created_at >= :startOfMonthFirstDay
        group by oi.product_id, oi.product_name_at_purchase
        order by totalQuantitySales DESC
        limit :limit;
""", nativeQuery = true)
    List<IReportTopSellingProductsResp> findTopSellingProductsByRange(
            @Param("startOfMonthFirstDay") Instant startOfMonthFirstDay,
            @Param("nextMonthFirstDay") Instant nextMonthFirstDay,
            @Param("limit") int limit
    );


    @Query(value = """
    SELECT
        COALESCE(oi.product_name_at_purchase, p.product_name) AS productName,
        COALESCE(oi.price_per_unit_at_purchase, p.price) AS pricePerUnit,
        COALESCE(SUM(oi.quantity), 0) AS totalSales,
        COALESCE(SUM(oi.subtotal_at_purchase), 0) AS subtotal
    FROM products p
    LEFT JOIN order_items oi ON p.product_id = oi.product_id
    LEFT JOIN orders o ON o.order_id = oi.order_id
        AND o.created_at >= :startOfMonthFirstDay
        AND o.created_at < :nextMonthFirstDay
        AND o.order_status IN ('TS', 'TR', 'CP')
    LEFT JOIN payments pm ON pm.order_id = o.order_id
        AND pm.payment_status = 'AP'
    GROUP BY
        p.product_id,
        p.product_name,
        p.price,
        oi.product_name_at_purchase,
        oi.price_per_unit_at_purchase
    ORDER BY p.product_id;
""", nativeQuery = true)
    List<IOrdersReportDataResp> findOrdersReportDataByRange(
            @Param("startOfMonthFirstDay") Instant startOfMonthFirstDay,
            @Param("nextMonthFirstDay") Instant nextMonthFirstDay
    );

}
