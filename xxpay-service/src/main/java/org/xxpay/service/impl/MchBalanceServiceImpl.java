package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.MchBalance;
import org.xxpay.core.entity.PayPassageStatistics;
import org.xxpay.core.service.IMchBalanceService;
import org.xxpay.service.dao.mapper.MchBalanceMapper;

import java.util.List;
import java.util.Map;

@Service(version = "1.0.0")
public class MchBalanceServiceImpl implements IMchBalanceService {

    @Autowired
    private MchBalanceMapper balanceMapper;

    @Override
    public MchBalance selectSynthesis(Map<String, Object> map) {
        return balanceMapper.selectSynthesis(map);
    }

    @Override
    public void insertSelective(MchBalance mchBalance) {
        balanceMapper.insertSelective(mchBalance);
    }

    /**
     * 获取总条数
     * @param mchBalance
     * @return
     */
    @Override
    public int count(MchBalance mchBalance) {
        return  balanceMapper.count(mchBalance);
    }

    @Override
    public List<MchBalance> select(int offset, int pageSize, MchBalance mchBalance) {
        return balanceMapper.select(offset,pageSize,mchBalance);
    }
}
