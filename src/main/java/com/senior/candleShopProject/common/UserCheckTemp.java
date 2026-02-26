package com.senior.candleShopProject.common;

import com.senior.candleShopProject.common.exception.ShopDataNotFoundException;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.datasource.repo.CustomersRepo;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import com.senior.candleShopProject.datasource.repo.SellerRepo;
import com.senior.candleShopProject.datasource.repo.UsersRepo;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UserCheckTemp{

    static UsersRepo usersRepo;
    static CustomersRepo customersRepo;
    static SellerRepo sellerRepo;

    public static void isExistsUser(UUID userId) throws ShopServiceApiException {
         if (!usersRepo.existsById(userId))
             throw new ShopDataNotFoundException(ResultCode.DATA_NOT_FOUND, "User not found.");
    }
    public static boolean isCustomer(UUID userId) {
        return customersRepo.existsCustomersEntitiesByUsersEntity_UserId(userId);
    }

    public static boolean isSeller(UUID userId){
        return sellerRepo.existsByUsersEntity_UserId(userId);
    }
}
