package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.PinduoduoOrders;
import org.xxpay.core.entity.PinduoduoOrdersExample;

import java.util.List;

public interface PinduoduoOrdersMapper {
    int countByExample(PinduoduoOrdersExample example);

    int deleteByExample(PinduoduoOrdersExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(PinduoduoOrders record);

    int insertSelective(PinduoduoOrders record);

    List<PinduoduoOrders> selectByExample(PinduoduoOrdersExample example);

    PinduoduoOrders selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") PinduoduoOrders record, @Param("example") PinduoduoOrdersExample example);

    int updateByExample(@Param("record") PinduoduoOrders record, @Param("example") PinduoduoOrdersExample example);

    int updateByPrimaryKeySelective(PinduoduoOrders record);

    int updateByPrimaryKey(PinduoduoOrders record);
}