package com.senior.candleShopProject.common.LineService.dto;

import lombok.Data;

import java.util.List;

@Data
public class LinePushMessageReq {
    private String to;
    private List<LineMessage> messages;
}
