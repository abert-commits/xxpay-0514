package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.*;
import org.xxpay.core.service.IAgentpayPassageAccountService;
import org.xxpay.service.dao.mapper.AgentpayPassageAccountMapper;

import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 2018/5/3
 * @description: 代付通道账户
 */
@Service(interfaceName = "org.xxpay.core.service.IAgentpayPassageAccountService", version = "1.0.0", retries = -1)
public class AgentpayPassageAccountServiceImpl implements IAgentpayPassageAccountService {

    @Autowired
    private AgentpayPassageAccountMapper agentpayPassageAccountMapper;

    @Override
    public int add(AgentpayPassageAccount agentpayPassageAccount) {
        return agentpayPassageAccountMapper.insertSelective(agentpayPassageAccount);
    }

    @Override
    public int update(AgentpayPassageAccount agentpayPassageAccount) {
        return agentpayPassageAccountMapper.updateByPrimaryKeySelective(agentpayPassageAccount);
    }

    @Override
    public AgentpayPassageAccount findById(Integer id) {
        return agentpayPassageAccountMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<AgentpayPassageAccount> select(int offset, int limit, AgentpayPassageAccount agentpayPassageAccount) {
        AgentpayPassageAccountExample example = new AgentpayPassageAccountExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        AgentpayPassageAccountExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentpayPassageAccount);
        return agentpayPassageAccountMapper.selectByExample(example);
    }

    @Override
    public Integer count(AgentpayPassageAccount agentpayPassageAccount) {
        AgentpayPassageAccountExample example = new AgentpayPassageAccountExample();
        AgentpayPassageAccountExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentpayPassageAccount);
        return agentpayPassageAccountMapper.countByExample(example);
    }

    @Override
    public List<AgentpayPassageAccount> selectAll(AgentpayPassageAccount agentpayPassageAccount) {
        AgentpayPassageAccountExample example = new AgentpayPassageAccountExample();
        example.setOrderByClause("createTime DESC");
        AgentpayPassageAccountExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentpayPassageAccount);
        return agentpayPassageAccountMapper.selectByExample(example);
    }

    @Override
    public List<AgentpayPassageAccount> selectAllByPassageId(Integer agentpayPassageId) {
        AgentpayPassageAccount agentpayPassageAccount = new AgentpayPassageAccount();
        agentpayPassageAccount.setAgentpayPassageId(agentpayPassageId);
        return selectAll(agentpayPassageAccount);
    }

    void setCriteria(AgentpayPassageAccountExample.Criteria criteria, AgentpayPassageAccount obj) {
        if(obj != null) {
            if(obj.getAgentpayPassageId() != null) criteria.andAgentpayPassageIdEqualTo(obj.getAgentpayPassageId());
            if(obj.getRiskStatus() != null && obj.getRiskStatus().byteValue() != -99) criteria.andRiskStatusEqualTo(obj.getRiskStatus());
            if(obj.getStatus() != null && obj.getStatus().byteValue() != -99) criteria.andStatusEqualTo(obj.getStatus());
        }
    }
}
