package com.senior.candleShopProject.common;

import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.entities.CustomersEntity;
import com.senior.candleShopProject.datasource.entities.UsersEntity;
import com.senior.candleShopProject.datasource.repo.CustomersRepo;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.datasource.repo.SellerRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserCheckTemp{

    private final UsersRepo usersRepo;
    private final CustomersRepo customersRepo;
    private final SellerRepo sellerRepo;
    private final OrdersRepo ordersRepo;

    public UserCheckTemp(UsersRepo usersRepo, CustomersRepo customersRepo, SellerRepo sellerRepo, OrdersRepo ordersRepo) {
        this.usersRepo = usersRepo;
        this.customersRepo = customersRepo;
        this.sellerRepo = sellerRepo;
        this.ordersRepo = ordersRepo;
    }

    public void checkExistsUser(UUID userId) throws ShopServiceApiException {
         if (!usersRepo.existsById(userId))
             throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User not found.");
    }
    public UUID getCustomerId(UUID userId) throws ShopDataNotFoundException {
        return customersRepo.findCustomersEntitiesByUsersEntity_UserId(userId).get().getCustomerId();
    }

    public UUID getSellerId(UUID userId){
        return sellerRepo.getSellerByUserId(userId)
                .getSellerId();
    }

    public boolean isOwnerOfOrder(UUID userId, UUID orderId) throws ShopDataNotFoundException {
        return ordersRepo.existsByOrderIdAndCustomersEntity_UsersEntity_UserId(orderId, userId);
    }
}
