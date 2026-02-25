package com.senior.candleShopProject.datasource.repo;
import com.senior.candleShopProject.datasource.domain.orders.IOrderByStatusResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderDetailByStatusResp;
import com.senior.candleShopProject.datasource.domain.orders.IOrderItemListResp;
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
    select  oi.order_id as orderId,
            oi.order_item_id as orderItemId,
            oi.product_name_at_purchase as productName,
            oi.quantity as quantity,
            oi.price_per_unit_at_purchase as pricePerUnit,
            oi.subtotal_at_purchase as subtotal,
            pi.product_img_path as productImagePath
    from order_items oi
    left join product_images pi on pi.product_id = oi.product_id and pi.is_primary = true
    where oi.order_id in :orderIds;
       """, nativeQuery = true)
    List<IOrderItemListResp> getOrderItemByOrderId(@Param("orderIds") List<UUID> orderIds);

    @Query(value = """
        select o.order_id as orderId,
              o.total_quantity as totalQuantity,
              o.total_amount as totalAmount,
              o.net_amount as netAmount,
              o.order_status as orderStatus,
              o.order_no as orderNo,
              osa.address_label as addressLabel
        from orders o
        left join order_shipping_address osa
        on o.order_id = osa.order_id
        where o.customer_id = 'c0a80212-9c4f-190a-819c-4fe930870001'
        and o.order_status = 'PD';
       """, nativeQuery = true)
    List<IOrderByStatusResp> getOrderByCustIdStatus(@Param("customerId") UUID customerId, @Param("status") String status);

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
             pm.created_at as paymentCreateAt,
             o.created_at as orderCreateAt,
             o.completed_at as orderCompletedAt
       from orders o
       join order_shipping_address osa
       on o.order_id = osa.order_id
       join payments pm on pm.order_id = o.order_id
       where o.order_id = :orderId
       and o.order_status = :status;
       """, nativeQuery = true)
    IOrderDetailByStatusResp getOrderDetailByStatus(@Param("orderId") UUID orderId, @Param("status") String status);

}
