package org.xxpay.service.dao.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.AgentAccount;
import org.xxpay.core.entity.AgentAccountExample;
import org.xxpay.core.entity.AgentInfo;

public interface AgentAccountMapper {
    int countByExample(AgentAccountExample example);

    int deleteByExample(AgentAccountExample example);

    int deleteByPrimaryKey(Long agentId);

    int insert(AgentAccount record);

    int insertSelective(AgentAccount record);

    List<AgentAccount> selectByExample(AgentAccountExample example);

    AgentAccount selectByPrimaryKey(Long agentId);

    int updateByExampleSelective(@Param("record") AgentAccount record, @Param("example") AgentAccountExample example);

    int updateByExample(@Param("record") AgentAccount record, @Param("example") AgentAccountExample example);

    int updateByPrimaryKeySelective(AgentAccount record);

    int updateByPrimaryKey(AgentAccount record);

    /**
     * 更新代理商账户结算金额
     * @param map
     * @return
     */
    int updateSettAmount(Map map);

    BigDecimal sumAgentBalance(AgentAccountExample exa);

    /**
     * 查询总代理下的二级代理余额总和
     */
    BigDecimal sumBalanceByParentAgentId(AgentInfo agentInfo);

    List<AgentAccount> findAllList ();
}