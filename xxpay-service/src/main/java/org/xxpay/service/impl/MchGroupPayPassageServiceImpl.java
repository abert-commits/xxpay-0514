package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.MchGroupPayPassage;
import org.xxpay.core.entity.MchGroupPayPassageExample;
import org.xxpay.core.service.IMchGroupPayPassageService;
import org.xxpay.service.dao.mapper.MchGroupPayPassageMapper;

import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 2018/5/3
 * @description: 商户支付通道
 */
@Service(interfaceName = "org.xxpay.core.service.IMchGroupPayPassageService", version = "1.0.0", retries = -1)
public class MchGroupPayPassageServiceImpl implements IMchGroupPayPassageService {

    @Autowired
    private MchGroupPayPassageMapper mchPayPassageMapper;

    @Override
    public int add(MchGroupPayPassage mchPayPassage) {
        return mchPayPassageMapper.insertSelective(mchPayPassage);
    }

    @Override
    public int update(MchGroupPayPassage mchPayPassage) {
        return mchPayPassageMapper.updateByPrimaryKeySelective(mchPayPassage);
    }

    @Override
    public int update(MchGroupPayPassage updateMchPayPassage, MchGroupPayPassage queryMchPayPassage) {
        MchGroupPayPassageExample example = new MchGroupPayPassageExample();
        MchGroupPayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, queryMchPayPassage);
        return mchPayPassageMapper.updateByExampleSelective(updateMchPayPassage, example);
    }


    @Override
    public MchGroupPayPassage findById(Integer id) {
        return mchPayPassageMapper.selectByPrimaryKey(id);
    }

    @Override
    public MchGroupPayPassage findByMchGroupIdAndProductId(Long groupId, Integer productId) {
        MchGroupPayPassage mchPayPassage = new MchGroupPayPassage();
        mchPayPassage.setMchGroupId(groupId);
        mchPayPassage.setProductId(productId);
        List<MchGroupPayPassage> mchPayPassageList = selectAll(mchPayPassage);
        if(CollectionUtils.isEmpty(mchPayPassageList)) return null;
        return mchPayPassageList.get(0);

    }

    @Override
    public List<MchGroupPayPassage> select(int offset, int limit, MchGroupPayPassage mchPayPassage) {
        MchGroupPayPassageExample example = new MchGroupPayPassageExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        MchGroupPayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchPayPassage);
        return mchPayPassageMapper.selectByExample(example);
    }

    @Override
    public Integer count(MchGroupPayPassage mchPayPassage) {
        MchGroupPayPassageExample example = new MchGroupPayPassageExample();
        MchGroupPayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchPayPassage);
        return mchPayPassageMapper.countByExample(example);
    }

    @Override
    public List<MchGroupPayPassage> selectAll(MchGroupPayPassage mchPayPassage) {
        MchGroupPayPassageExample example = new MchGroupPayPassageExample();
        example.setOrderByClause("createTime DESC");
        MchGroupPayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchPayPassage);
        return mchPayPassageMapper.selectByExample(example);
    }

    @Override
    public List<MchGroupPayPassage> selectAllByMchGroupId(Long mchId) {
        MchGroupPayPassage mchPayPassage = new MchGroupPayPassage();
        mchPayPassage.setMchGroupId(mchId);
        return selectAll(mchPayPassage);
    }

    void setCriteria(MchGroupPayPassageExample.Criteria criteria, MchGroupPayPassage obj) {
        if(obj != null) {
            if(obj.getMchGroupId() != null) criteria.andMchGroupIdEqualTo(obj.getMchGroupId());
            if(obj.getProductId() != null) criteria.andProductIdEqualTo(obj.getProductId());
            if(obj.getStatus() != null && obj.getStatus().byteValue() != -99) criteria.andStatusEqualTo(obj.getStatus());
        }
    }
}
