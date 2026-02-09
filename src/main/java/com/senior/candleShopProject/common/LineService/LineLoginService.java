package com.senior.candleShopProject.common.LineService;

import com.senior.candleShopProject.common.LineService.dto.LineProfileResp;
import com.senior.candleShopProject.common.ResultCode;
import com.senior.candleShopProject.common.exception.ShopServiceApiException;
import com.senior.candleShopProject.common.exception.ShopUnAuthorizedException;
import org.springframework.stereotype.Service;

@Service
public class LineLoginService {

    private final LineAPIClient lineAPIClient;

    public LineLoginService(LineAPIClient lineAPIClient) {
        this.lineAPIClient = lineAPIClient;
    }

    public LineProfileResp getLineProfile(String token) throws ShopServiceApiException {
//     Check Token format
        if (token == null || !token.startsWith("Bearer "))
            throw new ShopUnAuthorizedException(ResultCode.TOKEN_INVALID," Token format ผิดหรืออาจไม่ได้ส่ง Token");

        String accessToken = token.split("Bearer ")[1];

        if(lineAPIClient.isTokenExpired(accessToken)
            || lineAPIClient.getVerifyResp(accessToken) == null
        )
            throw new ShopUnAuthorizedException(ResultCode.TOKEN_INVALID,"เซสชันหมดอายุ หรือไม่สามารถยืนยันตัวตนได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");

        LineProfileResp profileResp = lineAPIClient.getProfileResp(accessToken);
        if(profileResp == null)
            throw new ShopUnAuthorizedException(ResultCode.TOKEN_INVALID,"ไม่สามารถดึงข้อมูลโปรไฟล์ได้ กรุณาเข้าสู่ระบบใหม่อีกครั้ง");

        return profileResp;
    }

}
