package com.senior.candleShopProject.datasource.repo;
import com.senior.candleShopProject.datasource.domain.orders.IOrderByStatusResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderDetailByStatusResp;
import com.senior.candleShopProject.datasource.entities.OrdersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
                  o.order_no as orderNo,
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
        and o.order_status = :status;
       """, nativeQuery = true)
    List<IOrderByStatusResp> getOrderByCustIdStatus(@Param("customerId") UUID customerId,
                                                    @Param("status") String status,
                                                    @Param("isSeller") Boolean isSeller);

    @Query(value = """
            select o.order_id as orderId,
             o.total_quantity as totalQauntity,
             o.total_amount as totalAmount,
             o.net_amount as netAmount,
             o.order_status as orderStatus,
             o.order_no as orderNo,
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
             o.created_at as orderCreatedAt,
             o.completed_at as orderCompletedAt
       from orders o
       join order_shipping_address osa
       on o.order_id = osa.order_id
       join payments pm on pm.order_id = o.order_id
       where o.order_id = :orderId;
       """, nativeQuery = true)
    IOrderDetailByStatusResp getOrderDetailByOrderId(@Param("orderId") UUID orderId);

    boolean existsByOrderIdAndCustomersEntity_UsersEntity_UserId(UUID orderId, UUID userId);
}
