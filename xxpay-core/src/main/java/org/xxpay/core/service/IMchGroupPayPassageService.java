package org.xxpay.core.service;

import org.xxpay.core.entity.MchGroupPayPassage;

import java.util.List;

public interface IMchGroupPayPassageService {
    int add(MchGroupPayPassage mchPayPassage);

    int update(MchGroupPayPassage mchPayPassage);

    int update(MchGroupPayPassage updateMchPayPassage, MchGroupPayPassage queryMchPayPassage);

    MchGroupPayPassage findById(Integer id);

    MchGroupPayPassage findByMchGroupIdAndProductId(Long groupId, Integer productId);

    List<MchGroupPayPassage> select(int offset, int limit, MchGroupPayPassage mchPayPassage);

    Integer count(MchGroupPayPassage mchPayPassage);

    List<MchGroupPayPassage> selectAll(MchGroupPayPassage mchPayPassage);

    /**
     * 根据商户组ID查询所有商户支付通道列表
     * @param mchGroupId
     * @return
     */
    List<MchGroupPayPassage> selectAllByMchGroupId(Long mchGroupId);

}
