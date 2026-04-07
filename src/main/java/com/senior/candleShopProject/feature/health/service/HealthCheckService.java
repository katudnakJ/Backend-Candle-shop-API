package com.senior.candleShopProject.feature.health.service;

import com.senior.candleShopProject.common.GenericResponse;
import com.senior.candleShopProject.common.ResultCode;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckService {
    public GenericResponse health() {
        GenericResponse response = new GenericResponse();
        response.setData("Health check endpoint called");
        response.setStatus(ResultCode.SUCCESS);
        return response;
    }
}
