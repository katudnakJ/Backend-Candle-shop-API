package com.senior.candleShopProject.feature.shoppingCart.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.utils.JwtUtils;
import com.senior.candleShopProject.datasource.entities.ShoppingCartEntity;
import com.senior.candleShopProject.datasource.domain.IShoppingCartResp;import com.senior.candleShopProject.datasource.repo.ShoppingCartRepo;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.response.ShoppingCartItemsList;
import com.senior.candleShopProject.feature.shoppingCart.controller.dto.response.ShoppingCartResp;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingCartService {

    @Value("${app.storage.public-image-base-url}")
    private String publicImageBaseUrl;

    private final ShoppingCartRepo shoppingCartRepo;

    public GenericResponse getShoppingCart(UUID userId) throws ShopServiceApiException {

        List<IShoppingCartResp> iShoppingCartResps = shoppingCartRepo.getShoppingCartByUserId(userId);

        if (iShoppingCartResps.isEmpty()){
            GenericResponse response = new GenericResponse();
            response.setData(null);
            response.setStatus(ResultCode.SUCCESS);
            return response;
        }

        List<ShoppingCartItemsList> shoppingCartItemsList = new ArrayList<>();
        shoppingCartItemsList.add(getShoppingCartItemsList(iShoppingCartResps));

        ShoppingCartResp shoppingCartResp = new ShoppingCartResp();
        shoppingCartResp.setShoppingCartId(iShoppingCartResps.get(0).getShoppingCartId());
        shoppingCartResp.setItems(shoppingCartItemsList);

        GenericResponse response = new GenericResponse();
        response.setData(shoppingCartResp);
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }

    ShoppingCartItemsList getShoppingCartItemsList(List <IShoppingCartResp> iShoppingCartResp) {

        ShoppingCartItemsList shoppingCartItemsList = new ShoppingCartItemsList();

        for (IShoppingCartResp cart : iShoppingCartResp) {
            ShoppingCartItemsList cartItemsList = new ShoppingCartItemsList();
            cartItemsList.setShoppingCartItemId(cart.getShoppingCartItemId());
            cartItemsList.setProductId(cart.getProductId());
            cartItemsList.setQuantity(cart.getQuantity());
            cartItemsList.setProductName(cart.getProductName());
            cartItemsList.setPrice(cart.getPrice());
            cartItemsList.setWeight(cart.getWeight());
            cartItemsList.setDescription(cart.getDescription());
            cartItemsList.setProductSlug(cart.getProductSlug());
            if(cart.getProductImgPath() == null)
                cartItemsList.setProductImgPath(null);
            else
                cartItemsList.setProductImgPath(publicImageBaseUrl + cart.getProductImgPath());
        }
        return shoppingCartItemsList;

    }
}
