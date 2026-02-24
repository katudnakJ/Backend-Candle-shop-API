package com.senior.candleShopProject.datasource.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "payments")
public class PaymentsEntity {

    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "create_at")
    private Instant createAt;

    @Column(name = "resubmit_at")
    private Instant resubmitAt;

    @Column(name = "status_changed_at")
    private Instant statusChangedAt;

    @Column(name = "approve_at")
    private Instant approveAt;

    @Column(name= "receipt_number")
    private String receiptNumber;

    @Column(name = "payment_proof_path")
    private String paymentProofPath;

    @Column(name = "rejection_reason")
    private String rejectionReason;

//    Relationships
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name ="order_id",nullable = false)
    private OrdersEntity ordersEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private SellerEntity sellerEntity;
}
