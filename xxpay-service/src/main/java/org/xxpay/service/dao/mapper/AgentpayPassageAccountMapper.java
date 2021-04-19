package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.AgentpayPassageAccount;
import org.xxpay.core.entity.AgentpayPassageAccountExample;

import java.util.List;

public interface AgentpayPassageAccountMapper {
    int countByExample(AgentpayPassageAccountExample example);

    int deleteByExample(AgentpayPassageAccountExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AgentpayPassageAccount record);

    int insertSelective(AgentpayPassageAccount record);

    List<AgentpayPassageAccount> selectByExample(AgentpayPassageAccountExample example);

    AgentpayPassageAccount selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AgentpayPassageAccount record, @Param("example") AgentpayPassageAccountExample example);

    int updateByExample(@Param("record") AgentpayPassageAccount record, @Param("example") AgentpayPassageAccountExample example);

    int updateByPrimaryKeySelective(AgentpayPassageAccount record);

    int updateByPrimaryKey(AgentpayPassageAccount record);
}