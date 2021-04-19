package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.AgentPassage;
import org.xxpay.core.entity.AgentPassageExample;

import java.util.List;

public interface AgentPassageMapper {
    int countByExample(AgentPassageExample example);

    int deleteByExample(AgentPassageExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AgentPassage record);

    int insertSelective(AgentPassage record);

    List<AgentPassage> selectByExample(AgentPassageExample example);

    AgentPassage selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AgentPassage record, @Param("example") AgentPassageExample example);

    int updateByExample(@Param("record") AgentPassage record, @Param("example") AgentPassageExample example);

    int updateByPrimaryKeySelective(AgentPassage record);

    int updateByPrimaryKey(AgentPassage record);
}