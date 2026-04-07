package com.senior.candleShopProject.datasource.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Setter
@Getter
@Table(name = "order_shipping_address")
public class OrderShippingAddressEntity {
    @Id
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "delivery_address")
    private String deliveryAddress;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "province")
    private String province;

    @Column(name = "district")
    private String district;

    @Column(name = "sub_district")
    private String subDistrict;

    @Column(name = "address_label")
    private String addressLabel;

    @Column(name = "recipient_first_name")
    private String recipientFirstName;

    @Column(name = "recipient_last_name")
    private String recipientLastName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

//    Relationships
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrdersEntity ordersEntity;

}
