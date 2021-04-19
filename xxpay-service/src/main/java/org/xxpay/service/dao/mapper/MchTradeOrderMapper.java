package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.MchTradeOrder;
import org.xxpay.core.entity.MchTradeOrderExample;

import java.util.List;
import java.util.Map;

public interface MchTradeOrderMapper {
    int countByExample(MchTradeOrderExample example);

    int deleteByExample(MchTradeOrderExample example);

    int deleteByPrimaryKey(String tradeOrderId);

    int insert(MchTradeOrder record);

    int insertSelective(MchTradeOrder record);

    List<MchTradeOrder> selectByExample(MchTradeOrderExample example);

    MchTradeOrder selectByPrimaryKey(String tradeOrderId);

    int updateByExampleSelective(@Param("record") MchTradeOrder record, @Param("example") MchTradeOrderExample example);

    int updateByExample(@Param("record") MchTradeOrder record, @Param("example") MchTradeOrderExample example);

    int updateByPrimaryKeySelective(MchTradeOrder record);

    int updateByPrimaryKey(MchTradeOrder record);

    /**
     * 统计所有订单
     * @param param
     * @return
     */
    Map count4All(Map param);
}