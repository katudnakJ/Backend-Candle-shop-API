package com.senior.candleShopProject.datasource.repo;
import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.datasource.entities.OrdersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrdersRepo extends JpaRepository<OrdersEntity, UUID> {
    @Query(value = """
        select count(*) AS OrderPdCount
        from orders
        where order_status = :status;
       """, nativeQuery = true)
    Integer getCountOrdersWithStatus(@Param("status") String status);

    @Query(value = "select nextval('" + Constants.DB_SEQUENCE_ORDER_NAME + "')", nativeQuery = true)
    Long getNextOrderNo();
}
