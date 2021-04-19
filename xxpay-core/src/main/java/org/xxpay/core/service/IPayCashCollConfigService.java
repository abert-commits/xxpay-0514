package org.xxpay.core.service;

import org.xxpay.core.entity.PayCashCollConfig;

import java.util.List;
import java.util.Map;

public interface IPayCashCollConfigService {


    List<PayCashCollConfig> select(int offset, int limit, PayCashCollConfig config) ;

    List<PayCashCollConfig> selectAll(PayCashCollConfig config);

    Integer count(PayCashCollConfig config) ;

    PayCashCollConfig findById(Integer id);

    int add(PayCashCollConfig config);

    int update(PayCashCollConfig config);

    List<PayCashCollConfig> select(int offset, int limit, Map<String,Object> map);

}
