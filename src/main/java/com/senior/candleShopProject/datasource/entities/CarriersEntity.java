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
@Table(name = "carriers")
public class CarriersEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "carrier_id")
    private UUID carrierId;

    @Column(name = "carrier_name")
    private String carrierName;

    @Column(name = "shipping_fee")
    private BigDecimal shippingFee;

//    Relationships
    @OneToOne(mappedBy = "carriersEntity")
    private ShipmentEntity shipmentEntity;
}
