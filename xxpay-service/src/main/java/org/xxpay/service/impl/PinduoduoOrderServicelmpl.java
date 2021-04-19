package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.*;
import org.xxpay.core.service.IPinduoduoOrderService;
import org.xxpay.service.dao.mapper.PinduoduoOrdersMapper;

import java.util.Date;
import java.util.List;

@Service(interfaceName = "org.xxpay.core.service.IPinduoduoOrderService", version = "1.0.0", retries = -1)
public class PinduoduoOrderServicelmpl implements IPinduoduoOrderService {

    @Autowired
    private PinduoduoOrdersMapper ordersMapper;

    @Override
    public Integer count(PinduoduoOrders order, Date createTimeStart, Date createTimeEnd) {
        PinduoduoOrdersExample example = new PinduoduoOrdersExample();
        PinduoduoOrdersExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria,order,createTimeStart,createTimeEnd);


        return ordersMapper.countByExample(example);
    }

    @Override
    public List<PinduoduoOrders> select(int offset, int limit, PinduoduoOrders order, Date createTimeStart, Date createTimeEnd) {
        PinduoduoOrdersExample example = new PinduoduoOrdersExample();
        example.setOrderByClause("ctime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        PinduoduoOrdersExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria,order,createTimeStart,createTimeEnd);
        return ordersMapper.selectByExample(example);
    }

    @Override
    public void add(PinduoduoOrders orders) {
        ordersMapper.insert(orders);
    }

    @Override
    public int update(PinduoduoOrders orders1) {
        return ordersMapper.updateByPrimaryKeySelective(orders1);
    }

    @Override
    public int updateStatus(String orderSn, PinduoduoOrders orders) {
        PinduoduoOrdersExample example = new PinduoduoOrdersExample();
        PinduoduoOrdersExample.Criteria criteria = example.createCriteria();
        criteria.andOrderSnEqualTo(orderSn);
        return ordersMapper.updateByExampleSelective(orders,example);
    }

    @Override
    public List<PinduoduoOrders> getOrdersByStatus(PinduoduoOrders orders) {
        PinduoduoOrdersExample example = new PinduoduoOrdersExample();
        PinduoduoOrdersExample.Criteria criteria = example.createCriteria();
        if(orders.getStatus()!=null) criteria.andStatusEqualTo(orders.getStatus());
        if(orders.getMtime()!=null) criteria.andMtimeGreaterThanOrEqualTo(orders.getMtime());

        return ordersMapper.selectByExample(example);
    }

    void setCriteria(PinduoduoOrdersExample.Criteria criteria, PinduoduoOrders orders,Date createTimeStart, Date createTimeEnd) {
        if(orders != null) {
            if(createTimeStart != null) criteria.andCtimeGreaterThanOrEqualTo(createTimeStart);
            if(createTimeEnd != null) criteria.andCtimeLessThanOrEqualTo(createTimeEnd);
            if(StringUtils.isNotBlank(orders.getApiOrderSn())) criteria.andApiOrderSnEqualTo(orders.getApiOrderSn());
            if(StringUtils.isNotBlank(orders.getOrderSn())) criteria.andOrderSnEqualTo(orders.getOrderSn());
            if(orders.getFromPlatform()!=null) criteria.andFromPlatformEqualTo(orders.getFromPlatform());
            if(orders.getIsPay()!=null) criteria.andIsPayEqualTo(orders.getIsPay());
            if(orders.getPayType()!=null) criteria.andPayTypeEqualTo(orders.getPayType());
            if(orders.getStatus()!=null) criteria.andStatusEqualTo(orders.getStatus());
            if(orders.getTotal()!=null) criteria.andTotalEqualTo(orders.getTotal());

        }

    }
}
