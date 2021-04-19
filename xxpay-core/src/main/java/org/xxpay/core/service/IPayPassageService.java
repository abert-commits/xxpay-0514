package org.xxpay.core.service;

import org.xxpay.core.entity.MchInfo;
import org.xxpay.core.entity.PayPassage;
import org.xxpay.core.entity.PayPassageStatistics;

import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 18/5/3
 * @description: 支付通道
 */
public interface IPayPassageService {

    int add(PayPassage payPassage);

    int update(PayPassage payPassage);

    int updatePassage(PayPassage payPassage);

    int updateRate(PayPassage payPassage);

    PayPassage findById(Integer id);

    PayPassage findByPayInterfaceId(Integer payInterfaceId );

    List<PayPassage> select(int offset, int limit, PayPassage payPassage);

    Integer count(PayPassage payPassage);

    List<PayPassage> selectAll(PayPassage payPassage);

    List<MchInfo> selectAll(MchInfo mchInfo);

    /**
     * 根据支付类型查询所有支付通道列表
     * @param payType
     * @return
     */
    List<PayPassage> selectAllByPayType(String payType);

    /**
     * 支付通道统计
     * @param payPassageStatistics
     * @return
     */
    List<PayPassageStatistics> selectStatistics(int offset, int limit, PayPassageStatistics payPassageStatistics);


    Map<String, String> channelSum(PayPassageStatistics payPassageStatistics);
}
