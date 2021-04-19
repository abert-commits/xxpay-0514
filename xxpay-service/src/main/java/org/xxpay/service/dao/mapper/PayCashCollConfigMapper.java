package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.PayCashCollConfig;
import org.xxpay.core.entity.PayCashCollConfigExample;

import java.math.BigDecimal;
import java.util.List;

public interface PayCashCollConfigMapper {
    int countByExample(PayCashCollConfigExample example);

    int deleteByExample(PayCashCollConfigExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(PayCashCollConfig record);

    int insertSelective(PayCashCollConfig record);

    List<PayCashCollConfig> selectByExample(PayCashCollConfigExample example);

    PayCashCollConfig selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") PayCashCollConfig record, @Param("example") PayCashCollConfigExample example);

    int updateByExample(@Param("record") PayCashCollConfig record, @Param("example") PayCashCollConfigExample example);

    int updateByPrimaryKeySelective(PayCashCollConfig record);

    int updateByPrimaryKey(PayCashCollConfig record);

    BigDecimal sumPercentageByExample(PayCashCollConfigExample example);

}