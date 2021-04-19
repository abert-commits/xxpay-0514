package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xxpay.core.entity.*;
import org.xxpay.core.service.IAgentpayPassageService;
import org.xxpay.service.dao.mapper.AgentpayPassageAccountMapper;
import org.xxpay.service.dao.mapper.AgentpayPassageMapper;

import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 2018/5/3
 * @description: 代付通道
 */
@Service(interfaceName = "org.xxpay.core.service.IAgentpayPassageService", version = "1.0.0", retries = -1)
public class AgentpayPassageServiceImpl implements IAgentpayPassageService {

    @Autowired
    private AgentpayPassageMapper agentpayPassageMapper;

    @Autowired
    private AgentpayPassageAccountMapper agentpayPassageAccountMapper;

    @Override
    public int add(AgentpayPassage agentpayPassage) {
        return agentpayPassageMapper.insertSelective(agentpayPassage);
    }

    @Override
    public int update(AgentpayPassage agentpayPassage) {
        return agentpayPassageMapper.updateByPrimaryKeySelective(agentpayPassage);
    }

    @Override
    public AgentpayPassage findById(Integer id) {
        return agentpayPassageMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<AgentpayPassage> select(int offset, int limit, AgentpayPassage agentpayPassage) {
        AgentpayPassageExample example = new AgentpayPassageExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        AgentpayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentpayPassage);
        return agentpayPassageMapper.selectByExample(example);
    }

    @Override
    public Integer count(AgentpayPassage agentpayPassage) {
        AgentpayPassageExample example = new AgentpayPassageExample();
        AgentpayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentpayPassage);
        return agentpayPassageMapper.countByExample(example);
    }

    @Override
    public List<AgentpayPassage> selectAll(AgentpayPassage agentpayPassage) {
        AgentpayPassageExample example = new AgentpayPassageExample();
        example.setOrderByClause("createTime DESC");
        AgentpayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentpayPassage);
        return agentpayPassageMapper.selectByExample(example);
    }

    void setCriteria(AgentpayPassageExample.Criteria criteria, AgentpayPassage obj) {
        if(obj != null) {
            if(obj.getIsDefault() != null) criteria.andIsDefaultEqualTo(obj.getIsDefault());
            if(obj.getRiskStatus() != null && obj.getRiskStatus().byteValue() != -99) criteria.andRiskStatusEqualTo(obj.getRiskStatus());
            if(obj.getStatus() != null && obj.getStatus().byteValue() != -99) criteria.andStatusEqualTo(obj.getStatus());
        }
    }
}
