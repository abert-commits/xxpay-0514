package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.PayOrderCashCollRecord;
import org.xxpay.core.entity.PayOrderCashCollRecordExample;
import org.xxpay.core.service.IPayOrderCashCollRecordService;
import org.xxpay.service.dao.mapper.PayOrderCashCollRecordMapper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(interfaceName = "org.xxpay.core.service.IPayOrderCashCollRecordService", version = "1.0.0", retries = -1)
public class PayOrderCashCollRecordServiceImpl implements IPayOrderCashCollRecordService {

    @Autowired
    private PayOrderCashCollRecordMapper recordMapper;

    @Override
    public int add(PayOrderCashCollRecord record) {
        return recordMapper.insertSelective(record);
    }

    @Override
    public List<PayOrderCashCollRecord> select(int offset, int limit, PayOrderCashCollRecord record, Date createTimeStart, Date createTimeEnd) {
        PayOrderCashCollRecordExample example = new PayOrderCashCollRecordExample();
        example.setOrderByClause("CreateTime desc");
        example.setOffset(offset);
        example.setLimit(limit);
        PayOrderCashCollRecordExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, record, createTimeStart, createTimeEnd);
        return recordMapper.selectByExample(example);
    }

    @Override
    public Integer count(PayOrderCashCollRecord record, Date createTimeStart, Date createTimeEnd) {
        PayOrderCashCollRecordExample example = new PayOrderCashCollRecordExample();
        PayOrderCashCollRecordExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, record, createTimeStart, createTimeEnd);
        return recordMapper.countByExample(example);
    }


    @Override
    public int update(PayOrderCashCollRecord record) {
        PayOrderCashCollRecordExample example = new PayOrderCashCollRecordExample();
        PayOrderCashCollRecordExample.Criteria criteria = example.createCriteria();
        if (record.getPayOrderId() != null) criteria.andPayOrderIdEqualTo(record.getPayOrderId());
        int count = recordMapper.updateByExampleSelective(record, example);
        return count;
    }


    @Override
    public int updateById(PayOrderCashCollRecord record) {
        int count = recordMapper.updateByPrimaryKeySelective(record);
        return count;
    }


    @Override
    public Map transSuccess(Map<String, Object> map) {

        Map param = new HashMap();
        if (map.containsKey("payOrderId") && map.get("payOrderId") != null && StringUtils.isNotBlank(map.get("payOrderId").toString())) {
            param.put("payOrderId", map.get("payOrderId").toString());
        }

        if (map.containsKey("channelOrderNo") && map.get("channelOrderNo") != null && StringUtils.isNotBlank(map.get("channelOrderNo").toString())) {
            param.put("channelOrderNo", map.get("channelOrderNo").toString());
        }

        if (map.containsKey("transInUserId") && map.get("transInUserId") != null && StringUtils.isNotBlank(map.get("transInUserId").toString())) {
            param.put("transInUserId", map.get("transInUserId").toString());
        }

        if (map.containsKey("status") && StringUtils.isNotBlank(map.get("status").toString()) && !map.get("status").toString().equals("-99")) {
            param.put("status", map.get("status").toString());
        }

        if (map.containsKey("passageAccountId") && map.get("passageAccountId") != null && !map.get("passageAccountId").toString().equals("-99")) {
            param.put("passageAccountId", map.get("passageAccountId").toString());
        }

        if (map.containsKey("type") && map.get("type") != null && StringUtils.isNotBlank(map.get("type").toString())) {
            param.put("type", map.get("type").toString());
        }

        if (map.get("createTimeStart") != null) {
            param.put("createTimeStart", map.get("createTimeStart"));
        }

        if (map.get("createTimeEnd") != null) {
            param.put("createTimeEnd", map.get("createTimeEnd"));
        }

        return recordMapper.transSuccess(param);
    }


    /**
     * 根据订单号查询
     *
     * @param payOrderId
     * @return
     */
    @Override
    public List<PayOrderCashCollRecord> selectByOrderId(String payOrderId) {
        PayOrderCashCollRecordExample example = new PayOrderCashCollRecordExample();
        example.setOrderByClause("CreateTime desc");
        PayOrderCashCollRecordExample.Criteria criteria = example.createCriteria();
        criteria.andPayOrderIdEqualTo(payOrderId);
        return recordMapper.selectByExample(example);
    }


    @Override
    public int delete(String payOrderId) {
        PayOrderCashCollRecordExample example = new PayOrderCashCollRecordExample();
        PayOrderCashCollRecordExample.Criteria criteria = example.createCriteria();
        criteria.andPayOrderIdEqualTo(payOrderId);
        return recordMapper.deleteByExample(example);
    }

    @Override
    public PayOrderCashCollRecord findById(Integer id) {
        return recordMapper.selectByPrimaryKey(id);
    }

    void setCriteria(PayOrderCashCollRecordExample.Criteria criteria, PayOrderCashCollRecord record, Date createTimeStart, Date createTimeEnd) {
        if (record != null) {
            if (record.getStatus() != null) criteria.andStatusEqualTo(record.getStatus());
            if (StringUtils.isNotEmpty(record.getPayOrderId())) criteria.andPayOrderIdEqualTo(record.getPayOrderId());
            if (StringUtils.isNotEmpty(record.getChannelOrderNo()))
                criteria.andChannelOrderNoEqualTo(record.getChannelOrderNo());
            if (StringUtils.isNotEmpty(record.getTransInUserId()))
                criteria.andTransInUserIdEqualTo(record.getTransInUserId());

            if (record.getPassageAccountId() != null && !record.getPassageAccountId().equals("-99")) {
                criteria.andPassageAccountIdEqualTo(record.getPassageAccountId());
            }
        }
        if (createTimeStart != null) {
            criteria.andCreateTimeGreaterThanOrEqualTo(createTimeStart);
        }
        if (createTimeEnd != null) {
            criteria.andCreateTimeLessThanOrEqualTo(createTimeEnd);
        }

    }
}
