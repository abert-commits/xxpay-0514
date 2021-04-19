package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.OrderCashColl;
import org.xxpay.core.entity.OrderCashCollExample;

import java.util.List;

public interface OrderCashCollMapper {
    int countByExample(OrderCashCollExample example);

    int deleteByExample(OrderCashCollExample example);

    int deleteByPrimaryKey(String payOrderId);

    int insert(OrderCashColl record);

    int insertSelective(OrderCashColl record);

    List<OrderCashColl> selectByExample(OrderCashCollExample example);

    OrderCashColl selectByPrimaryKey(String payOrderId);

    int updateByExampleSelective(@Param("record") OrderCashColl record, @Param("example") OrderCashCollExample example);

    int updateByExample(@Param("record") OrderCashColl record, @Param("example") OrderCashCollExample example);

    int updateByPrimaryKeySelective(OrderCashColl record);

    int updateByPrimaryKey(OrderCashColl record);
}