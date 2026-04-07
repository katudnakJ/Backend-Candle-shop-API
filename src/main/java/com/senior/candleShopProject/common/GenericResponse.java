package com.senior.candleShopProject.common;

import lombok.Data;

@Data
public class GenericResponse {
    private Status status;
    private Object data;
}
