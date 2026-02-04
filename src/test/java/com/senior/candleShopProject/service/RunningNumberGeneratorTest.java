package com.senior.candleShopProject.service;

import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.common.utils.RunningNumberGenerator;
import com.senior.candleShopProject.datasource.repo.OrdersRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RunningNumberGeneratorTest {

    @InjectMocks
    private RunningNumberGenerator generator;

    @Mock
    private OrdersRepo ordersRepo;

    @BeforeEach
    void init() { MockitoAnnotations.openMocks(this); }

    @Test
    void shouldPrefixWithOrderConstantAndAppendSequence() {
        when(ordersRepo.getNextOrderNo()).thenReturn(12345L);
        String orderNo = generator.generateOrderNo(1L);
        assertEquals(Constants.PREFIX_ORDER_NO + 12345L, orderNo);
        verify(ordersRepo, times(1)).getNextOrderNo();
    }

    @Test
    void shouldCallRepositoryEveryTimeToEnsureMonotonicGrowth() {
        when(ordersRepo.getNextOrderNo()).thenReturn(1L, 2L, 3L);
        String first = generator.generateOrderNo(1L);
        String second = generator.generateOrderNo(2L);
        String third = generator.generateOrderNo(3L);
        assertEquals(Constants.PREFIX_ORDER_NO + 1L, first);
        assertEquals(Constants.PREFIX_ORDER_NO + 2L, second);
        assertEquals(Constants.PREFIX_ORDER_NO + 3L, third);
        verify(ordersRepo, times(3)).getNextOrderNo();
    }

    @Test
    void shouldHandleLargeSequenceNumbers() {
        long large = Long.MAX_VALUE - 1;
        when(ordersRepo.getNextOrderNo()).thenReturn(large);
        String orderNo = generator.generateOrderNo(large);
        assertTrue(orderNo.startsWith(Constants.PREFIX_ORDER_NO));
        assertTrue(orderNo.endsWith(String.valueOf(large)));
    }

    @Test
    void shouldNotUsePassedSequenceParameterDirectly() {
        when(ordersRepo.getNextOrderNo()).thenReturn(999L);
        String orderNo = generator.generateOrderNo(123L);
        assertEquals(Constants.PREFIX_ORDER_NO + 999L, orderNo);
    }

    @Test
    void shouldPropagateRepositoryExceptions() {
        when(ordersRepo.getNextOrderNo()).thenThrow(new RuntimeException("db down"));
        RuntimeException ex = assertThrows(RuntimeException.class, () -> generator.generateOrderNo(1L));
        assertEquals("db down", ex.getMessage());
    }
}
