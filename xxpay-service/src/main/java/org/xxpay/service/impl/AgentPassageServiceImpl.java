package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.AgentPassage;
import org.xxpay.core.entity.AgentPassageExample;
import org.xxpay.core.service.IAgentPassageService;
import org.xxpay.service.dao.mapper.AgentPassageMapper;

import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 2018/5/3
 * @description: 代理商通道
 */
@Service(interfaceName = "org.xxpay.core.service.IAgentPassageService", version = "1.0.0", retries = -1)
public class AgentPassageServiceImpl implements IAgentPassageService {

    @Autowired
    private AgentPassageMapper agentPassageMapper;

    @Override
    public int add(AgentPassage agentPassage) {
        return agentPassageMapper.insertSelective(agentPassage);
    }

    @Override
    public int update(AgentPassage agentPassage) {
        return agentPassageMapper.updateByPrimaryKeySelective(agentPassage);
    }

    @Override
    public int update(AgentPassage updateAgentPassage, AgentPassage queryAgentPassage) {
        AgentPassageExample example = new AgentPassageExample();
        AgentPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, queryAgentPassage);
        return agentPassageMapper.updateByExampleSelective(updateAgentPassage, example);
    }

    @Override
    public AgentPassage findById(Integer id) {
        return agentPassageMapper.selectByPrimaryKey(id);
    }

    @Override
    public AgentPassage findByAgentIdAndProductId(Long agentId, Integer productId) {
        AgentPassage agentPassage = new AgentPassage();
        agentPassage.setAgentId(agentId);
        agentPassage.setProductId(productId);
        List<AgentPassage> agentPassageList = selectAll(agentPassage);
        if(CollectionUtils.isEmpty(agentPassageList)) return null;
        return agentPassageList.get(0);
    }

    @Override
    public List<AgentPassage> select(int offset, int limit, AgentPassage agentPassage) {
        AgentPassageExample example = new AgentPassageExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        AgentPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentPassage);
        return agentPassageMapper.selectByExample(example);
    }

    @Override
    public Integer count(AgentPassage agentPassage) {
        AgentPassageExample example = new AgentPassageExample();
        AgentPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentPassage);
        return agentPassageMapper.countByExample(example);
    }

    @Override
    public List<AgentPassage> selectAll(AgentPassage agentPassage) {
        AgentPassageExample example = new AgentPassageExample();
        AgentPassageExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentPassage);
        return agentPassageMapper.selectByExample(example);
    }

    @Override
    public List<AgentPassage> selectAllByAgentId(Long agentId) {
        AgentPassage agentPassage = new AgentPassage();
        agentPassage.setAgentId(agentId);
        return selectAll(agentPassage);
    }

    void setCriteria(AgentPassageExample.Criteria criteria, AgentPassage obj) {
        if(obj != null) {
            if(obj.getAgentId() != null) criteria.andAgentIdEqualTo(obj.getAgentId());
            if(obj.getProductId() != null) criteria.andProductIdEqualTo(obj.getProductId());
            if(obj.getStatus() != null && obj.getStatus().byteValue() != -99) criteria.andStatusEqualTo(obj.getStatus());
        }
    }

}
