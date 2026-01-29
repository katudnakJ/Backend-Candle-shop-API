package com.senior.candleShopProject.datasource.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "users")
public class UsersEntity {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.TIME)
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "line_id")
    private String lineId;

    @Column(name="line_display_name")
    private String lineDisplayName;

    @Column(name="is_seller")
    private Boolean isSeller;

    @Column(name="user_role")
    private String userRole;

}
