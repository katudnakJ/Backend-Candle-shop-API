package com.senior.candleShopProject.datasource.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "order_items")
public class OrderItemsEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "order_item_id")
    private UUID orderItemId;

    @Column(name = "product_name_at_purchase")
    private String productNameAtPurchase;

    @Column(name = "price_at_purchase")
    private BigDecimal priceAtPurchase;

    @Column(name = "quantity")
    private Integer quantity;

//    Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrdersEntity ordersEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductsEntity productsEntity;
}
