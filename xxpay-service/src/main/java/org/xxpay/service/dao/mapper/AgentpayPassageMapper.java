package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.AgentpayPassage;
import org.xxpay.core.entity.AgentpayPassageExample;

import java.util.List;

public interface AgentpayPassageMapper {
    int countByExample(AgentpayPassageExample example);

    int deleteByExample(AgentpayPassageExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AgentpayPassage record);

    int insertSelective(AgentpayPassage record);

    List<AgentpayPassage> selectByExample(AgentpayPassageExample example);

    AgentpayPassage selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AgentpayPassage record, @Param("example") AgentpayPassageExample example);

    int updateByExample(@Param("record") AgentpayPassage record, @Param("example") AgentpayPassageExample example);

    int updateByPrimaryKeySelective(AgentpayPassage record);

    int updateByPrimaryKey(AgentpayPassage record);
}