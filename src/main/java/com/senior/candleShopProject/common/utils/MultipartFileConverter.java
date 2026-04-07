package com.senior.candleShopProject.common.utils;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.UncheckedIOException;

public final class MultipartFileConverter {

    private MultipartFileConverter() {}

    public static Flux<DataBuffer> toDataBufferFlux(MultipartFile file) {
        return DataBufferUtils.readInputStream(
                () -> {
                    try {
                        return file.getInputStream();
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                },
                new DefaultDataBufferFactory(),
                4096
        );
    }
}