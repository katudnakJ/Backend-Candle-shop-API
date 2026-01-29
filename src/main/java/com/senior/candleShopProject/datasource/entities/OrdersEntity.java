package com.senior.candleShopProject.datasource.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "orders")
public class OrdersEntity {
    @Id
    @Column(name = "order_id")
    private UUID order_id;

    @Column(name = "customer_id")
    private UUID customer_id;

    @Column(name = "total_quantity_amount")
    private Integer totalQuantityAmount;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "net_amount")
    private BigDecimal netAmount;

    @Column(name = "order_status")
    private String orderStatus;

    @Column(name = "order_created_date")
    private Instant orderCreatedDate;

    @Column(name = "total_amount_purchase")
    private BigDecimal totalAmountPurchase;

}
