package org.xxpay.core.service;

import org.xxpay.core.entity.MchInfo;
import org.xxpay.core.entity.PayDataStatistics;

import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/9/8
 * @description:
 */
public interface IMchInfoService {

    int add(MchInfo mchInfo);

    int register(MchInfo mchInfo);

    int update(MchInfo mchInfo);

    MchInfo find(MchInfo mchInfo);

    MchInfo findByMchId(Long mchId);

    MchInfo findByLoginName(String loginName);

    MchInfo findByMobile(Long mobile);

    MchInfo findByEmail(String email);

    MchInfo findByTag(String tag);

    MchInfo findByUserName(String userName);

    int auditOk(Long mchInd);

    int auditNot(Long mchInd);

    List<MchInfo> select(int offset, int limit, MchInfo mchInfo);

    List<MchInfo> select4Audit(int offset, int limit, MchInfo mchInfo);

    List<MchInfo> select4Normal(int offset, int limit, MchInfo mchInfo);

    Integer count(MchInfo mchInfo);

    Integer countsDataStatisies(PayDataStatistics payDataStatistics);

    Integer merchantTopup(PayDataStatistics payDataStatistics);

    Integer count4Audit(MchInfo mchInfo);

    Integer count4Normal(MchInfo mchInfo);

    Map count4Mch();

    /**
     * 重新设置商户信息
     * @param info
     */
    MchInfo reBuildMchInfoSettConfig(MchInfo info);

}
