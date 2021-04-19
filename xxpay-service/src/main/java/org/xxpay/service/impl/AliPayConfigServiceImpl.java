package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.AlipayConfig;
import org.xxpay.core.entity.AlipayConfigExample;
import org.xxpay.core.entity.MchInfo;
import org.xxpay.core.entity.MchInfoExample;
import org.xxpay.core.service.IAliPayConfigService;
import org.xxpay.service.dao.mapper.AlipayConfigMapper;

import java.security.PublicKey;
import java.util.Date;
import java.util.List;

@Service(interfaceName = "org.xxpay.core.service.IAliPayConfigService", version = "1.0.0", retries = -1)
public class AliPayConfigServiceImpl implements IAliPayConfigService {

    @Autowired
    private AlipayConfigMapper alipayConfigMapper;


    @Override
    public int insert(AlipayConfig record) {
        int count = this.alipayConfigMapper.insert(record);
        return count;
    }

    @Override
    public int insertSelective(AlipayConfig record) {
        int count = this.alipayConfigMapper.insertSelective(record);
        return count;
    }

    @Override
    public AlipayConfig selectById(Integer id) {
        AlipayConfig model = this.alipayConfigMapper.selectByPrimaryKey(id);
        return model;
    }

    @Override
    public int updateByPrimaryKeySelective(AlipayConfig record) {
        int count = this.alipayConfigMapper.updateByPrimaryKeySelective(record);
        return count;
    }

    @Override
    public int updateByPrimaryKey(AlipayConfig record) {
        int count = this.alipayConfigMapper.updateByPrimaryKey(record);
        return count;
    }

    @Override
    public List<AlipayConfig> select(int offset, int limit, AlipayConfig record, Date createTimeStart, Date createTimeEnd) {
        AlipayConfigExample example = new AlipayConfigExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        AlipayConfigExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, record, createTimeStart, createTimeEnd);
        List<AlipayConfig> list = this.alipayConfigMapper.selectByExample(example);
        return list;
    }


    @Override
    public Integer count(AlipayConfig record, Date createTimeStart, Date createTimeEnd) {
        AlipayConfigExample example = new AlipayConfigExample();
        AlipayConfigExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, record, createTimeStart, createTimeEnd);
        return alipayConfigMapper.countByExample(example);
    }


    public AlipayConfig findAliPayConfig(AlipayConfig record) {
        AlipayConfigExample example = new AlipayConfigExample();
        AlipayConfigExample.Criteria criteria = example.createCriteria();
        setCriteriaEqualTo(criteria, record);
        List<AlipayConfig> list = this.alipayConfigMapper.selectByExample(example);
        return list != null && list.size() > 0 ? list.get(0) : null;
    }


    void setCriteria(AlipayConfigExample.Criteria criteria, AlipayConfig alipayConfig, Date createTimeStart, Date createTimeEnd) {
        if (alipayConfig != null) {
            if (alipayConfig.getAgentId() != null && alipayConfig.getAgentId() != null) {
                criteria.andAgentIdEqualTo(alipayConfig.getAgentId());
            }

            if (StringUtils.isNotBlank(alipayConfig.getCompanyName())) {
                criteria.andCompanyNameLike("%" + alipayConfig.getCompanyName() + "%");
            }

            if (alipayConfig.getStatus() != null && alipayConfig.getStatus() != -99) {
                criteria.andStatusEqualTo(alipayConfig.getStatus());
            }

            if (StringUtils.isNotBlank(alipayConfig.getParentAgentId())) {
                criteria.andParentAgentIdEqualTo(alipayConfig.getParentAgentId());
            }

            if (StringUtils.isNotBlank(alipayConfig.getEmail())) {
                criteria.andEmailLike("%" + alipayConfig.getEmail() + "%");
            }
            if (StringUtils.isNotBlank(alipayConfig.getAPPID())) {
                criteria.andAPPIDLike("%" + alipayConfig.getAPPID() + "%");
            }
            if (StringUtils.isNotBlank(alipayConfig.getPID())) {
                criteria.andPIDLike("%" + alipayConfig.getPID() + "%");
            }

            if (createTimeStart != null) {
                criteria.andCreateTimeGreaterThanOrEqualTo(createTimeStart);
            }
            if (createTimeEnd != null) {
                criteria.andCreateTimeLessThanOrEqualTo(createTimeEnd);
            }

        }
    }


    void setCriteriaEqualTo(AlipayConfigExample.Criteria criteria, AlipayConfig alipayConfig) {
        if (alipayConfig != null) {
            if (alipayConfig.getAgentId() != null && alipayConfig.getAgentId() != null) {
                criteria.andAgentIdEqualTo(alipayConfig.getAgentId());
            }

            if (StringUtils.isNotBlank(alipayConfig.getCompanyName())) {
                criteria.andCompanyNameEqualTo(alipayConfig.getCompanyName());
            }

            if (StringUtils.isNotBlank(alipayConfig.getEmail())) {
                criteria.andEmailEqualTo(alipayConfig.getEmail());
            }

            if (StringUtils.isNotBlank(alipayConfig.getAPPID())) {
                criteria.andAPPIDEqualTo(alipayConfig.getAPPID());
            }

            if (StringUtils.isNotBlank(alipayConfig.getPID())) {
                criteria.andPIDEqualTo(alipayConfig.getPID());
            }

            if (alipayConfig.getId() != null) {
                criteria.andIdEqualTo(alipayConfig.getId());
            }
        }
    }
}
