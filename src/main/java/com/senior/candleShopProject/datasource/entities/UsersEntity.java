package com.senior.candleShopProject.datasource.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "users")
public class UsersEntity {
    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "line_id")
    private String lineId;

    @Column(name="line_display_name")
    private String lineDisplayName;

    @Column(name="is_seller")
    private Boolean isSeller;

}
