package org.xxpay.core.service;

import org.xxpay.core.entity.PayOrderCashCollRecord;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface IPayOrderCashCollRecordService {

    int add(PayOrderCashCollRecord record);

    List<PayOrderCashCollRecord> select(int offset, int limit, PayOrderCashCollRecord record, Date createTimeStart, Date createTimeEnd);

    Integer count(PayOrderCashCollRecord record, Date createTimeStart, Date createTimeEnd);

    Map transSuccess(Map<String, Object> map);


    int update(PayOrderCashCollRecord record);

    List<PayOrderCashCollRecord> selectByOrderId(String payOrderId);

    int delete(String payOrderId);


    PayOrderCashCollRecord findById(Integer id);

    int updateById(PayOrderCashCollRecord record);

}
