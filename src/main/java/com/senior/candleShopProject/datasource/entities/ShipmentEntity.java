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
@Table(name = "shipment")
public class ShipmentEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "shipment_id")
    private UUID shipmentId;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "delivery_method")
    private String deliveryMethod;

    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;

    @Column(name = "tracking_number")
    private String trackingNumber;

//    Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrdersEntity ordersEntity;
}
