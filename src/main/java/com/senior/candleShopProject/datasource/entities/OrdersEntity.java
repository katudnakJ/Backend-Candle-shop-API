package com.senior.candleShopProject.datasource.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "orders")
public class OrdersEntity {
    @Id
    @Column(name = "order_id")
    private UUID order_id;

    @Column(name="order_no")
    private String orderNo;

    @Column(name = "total_quantity")
    private Integer totalQuantity;

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

//    Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomersEntity customersEntity;

    @OneToMany(mappedBy = "ordersEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemsEntity> orderItemsEntities = new ArrayList<>();

    @OneToOne(mappedBy = "ordersEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private PaymentsEntity paymentsEntity;

    @OneToMany(mappedBy = "ordersEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShipmentEntity> shipmentsEntities = new ArrayList<>();
}
