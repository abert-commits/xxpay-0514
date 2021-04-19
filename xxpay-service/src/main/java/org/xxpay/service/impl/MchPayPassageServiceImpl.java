package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.MchPayPassage;
import org.xxpay.core.entity.MchPayPassageExample;
import org.xxpay.core.service.IMchPayPassageService;
import org.xxpay.service.dao.mapper.MchPayPassageMapper;

import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 2018/5/3
 * @description: 商户支付通道
 */
@Service(interfaceName = "org.xxpay.core.service.IMchPayPassageService", version = "1.0.0", retries = -1)
public class MchPayPassageServiceImpl implements IMchPayPassageService {

    @Autowired
    private MchPayPassageMapper mchPayPassageMapper;

    @Override
    public int add(MchPayPassage mchPayPassage) {
        return mchPayPassageMapper.insertSelective(mchPayPassage);
    }

    @Override
    public int update(MchPayPassage mchPayPassage) {
        return mchPayPassageMapper.updateByPrimaryKeySelective(mchPayPassage);
    }

    @Override
    public int update(MchPayPassage updateMchPayPassage, MchPayPassage queryMchPayPassage) {
        MchPayPassageExample example = new MchPayPassageExample();
        MchPayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, queryMchPayPassage);
        return mchPayPassageMapper.updateByExampleSelective(updateMchPayPassage, example);
    }


    @Override
    public MchPayPassage findById(Integer id) {
        return mchPayPassageMapper.selectByPrimaryKey(id);
    }

    @Override
    public MchPayPassage findByMchIdAndProductId(Long mchId, Integer productId) {
        MchPayPassage mchPayPassage = new MchPayPassage();
        mchPayPassage.setMchId(mchId);
        mchPayPassage.setProductId(productId);
        List<MchPayPassage> mchPayPassageList = selectAll(mchPayPassage);
        if(CollectionUtils.isEmpty(mchPayPassageList)) return null;
        return mchPayPassageList.get(0);

    }

    @Override
    public List<MchPayPassage> select(int offset, int limit, MchPayPassage mchPayPassage) {
        MchPayPassageExample example = new MchPayPassageExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        MchPayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchPayPassage);
        return mchPayPassageMapper.selectByExample(example);
    }

    @Override
    public Integer count(MchPayPassage mchPayPassage) {
        MchPayPassageExample example = new MchPayPassageExample();
        MchPayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchPayPassage);
        return mchPayPassageMapper.countByExample(example);
    }

    @Override
    public List<MchPayPassage> selectAll(MchPayPassage mchPayPassage) {
        MchPayPassageExample example = new MchPayPassageExample();
        example.setOrderByClause("createTime DESC");
        MchPayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchPayPassage);
        return mchPayPassageMapper.selectByExample(example);
    }

    @Override
    public List<MchPayPassage> selectAllByMchId(Long mchId) {
        MchPayPassage mchPayPassage = new MchPayPassage();
        mchPayPassage.setMchId(mchId);
        return selectAll(mchPayPassage);
    }

    void setCriteria(MchPayPassageExample.Criteria criteria, MchPayPassage obj) {
        if(obj != null) {
            if(obj.getMchId() != null) criteria.andMchIdEqualTo(obj.getMchId());
            if(obj.getProductId() != null) criteria.andProductIdEqualTo(obj.getProductId());
            if(obj.getStatus() != null && obj.getStatus().byteValue() != -99) criteria.andStatusEqualTo(obj.getStatus());
        }
    }
}
