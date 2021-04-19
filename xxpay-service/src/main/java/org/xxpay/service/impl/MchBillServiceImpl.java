package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.entity.MchBill;
import org.xxpay.core.entity.MchBillExample;
import org.xxpay.core.service.IMchBillService;
import org.xxpay.service.dao.mapper.MchBillMapper;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 18/2/6
 * @description:
 */
@Service(version = "1.0.0")
public class MchBillServiceImpl implements IMchBillService {

    @Autowired
    private MchBillMapper mchBillMapper;

    @Override
    public MchBill findById(Long id) {
        return mchBillMapper.selectByPrimaryKey(id);
    }

    @Override
    public MchBill findByMchIdAndId(Long mchId, Long id) {
        MchBillExample example = new MchBillExample();
        MchBillExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andIdEqualTo(id);
        List<MchBill> mchBillList = mchBillMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(mchBillList)) return null;
        return mchBillList.get(0);
    }

    @Override
    public List<MchBill> select(int offset, int limit, MchBill mchBill) {
        MchBillExample example = new MchBillExample();
        example.setOrderByClause("billDate desc");
        example.setOffset(offset);
        example.setLimit(limit);
        MchBillExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchBill);
        return mchBillMapper.selectByExample(example);
    }

    @Override
    public Integer count(MchBill mchBill) {
        MchBillExample example = new MchBillExample();
        MchBillExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchBill);
        return mchBillMapper.countByExample(example);
    }

    @Override
    public MchBill findByMchIdAndBillDate(Long mchId, Date billDate) {
        MchBillExample example = new MchBillExample();
        MchBillExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andBillDateEqualTo(billDate);
        List<MchBill> mchBillList = mchBillMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(mchBillList)) return null;
        return mchBillList.get(0);
    }

    @Override
    public int add(MchBill mchBill) {
        return mchBillMapper.insertSelective(mchBill);
    }

    @Override
    public int updateComplete(Long mchId, Date billDate) {
        MchBillExample example = new MchBillExample();
        MchBillExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andBillDateEqualTo(billDate);
        MchBill mchBill = new MchBill();
        mchBill.setStatus(MchConstant.MCH_BILL_STATUS_COMPLETE);
        return mchBillMapper.updateByExampleSelective(mchBill, example);
    }

    void setCriteria(MchBillExample.Criteria criteria, MchBill mchBill) {
        if(mchBill != null) {
            if(mchBill.getMchId() != null) criteria.andMchIdEqualTo(mchBill.getMchId());
            if(mchBill.getBillDate() != null) criteria.andBillDateEqualTo(mchBill.getBillDate());
            if(mchBill.getStatus() != null && mchBill.getStatus() != -99) criteria.andStatusEqualTo(mchBill.getStatus());
        }
    }

}
