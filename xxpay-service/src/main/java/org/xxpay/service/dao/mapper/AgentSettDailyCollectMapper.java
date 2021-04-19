package org.xxpay.service.dao.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.AgentSettDailyCollect;
import org.xxpay.core.entity.AgentSettDailyCollectExample;

public interface AgentSettDailyCollectMapper {
    int countByExample(AgentSettDailyCollectExample example);

    int deleteByExample(AgentSettDailyCollectExample example);

    int deleteByPrimaryKey(Long id);

    int insert(AgentSettDailyCollect record);

    int insertSelective(AgentSettDailyCollect record);

    List<AgentSettDailyCollect> selectByExample(AgentSettDailyCollectExample example);

    AgentSettDailyCollect selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") AgentSettDailyCollect record, @Param("example") AgentSettDailyCollectExample example);

    int updateByExample(@Param("record") AgentSettDailyCollect record, @Param("example") AgentSettDailyCollectExample example);

    int updateByPrimaryKeySelective(AgentSettDailyCollect record);

    int updateByPrimaryKey(AgentSettDailyCollect record);
}