package org.xxpay.core.service;

import org.xxpay.core.entity.OrderCashColl;
import org.xxpay.core.entity.PayCashCollConfig;

import java.util.List;

public interface IOrderCashCollService {

    int add(OrderCashColl record);

    int update(OrderCashColl record);

    List<PayCashCollConfig> select(int offset, int limit, OrderCashColl record);

    
}
