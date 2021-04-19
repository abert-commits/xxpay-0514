package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.PinduoduoPreOrders;
import org.xxpay.core.entity.PinduoduoPreOrdersExample;
import org.xxpay.core.service.IPinduoduoOrderService;
import org.xxpay.core.service.IPinduoduoPreOrderService;
import org.xxpay.service.dao.mapper.PinduoduoPreOrdersMapper;

import java.util.Date;
import java.util.List;

@Service(interfaceName = "org.xxpay.core.service.IPinduoduoPreOrderService", version = "1.0.0", retries = -1)
public class PinduoduoPreOrderServicelmpl implements IPinduoduoPreOrderService {

    @Autowired
    private PinduoduoPreOrdersMapper preOrdersMapper;

    @Override
    public Integer count(PinduoduoPreOrders order) {
        PinduoduoPreOrdersExample example = new PinduoduoPreOrdersExample();
        PinduoduoPreOrdersExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria,order);
        return preOrdersMapper.countByExample(example);
    }

    @Override
    public List<PinduoduoPreOrders> select(int offset, int limit, PinduoduoPreOrders order) {
        PinduoduoPreOrdersExample example = new PinduoduoPreOrdersExample();
        example.setOrderByClause("ctime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        PinduoduoPreOrdersExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria,order);
        return preOrdersMapper.selectByExample(example);
    }

    @Override
    public int add(PinduoduoPreOrders preOrders) {
        return preOrdersMapper.insert(preOrders);
    }

    @Override
    public int update(PinduoduoPreOrders orders) {
        return preOrdersMapper.updateByPrimaryKeySelective(orders);
    }

    void setCriteria(PinduoduoPreOrdersExample.Criteria criteria, PinduoduoPreOrders orders) {
        if(orders != null) {
            if(orders.getStartingTime() != null) criteria.andStartingTimeLessThanOrEqualTo(orders.getStartingTime());
            if(orders.getEndTime() != null) criteria.andEndTimeGreaterThanOrEqualTo(orders.getEndTime());
            if(orders.getStatus()!=null) criteria.andStatusEqualTo(orders.getStatus());
            if(!StringUtils.isBlank(orders.getName())) criteria.andNameLike("%"+orders.getName()+"%");

        }

    }
}
