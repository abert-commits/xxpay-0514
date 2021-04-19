package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.MchBankAccount;
import org.xxpay.core.entity.MchBankAccountExample;
import org.xxpay.core.service.IMchBankAccountService;
import org.xxpay.service.dao.mapper.MchBankAccountMapper;

import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 17/12/7
 * @description:
 */
@Service(version = "1.0.0")
public class MchBankAccountServiceImpl implements IMchBankAccountService {

    @Autowired
    private MchBankAccountMapper mchBankAccountMapper;

    @Override
    public List<MchBankAccount> select(int offset, int limit, MchBankAccount mchBankAccount) {
        MchBankAccountExample example = new MchBankAccountExample();
        example.setOffset(offset);
        example.setLimit(limit);
        MchBankAccountExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchBankAccount);
        return mchBankAccountMapper.selectByExample(example);
    }

    @Override
    public int count(MchBankAccount mchBankAccount) {
        MchBankAccountExample example = new MchBankAccountExample();
        MchBankAccountExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchBankAccount);
        return mchBankAccountMapper.countByExample(example);
    }

    @Override
    public MchBankAccount findById(Long id) {
        return mchBankAccountMapper.selectByPrimaryKey(id);
    }

    @Override
    public MchBankAccount findByAccountNo(String accountNo) {
        MchBankAccount mchBankAccount = new MchBankAccount();
        mchBankAccount.setAccountNo(accountNo);
        return find(mchBankAccount);
    }

    @Override
    public MchBankAccount find(MchBankAccount mchBankAccount) {
        MchBankAccountExample example = new MchBankAccountExample();
        MchBankAccountExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchBankAccount);
        List<MchBankAccount> mchBankAccountList = mchBankAccountMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(mchBankAccountList)) return null;
        return mchBankAccountList.get(0);
    }

    @Override
    public int add(MchBankAccount mchBankAccount) {
        return mchBankAccountMapper.insertSelective(mchBankAccount);
    }

    @Override
    public int update(MchBankAccount mchBankAccount) {
        return mchBankAccountMapper.updateByPrimaryKeySelective(mchBankAccount);
    }

    @Override
    public int updateByMchId(MchBankAccount updateMchBankAccount, Long mchId) {
        MchBankAccountExample example = new MchBankAccountExample();
        MchBankAccountExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        return mchBankAccountMapper.updateByExampleSelective(updateMchBankAccount, example);
    }

    @Override
    public int delete(Long id) {
        return mchBankAccountMapper.deleteByPrimaryKey(id);
    }

    void setCriteria(MchBankAccountExample.Criteria criteria, MchBankAccount mchBankAccount) {
        if(mchBankAccount != null) {
            if(mchBankAccount.getMchId() != null) criteria.andMchIdEqualTo(mchBankAccount.getMchId());
            if(mchBankAccount.getId() != null) criteria.andIdEqualTo(mchBankAccount.getId());
            if(StringUtils.isNotBlank(mchBankAccount.getAccountNo())) criteria.andAccountNoEqualTo(mchBankAccount.getAccountNo());
            if(mchBankAccount.getIsDefault() != null) criteria.andIsDefaultEqualTo(mchBankAccount.getIsDefault());
            if(mchBankAccount.getAccountType() != null && mchBankAccount.getAccountType() != -99) criteria.andAccountTypeEqualTo(mchBankAccount.getAccountType());
        }
    }

}
