package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.entity.MchTradeOrder;
import org.xxpay.core.entity.MchTradeOrderExample;
import org.xxpay.core.service.IMchTradeOrderService;
import org.xxpay.service.dao.mapper.MchTradeOrderMapper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/12/21
 * @description:
 */
@Service(version = "1.0.0")
public class MchTradeOrderServiceImpl implements IMchTradeOrderService {

    @Autowired
    private MchTradeOrderMapper mchTradeOrderMapper;

    @Override
    public List<MchTradeOrder> select(int pageIndex, int pageSize, MchTradeOrder mchTradeOrder, Date createTimeStart, Date createTimeEnd) {
        MchTradeOrderExample example = new MchTradeOrderExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(pageIndex);
        example.setLimit(pageSize);
        MchTradeOrderExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchTradeOrder, createTimeStart, createTimeEnd);
        return mchTradeOrderMapper.selectByExample(example);
    }

    @Override
    public int count(MchTradeOrder mchTradeOrder, Date createTimeStart, Date createTimeEnd) {
        MchTradeOrderExample example = new MchTradeOrderExample();
        MchTradeOrderExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchTradeOrder, createTimeStart, createTimeEnd);
        return mchTradeOrderMapper.countByExample(example);
    }

    @Override
    public int add(MchTradeOrder mchTradeOrder) {
        return mchTradeOrderMapper.insertSelective(mchTradeOrder);
    }

    @Override
    public MchTradeOrder findByTradeOrderId(String tradeOrderId) {
        return mchTradeOrderMapper.selectByPrimaryKey(tradeOrderId);
    }

    @Override
    public MchTradeOrder findByMchIdAndTradeOrderId(Long mchId, String tradeOrderId) {
        MchTradeOrderExample example = new MchTradeOrderExample();
        MchTradeOrderExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andTradeOrderIdEqualTo(tradeOrderId);
        List<MchTradeOrder> mchTradeOrderList = mchTradeOrderMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(mchTradeOrderList)) return null;
        return mchTradeOrderList.get(0);
    }

    @Override
    public MchTradeOrder findByMchIdAndPayOrderId(Long mchId, String payOrderId) {
        MchTradeOrderExample example = new MchTradeOrderExample();
        MchTradeOrderExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andPayOrderIdEqualTo(payOrderId);
        List<MchTradeOrder> mchTradeOrderList = mchTradeOrderMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(mchTradeOrderList)) return null;
        return mchTradeOrderList.get(0);
    }

    @Override
    public int updateStatus4Ing(String tradeOrderId) {
        MchTradeOrder mchTradeOrder = new MchTradeOrder();
        mchTradeOrder.setStatus(MchConstant.TRADE_ORDER_STATUS_ING);
        MchTradeOrderExample example = new MchTradeOrderExample();
        MchTradeOrderExample.Criteria criteria = example.createCriteria();
        criteria.andTradeOrderIdEqualTo(tradeOrderId);
        criteria.andStatusEqualTo(MchConstant.TRADE_ORDER_STATUS_INIT);
        return mchTradeOrderMapper.updateByExampleSelective(mchTradeOrder, example);
    }

    @Override
    public int updateStatus4Success(String tradeOrderId, Long income) {
        return updateStatus4Success(tradeOrderId, null, income);
    }

    @Override
    public int updateStatus4Success(String tradeOrderId, String payOrderId, Long income) {
        MchTradeOrder mchTradeOrder = new MchTradeOrder();
        mchTradeOrder.setTradeSuccTime(new Date());
        if(StringUtils.isNotBlank(payOrderId)) mchTradeOrder.setPayOrderId(payOrderId);
        mchTradeOrder.setMchIncome(income);
        mchTradeOrder.setStatus(MchConstant.TRADE_ORDER_STATUS_SUCCESS);
        MchTradeOrderExample example = new MchTradeOrderExample();
        MchTradeOrderExample.Criteria criteria = example.createCriteria();
        criteria.andTradeOrderIdEqualTo(tradeOrderId);
        criteria.andStatusEqualTo(MchConstant.TRADE_ORDER_STATUS_ING);
        return mchTradeOrderMapper.updateByExampleSelective(mchTradeOrder, example);
    }

    @Override
    public int updateStatus4Fail(String tradeOrderId) {
        MchTradeOrder mchTradeOrder = new MchTradeOrder();
        mchTradeOrder.setStatus(MchConstant.TRADE_ORDER_STATUS_FAIL);
        MchTradeOrderExample example = new MchTradeOrderExample();
        MchTradeOrderExample.Criteria criteria = example.createCriteria();
        criteria.andTradeOrderIdEqualTo(tradeOrderId);
        criteria.andStatusEqualTo(MchConstant.TRADE_ORDER_STATUS_ING);
        return mchTradeOrderMapper.updateByExampleSelective(mchTradeOrder, example);
    }

    @Override
    public Map count4All(Long mchId, String tradeOrderId, String payOrderId, Byte tradeType, Byte status, String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if(mchId != null) param.put("mchId", mchId);
        if(StringUtils.isNotBlank(tradeOrderId)) param.put("tradeOrderId", tradeOrderId);
        if(StringUtils.isNotBlank(payOrderId)) param.put("payOrderId", payOrderId);
        if(tradeType != null && tradeType != -99) param.put("tradeType", tradeType);
        if(status != null && status != -99) param.put("status", status);
        if(StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if(StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return mchTradeOrderMapper.count4All(param);
    }

    @Override
    public int update(MchTradeOrder mchTradeOrder) {
        return mchTradeOrderMapper.updateByPrimaryKeySelective(mchTradeOrder);
    }

    void setCriteria(MchTradeOrderExample.Criteria criteria, MchTradeOrder mchTradeOrder) {
        setCriteria(criteria, mchTradeOrder, null, null);
    }

    void setCriteria(MchTradeOrderExample.Criteria criteria, MchTradeOrder mchTradeOrder, Date createTimeStart, Date createTimeEnd) {
        if(mchTradeOrder != null) {
            if(mchTradeOrder.getMchId() != null) criteria.andMchIdEqualTo(mchTradeOrder.getMchId());
            if(mchTradeOrder.getTradeType() != null && mchTradeOrder.getTradeType() != -99) criteria.andTradeTypeEqualTo(mchTradeOrder.getTradeType());
            if(StringUtils.isNotBlank(mchTradeOrder.getAppId())) criteria.andAppIdEqualTo(mchTradeOrder.getAppId());
            if(StringUtils.isNotBlank(mchTradeOrder.getTradeOrderId())) criteria.andTradeOrderIdEqualTo(mchTradeOrder.getTradeOrderId());
            if(mchTradeOrder.getStatus() != null && mchTradeOrder.getStatus() != -99) criteria.andStatusEqualTo(mchTradeOrder.getStatus());
            if(StringUtils.isNotBlank(mchTradeOrder.getPayOrderId())) criteria.andPayOrderIdEqualTo(mchTradeOrder.getPayOrderId());
        }
        if(createTimeStart != null) {
            criteria.andCreateTimeGreaterThanOrEqualTo(createTimeStart);
        }
        if(createTimeEnd != null) {
            criteria.andCreateTimeLessThanOrEqualTo(createTimeEnd);
        }
    }

}
