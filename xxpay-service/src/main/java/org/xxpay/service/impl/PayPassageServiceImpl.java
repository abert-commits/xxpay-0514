package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xxpay.core.entity.*;
import org.xxpay.core.service.IPayPassageService;
import org.xxpay.service.dao.mapper.MchInfoMapper;
import org.xxpay.service.dao.mapper.PayPassageAccountMapper;
import org.xxpay.service.dao.mapper.PayPassageMapper;

import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 2018/5/3
 * @description: 支付通道
 */
@Service(interfaceName = "org.xxpay.core.service.IPayPassageService", version = "1.0.0", retries = -1)
public class PayPassageServiceImpl implements IPayPassageService {

    @Autowired
    private PayPassageMapper payPassageMapper;

    @Autowired
    private MchInfoMapper mchInfoMapper;

    @Autowired
    private PayPassageAccountMapper payPassageAccountMapper;

    @Override
    public int add(PayPassage payPassage) {
        return payPassageMapper.insertSelective(payPassage);
    }

    @Override
    public int update(PayPassage payPassage) {
        return payPassageMapper.updateByPrimaryKeySelective(payPassage);
    }

    @Override
    public int updatePassage(PayPassage payPassage) {
        return payPassageMapper.updatePassage(payPassage);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int updateRate(PayPassage payPassage) {
        // 修改通道费率
        int count = payPassageMapper.updateByPrimaryKeySelective(payPassage);
        // 修改该通道下所有子账户费率
        if(count == 1) {
            PayPassageAccount payPassageAccount = new PayPassageAccount();
            payPassageAccount.setPassageRate(payPassage.getPassageRate());
            PayPassageAccountExample example = new PayPassageAccountExample();
            PayPassageAccountExample.Criteria criteria = example.createCriteria();
            criteria.andPayPassageIdEqualTo(payPassage.getId());
            return payPassageAccountMapper.updateByExampleSelective(payPassageAccount, example);
        }
        return count;
    }

    @Override
    public PayPassage findById(Integer id) {
        return payPassageMapper.selectByPrimaryKey(id);
    }

    @Override
    public PayPassage findByPayInterfaceId(Integer payInterfaceId) {
        return payPassageMapper.findByPayInterfaceId(payInterfaceId);
    }

    @Override
    public List<PayPassage> select(int offset, int limit, PayPassage payPassage) {
        PayPassageExample example = new PayPassageExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        PayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payPassage);
        return payPassageMapper.selectByExample(example);
    }

    @Override
    public List<PayPassageStatistics> selectStatistics(int offset, int limit, PayPassageStatistics payPassageStatistics) {
        payPassageStatistics.setOffset(offset);
        payPassageStatistics.setLimit(limit);
        return payPassageMapper.selectStatistics(payPassageStatistics);
    }

    @Override
    public Map<String, String> channelSum(PayPassageStatistics payPassageStatistics) {
        return payPassageMapper.channelSum(payPassageStatistics);
    }

    @Override
    public Integer count(PayPassage payPassage) {
        PayPassageExample example = new PayPassageExample();
        PayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payPassage);
        return payPassageMapper.countByExample(example);
    }

    @Override
    public List<PayPassage> selectAll(PayPassage payPassage) {
        PayPassageExample example = new PayPassageExample();
        example.setOrderByClause("createTime DESC");
        PayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payPassage);
        return payPassageMapper.selectByExample(example);
    }

    @Override
    public List<MchInfo> selectAll(MchInfo mchInfo) {
        MchInfoExample example = new MchInfoExample();
        example.setOrderByClause("createTime DESC");
        MchInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchInfo);
        return mchInfoMapper.selectByExample(example);
    }

    @Override
    public List<PayPassage> selectAllByPayType(String payType) {
        PayPassage payPassage = new PayPassage();
        payPassage.setPayType(payType);
        return selectAll(payPassage);
    }

    void setCriteria(PayPassageExample.Criteria criteria, PayPassage obj) {
        if(obj != null) {
            if(obj.getPayType() != null) criteria.andPayTypeEqualTo(obj.getPayType());
            if(obj.getRiskStatus() != null && obj.getRiskStatus().byteValue() != -99) criteria.andRiskStatusEqualTo(obj.getRiskStatus());
            if(obj.getStatus() != null && obj.getStatus().byteValue() != -99) criteria.andStatusEqualTo(obj.getStatus());
            if (obj.getId() != null && obj.getId() != -99) criteria.andIdEqualTo(obj.getId());
        }
    }

    void setCriteria(MchInfoExample.Criteria criteria, MchInfo obj) {
        if(obj != null) {
            if(obj.getMchId() != null) criteria.andMchIdEqualTo(obj.getMchId());
            if(obj.getStatus() != null && obj.getStatus().byteValue() != -99) criteria.andStatusEqualTo(obj.getStatus());
        }
    }
}
