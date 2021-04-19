package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.AgentAgentpayPassage;
import org.xxpay.core.entity.AgentAgentpayPassageExample;
import org.xxpay.core.service.IAgentAgentpayPassageService;
import org.xxpay.service.dao.mapper.AgentAgentpayPassageMapper;

import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 2018/8/30
 * @description: 代理商代付通道
 */
@Service(interfaceName = "org.xxpay.core.service.IAgentAgentpayPassageService", version = "1.0.0", retries = -1)
public class AgentAgentpayPassageServiceImpl implements IAgentAgentpayPassageService {

    @Autowired
    private AgentAgentpayPassageMapper agentAgentpayPassageMapper;

    @Override
    public int add(AgentAgentpayPassage agentAgentpayPassage) {
        return agentAgentpayPassageMapper.insertSelective(agentAgentpayPassage);
    }

    @Override
    public int update(AgentAgentpayPassage agentAgentpayPassage) {
        return agentAgentpayPassageMapper.updateByPrimaryKeySelective(agentAgentpayPassage);
    }

    @Override
    public int update(AgentAgentpayPassage updateAgentAgentpayPassage, AgentAgentpayPassage queryAgentAgentpayPassage) {
        AgentAgentpayPassageExample example = new AgentAgentpayPassageExample();
        AgentAgentpayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, queryAgentAgentpayPassage);
        return agentAgentpayPassageMapper.updateByExampleSelective(updateAgentAgentpayPassage, example);
    }

    @Override
    public AgentAgentpayPassage findById(Integer id) {
        return agentAgentpayPassageMapper.selectByPrimaryKey(id);
    }

    @Override
    public AgentAgentpayPassage findByAgentIdAndPassageId(Long agentId, Integer passageId) {
        AgentAgentpayPassage agentAgentpayPassage = new AgentAgentpayPassage();
        agentAgentpayPassage.setAgentId(agentId);
        agentAgentpayPassage.setAgentpayPassageId(passageId);
        List<AgentAgentpayPassage> agentAgentpayPassageList = selectAll(agentAgentpayPassage);
        if(CollectionUtils.isEmpty(agentAgentpayPassageList)) return null;
        return agentAgentpayPassageList.get(0);
    }

    @Override
    public List<AgentAgentpayPassage> select(int offset, int limit, AgentAgentpayPassage agentAgentpayPassage) {
        AgentAgentpayPassageExample example = new AgentAgentpayPassageExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        AgentAgentpayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentAgentpayPassage);
        return agentAgentpayPassageMapper.selectByExample(example);
    }

    @Override
    public Integer count(AgentAgentpayPassage agentAgentpayPassage) {
        AgentAgentpayPassageExample example = new AgentAgentpayPassageExample();
        AgentAgentpayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentAgentpayPassage);
        return agentAgentpayPassageMapper.countByExample(example);
    }

    @Override
    public List<AgentAgentpayPassage> selectAll(AgentAgentpayPassage agentAgentpayPassage) {
        AgentAgentpayPassageExample example = new AgentAgentpayPassageExample();
        AgentAgentpayPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentAgentpayPassage);
        return agentAgentpayPassageMapper.selectByExample(example);
    }

    @Override
    public List<AgentAgentpayPassage> selectAllByAgentId(Long agentId) {
        AgentAgentpayPassage agentAgentpayPassage = new AgentAgentpayPassage();
        agentAgentpayPassage.setAgentId(agentId);
        return selectAll(agentAgentpayPassage);
    }

    void setCriteria(AgentAgentpayPassageExample.Criteria criteria, AgentAgentpayPassage obj) {
        if(obj != null) {
            if(obj.getAgentId() != null) criteria.andAgentIdEqualTo(obj.getAgentId());
            if(obj.getAgentpayPassageId() != null) criteria.andAgentpayPassageIdEqualTo(obj.getAgentpayPassageId());
            if(obj.getStatus() != null && obj.getStatus().byteValue() != -99) criteria.andStatusEqualTo(obj.getStatus());
        }
    }

}
