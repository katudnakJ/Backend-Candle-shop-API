package com.senior.candleShopProject.common.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class ShippingUtils {

    public static BigDecimal calculateShippingCost(int totalQuantity) {

        if ( totalQuantity >= 100){
            BigDecimal roundedPrice =
                    BigDecimal.valueOf(totalQuantity)
                            .divide(
                                    BigDecimal.valueOf(Constants.SHIPPING_ITEMS_PER_BOX),
                                    0,
                                    RoundingMode.DOWN
                            );
            BigDecimal remainder = BigDecimal.valueOf(totalQuantity)
                    .remainder(
                            BigDecimal.valueOf(Constants.SHIPPING_ITEMS_PER_BOX)
                    );

            return (roundedPrice.multiply(Constants.SHIPPING_PRICE_MORE_THAN_10))
                            .add(remainder.equals(BigDecimal.ZERO) ?
                                     BigDecimal.ZERO :
                                    isLessThan10(remainder.intValue()) ?
                                            Constants.SHIPPING_PRICE_NOT_MORE_THAN_10 :
                                                    Constants.SHIPPING_PRICE_MORE_THAN_10
                            );
        }
        return isLessThan10(totalQuantity) ?
                Constants.SHIPPING_PRICE_NOT_MORE_THAN_10
                : Constants.SHIPPING_PRICE_MORE_THAN_10;

    }

    private static boolean isLessThan10(int quantity) {
        return quantity <= 10;
    }

}
