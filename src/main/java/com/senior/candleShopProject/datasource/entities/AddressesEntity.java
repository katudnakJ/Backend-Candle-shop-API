package com.senior.candleShopProject.datasource.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name  = "addresses")
public class AddressesEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "address_id")
    private UUID addressId;

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

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "recipient_first_name")
    private String recipientFirstName;

    @Column(name = "recipient_last_name")
    private String recipientLastName;

    @Column(name = "recipient_phone")
    private String recipientPhone;

//    Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity usersEntity;
}
