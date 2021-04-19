package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.PinduoduoPreOrders;
import org.xxpay.core.entity.PinduoduoPreOrdersExample;

import java.util.List;

public interface PinduoduoPreOrdersMapper {
    int countByExample(PinduoduoPreOrdersExample example);

    int deleteByExample(PinduoduoPreOrdersExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(PinduoduoPreOrders record);

    int insertSelective(PinduoduoPreOrders record);

    List<PinduoduoPreOrders> selectByExample(PinduoduoPreOrdersExample example);

    PinduoduoPreOrders selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") PinduoduoPreOrders record, @Param("example") PinduoduoPreOrdersExample example);

    int updateByExample(@Param("record") PinduoduoPreOrders record, @Param("example") PinduoduoPreOrdersExample example);

    int updateByPrimaryKeySelective(PinduoduoPreOrders record);

    int updateByPrimaryKey(PinduoduoPreOrders record);
}