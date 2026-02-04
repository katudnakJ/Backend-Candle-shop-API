package com.senior.candleShopProject.datasource.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "seller")
public class SellerEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "seller_id")
    private UUID sellerId;

    @Column(name = "bank_qrpayment_img_path")
    private String bankQrPaymentImgPath;

//    Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UsersEntity usersEntity;

    @OneToMany(mappedBy = "sellerEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentsEntity> paymentsEntities = new ArrayList<>();
}
