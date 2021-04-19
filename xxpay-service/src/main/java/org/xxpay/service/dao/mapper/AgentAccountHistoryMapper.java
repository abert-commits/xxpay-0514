package org.xxpay.service.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.AgentAccountHistory;
import org.xxpay.core.entity.AgentAccountHistoryExample;

public interface AgentAccountHistoryMapper {
    int countByExample(AgentAccountHistoryExample example);

    int deleteByExample(AgentAccountHistoryExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AgentAccountHistory record);

    int insertSelective(AgentAccountHistory record);

    List<AgentAccountHistory> selectByExample(AgentAccountHistoryExample example);

    AgentAccountHistory selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AgentAccountHistory record, @Param("example") AgentAccountHistoryExample example);

    int updateByExample(@Param("record") AgentAccountHistory record, @Param("example") AgentAccountHistoryExample example);

    int updateByPrimaryKeySelective(AgentAccountHistory record);

    int updateByPrimaryKey(AgentAccountHistory record);

    /**
     * 统计代理商分润数据
     * @param param
     * @return
     */
    List<Map> count4AgentProfit(Map param);
}