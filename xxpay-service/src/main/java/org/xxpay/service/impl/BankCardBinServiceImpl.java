package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.BankCardBin;
import org.xxpay.core.entity.BankCardBinExample;
import org.xxpay.core.service.IBankCardBinService;
import org.xxpay.service.dao.mapper.BankCardBinMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 2018/7/22
 * @description:
 */
@Service(interfaceName = "org.xxpay.core.service.IBankCardBinService", version = "1.0.0", retries = -1)
public class BankCardBinServiceImpl implements IBankCardBinService {

    @Autowired
    private BankCardBinMapper bankCardBinMapper;

    @Override
    public int add(BankCardBin bankCardBin) {
        return bankCardBinMapper.insertSelective(bankCardBin);
    }

    @Override
    public int update(BankCardBin bankCardBin) {
        return bankCardBinMapper.updateByPrimaryKeySelective(bankCardBin);
    }

    @Override
    public BankCardBin findById(Long id) {
        return bankCardBinMapper.selectByPrimaryKey(id);
    }

    @Override
    public BankCardBin findByCardBin(String cardBin) {
        BankCardBinExample example = new BankCardBinExample();
        BankCardBinExample.Criteria criteria = example.createCriteria();
        criteria.andCardBinEqualTo(cardBin);
        List<BankCardBin> bankCardBinList = bankCardBinMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(bankCardBinList)) return null;
        return bankCardBinList.get(0);
    }

    @Override
    public BankCardBin findByCardNo(String cardNo) {
        return bankCardBinMapper.selectByCardNo(cardNo);
    }

    @Override
    public BankCardBin findByCardNoAndIfTypeCode(String cardNo, String ifTypeCode) {
        Map param = new HashMap<>();
        if(StringUtils.isNotBlank(cardNo)) param.put("cardNo", cardNo);
        if(StringUtils.isNotBlank(ifTypeCode)) param.put("ifTypeCode", ifTypeCode);
        return bankCardBinMapper.findByCardNoAndIfTypeCode(param);
    }

    @Override
    public List<BankCardBin> select(int offset, int limit, BankCardBin bankCardBin) {
        BankCardBinExample example = new BankCardBinExample();
        example.setOrderByClause("bankCode ASC");
        example.setOffset(offset);
        example.setLimit(limit);
        BankCardBinExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, bankCardBin);
        return bankCardBinMapper.selectByExample(example);
    }

    @Override
    public Integer count(BankCardBin bankCardBin) {
        BankCardBinExample example = new BankCardBinExample();
        BankCardBinExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, bankCardBin);
        return bankCardBinMapper.countByExample(example);
    }

    @Override
    public int delete(Long id) {
        return bankCardBinMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int delete(List<Long> ids) {
        BankCardBinExample example = new BankCardBinExample();
        BankCardBinExample.Criteria criteria = example.createCriteria();
        criteria.andIdIn(ids);
        return bankCardBinMapper.deleteByExample(example);
    }

    @Override
    public int insertBatch(List<BankCardBin> bankCardBinList) {
        return bankCardBinMapper.insertBatch(bankCardBinList);
    }

    void setCriteria(BankCardBinExample.Criteria criteria, BankCardBin obj) {
        if(obj != null) {
            if(StringUtils.isNotBlank(obj.getBankCode())) criteria.andBankCodeEqualTo(obj.getBankCode());
            if(StringUtils.isNotBlank(obj.getBankName())) criteria.andBankNameEqualTo(obj.getBankName());
            if(StringUtils.isNotBlank(obj.getCardBin())) criteria.andCardBinEqualTo(obj.getCardBin());
        }
    }

}
