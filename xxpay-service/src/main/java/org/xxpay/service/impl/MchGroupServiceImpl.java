package org.xxpay.service.impl;


import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.MchGroup;
import org.xxpay.core.service.IMchGroupService;
import org.xxpay.service.dao.mapper.MchGroupMapper;
import org.xxpay.service.dao.mapper.MchInfoMapper;

import java.util.List;

@Service(interfaceName = "org.xxpay.core.service.IMchGroupService", version = "1.0.0", retries = -1)
public class MchGroupServiceImpl implements IMchGroupService {

    @Autowired
    private MchGroupMapper mchGroupMapper;

    @Override
    public int add(MchGroup mchGroup) {
        return mchGroupMapper.add(mchGroup);
    }

    @Override
    public int update(MchGroup mchGroup) {
        return mchGroupMapper.update(mchGroup);
    }

    @Override
    public MchGroup findByMchGroupId(Integer mchGroupId) {
        return mchGroupMapper.findByMchGroupId(mchGroupId);
    }

    @Override
    public MchGroup findByGroupName(String groupName) {
        return mchGroupMapper.findByGroupName(groupName);
    }

    @Override
    public List<MchGroup> selectAll() {
        return mchGroupMapper.selectAll();
    }
}
