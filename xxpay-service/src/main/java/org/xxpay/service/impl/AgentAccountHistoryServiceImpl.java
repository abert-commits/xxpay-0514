package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.entity.AgentAccountHistory;
import org.xxpay.core.entity.AgentAccountHistoryExample;
import org.xxpay.core.service.IAgentAccountHistoryService;
import org.xxpay.service.dao.mapper.AgentAccountHistoryMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 18/05/08
 * @description:
 */
@Service(version = "1.0.0")
public class AgentAccountHistoryServiceImpl implements IAgentAccountHistoryService {

    @Autowired
    private AgentAccountHistoryMapper agentAccountHistoryMapper;

    @Override
    public List<AgentAccountHistory> select(int offset, int limit, AgentAccountHistory agentAccountHistory) {
        AgentAccountHistoryExample example = new AgentAccountHistoryExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        AgentAccountHistoryExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentAccountHistory);
        return agentAccountHistoryMapper.selectByExample(example);
    }

    @Override
    public int count(AgentAccountHistory agentAccountHistory) {
        AgentAccountHistoryExample example = new AgentAccountHistoryExample();
        AgentAccountHistoryExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentAccountHistory);
        return agentAccountHistoryMapper.countByExample(example);
    }

    @Override
    public AgentAccountHistory findById(Long id) {
        return agentAccountHistoryMapper.selectByPrimaryKey(id);
    }

    @Override
    public AgentAccountHistory findByAgentIdAndId(Long agentId, Long id) {
        AgentAccountHistoryExample example = new AgentAccountHistoryExample();
        AgentAccountHistoryExample.Criteria criteria = example.createCriteria();
        criteria.andAgentIdEqualTo(agentId);
        criteria.andIdEqualTo(id);
        List<AgentAccountHistory> agentAccountHistoryList = agentAccountHistoryMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(agentAccountHistoryList)) return null;
        return agentAccountHistoryList.get(0);
    }

    @Override
    public List<Map> count4AgentProfit(Long agentId) {
        Map param = new HashMap<>();
        if(agentId != null) param.put("agentId", agentId);
        return agentAccountHistoryMapper.count4AgentProfit(param);
    }

    void setCriteria(AgentAccountHistoryExample.Criteria criteria, AgentAccountHistory agentAccountHistory) {
        if(agentAccountHistory != null) {
            if(agentAccountHistory.getId() != null) criteria.andIdEqualTo(agentAccountHistory.getId());
            if(agentAccountHistory.getAgentId() != null) criteria.andAgentIdEqualTo(agentAccountHistory.getAgentId());
            if(agentAccountHistory.getFundDirection() != null && !"-99".equals(agentAccountHistory.getFundDirection())) criteria.andFundDirectionEqualTo(agentAccountHistory.getFundDirection());
        }
    }

}
