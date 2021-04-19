package org.xxpay.core.service;

import org.xxpay.core.entity.MchBalance;
import org.xxpay.core.entity.PayPassageStatistics;

import java.util.List;
import java.util.Map;

public interface IMchBalanceService {

    MchBalance selectSynthesis(Map<String,Object> map);

    void insertSelective(MchBalance mchBalance);

    int count(MchBalance mchBalance);

    List<MchBalance> select(int i, int pageSize, MchBalance mchBalance);
}
