package com.senior.candleShopProject.datasource.repo;

import com.senior.candleShopProject.datasource.domain.orders.IOrderItemListResp;
import com.senior.candleShopProject.datasource.entities.OrderItemsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemsRepo extends JpaRepository<OrderItemsEntity, UUID> {
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
    List<IOrderItemListResp> getOrderItemByOrderIds(@Param("orderIds") List<UUID> orderIds);
}
