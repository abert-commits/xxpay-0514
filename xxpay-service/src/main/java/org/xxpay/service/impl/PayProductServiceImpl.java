package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.entity.*;
import org.xxpay.core.service.IPayProductService;
import org.xxpay.service.dao.mapper.PayPassageMapper;
import org.xxpay.service.dao.mapper.PayProductMapper;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 2018/5/3
 * @description: 支付产品
 */
@Service(interfaceName = "org.xxpay.core.service.IPayProductService", version = "1.0.0", retries = -1)
public class PayProductServiceImpl implements IPayProductService {

    @Autowired
    private PayProductMapper payProductMapper;

    @Autowired
    private PayPassageMapper payPassageMapper;

    @Override
    public int add(PayProduct payProduct) {
        return payProductMapper.insertSelective(payProduct);
    }

    @Override
    public int update(PayProduct payProduct) {
        return payProductMapper.updateByPrimaryKeySelective(payProduct);
    }

    @Override
    public PayProduct findById(Integer id) {
        return payProductMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<PayProduct> select(int offset, int limit, PayProduct payProduct) {
        PayProductExample example = new PayProductExample();
        example.setOrderByClause("id ASC");
        example.setOffset(offset);
        example.setLimit(limit);
        PayProductExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payProduct);
        return payProductMapper.selectByExample(example);
    }

    @Override
    public Integer count(PayProduct payProduct) {
        PayProductExample example = new PayProductExample();
        PayProductExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payProduct);
        return payProductMapper.countByExample(example);
    }

    @Override
    public List<PayProduct> selectAll() {
        PayProductExample example = new PayProductExample();
        example.setOrderByClause("id ASC");
        return payProductMapper.selectByExample(example);
    }

    @Override
    public List<PayProduct> selectAll(PayProduct payProduct) {
        PayProductExample example = new PayProductExample();
        example.setOrderByClause("id ASC");
        PayProductExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payProduct);
        return payProductMapper.selectByExample(example);
    }

    @Override
    public List<PayPassage> selectAll(PayPassage payPassage) {
        PayPassageExample example = new PayPassageExample();
        example.setOrderByClause("id ASC");
        PayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payPassage);
        example.createCriteria();
        return payPassageMapper.selectByExample(example);
    }

    @Override
    public List<PayProduct> selectAll(List<Integer> ids) {
        PayProductExample example = new PayProductExample();
        example.setOrderByClause("id ASC");
        PayProductExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(ids);
        criteria.andStatusEqualTo(MchConstant.PUB_YES);
        return payProductMapper.selectByExample(example);
    }

    @Override
    public List<PayProduct> selectAll(String[] ids) {
        if(ids == null || ids.length == 0) return new LinkedList<>();
        List<Integer> idList = new LinkedList<>();
        for(int i = 0; i< ids.length; i++) {
            idList.add(Integer.parseInt(ids[i]));
        }
        return selectAll(idList);
    }

    @Override
    public List<PayProduct> selectAllByPayType(String payType) {
        PayProduct payProduct = new PayProduct();
        payProduct.setPayType(payType);
        return selectAll(payProduct);
    }

    void setCriteria(PayProductExample.Criteria criteria, PayProduct obj) {
        if(obj != null) {
            if(obj.getPayType() != null) criteria.andPayTypeEqualTo(obj.getPayType());
            if(obj.getProductType() != null && obj.getProductType() != -99) criteria.andProductTypeEqualTo(obj.getProductType());
            if(obj.getStatus() != null && obj.getStatus().byteValue() != -99) criteria.andStatusEqualTo(obj.getStatus());
            if (obj.getId() != null && obj.getId() != -99) criteria.andIdEqualTo(obj.getId());
        }
    }

    void setCriteria(PayPassageExample.Criteria criteria, PayPassage obj) {
        if(obj != null) {
            if(obj.getPayType() != null) criteria.andPayTypeEqualTo(obj.getPayType());
            if(obj.getStatus() != null && obj.getStatus().byteValue() != -99) criteria.andStatusEqualTo(obj.getStatus());
        }
    }
}
