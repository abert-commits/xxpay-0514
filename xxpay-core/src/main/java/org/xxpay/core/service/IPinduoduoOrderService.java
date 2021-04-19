package org.xxpay.core.service;

import org.xxpay.core.entity.PinduoduoOrders;

import java.util.Date;
import java.util.List;


public interface IPinduoduoOrderService {

    Integer count(PinduoduoOrders order, Date createTimeStart, Date createTimeEnd);

    List<PinduoduoOrders> select(int offset, int limit, PinduoduoOrders order, Date createTimeStart, Date createTimeEnd);

    void add(PinduoduoOrders orders);

    int update(PinduoduoOrders orders1);

    int updateStatus(String orderSn, PinduoduoOrders orders);

    List<PinduoduoOrders> getOrdersByStatus(PinduoduoOrders orders);
}
