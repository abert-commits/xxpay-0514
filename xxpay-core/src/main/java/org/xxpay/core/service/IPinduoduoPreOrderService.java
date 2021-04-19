package org.xxpay.core.service;

import org.xxpay.core.entity.PinduoduoPreOrders;

import java.util.List;

public interface IPinduoduoPreOrderService {

    Integer count(PinduoduoPreOrders preOrders);

    List<PinduoduoPreOrders> select(int offset, int limit, PinduoduoPreOrders preOrders);

    int add(PinduoduoPreOrders preOrders);

    int update(PinduoduoPreOrders orders);
}
