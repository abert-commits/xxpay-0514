package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.MchBalance;
import org.xxpay.core.entity.MchBankAccount;
import org.xxpay.core.entity.MchBankAccountExample;
import org.xxpay.core.entity.PayPassageStatistics;

import java.util.List;
import java.util.Map;

public interface MchBalanceMapper {
    MchBalance selectSynthesis(Map<String,Object> map);

    void insertSelective(MchBalance mchBalance);

    int count(MchBalance mchBalance);

    List<MchBalance> select(@Param("offset") int offset,@Param("limit") int pageSize,@Param("mchBalance") MchBalance mchBalance);
}