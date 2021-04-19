package org.xxpay.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.OrderCashColl;
import org.xxpay.core.entity.OrderCashCollExample;
import org.xxpay.core.entity.PayCashCollConfig;
import org.xxpay.core.entity.PayCashCollConfigExample;
import org.xxpay.core.service.IOrderCashCollService;
import org.xxpay.service.dao.mapper.OrderCashCollMapper;
import org.xxpay.service.dao.mapper.PayCashCollConfigMapper;

import java.util.List;

public class OrderCashCollServiceImpl implements IOrderCashCollService {

    @Autowired
    private OrderCashCollMapper orderCashCollMapper;

    @Override
    public int add(OrderCashColl record) {
        int count=this.orderCashCollMapper.insertSelective(record);
        return  count;
    }

    @Override
    public int update(OrderCashColl record) {
        int count=this.orderCashCollMapper.updateByPrimaryKeySelective(record);
        return count;
    }

    @Override
    public List<PayCashCollConfig> select(int offset, int limit, OrderCashColl record) {
        OrderCashCollExample orderCashCollExample=new OrderCashCollExample();
        OrderCashCollExample example = new OrderCashCollExample();
        example.setOrderByClause("status desc");
        example.setOffset(offset);
        example.setLimit(limit);
        OrderCashCollExample.Criteria criteria = example.createCriteria();
//        setCriteria(criteria, config);
//        return recordMapper.selectByExample(example);
        return null;
    }
}
