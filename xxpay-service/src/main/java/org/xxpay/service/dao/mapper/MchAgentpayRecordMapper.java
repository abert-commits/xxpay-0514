package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.MchAgentpayRecord;
import org.xxpay.core.entity.MchAgentpayRecordExample;

import java.util.List;
import java.util.Map;

public interface MchAgentpayRecordMapper {
    int countByExample(MchAgentpayRecordExample example);

    int deleteByExample(MchAgentpayRecordExample example);

    int deleteByPrimaryKey(String agentpayOrderId);

    int insert(MchAgentpayRecord record);

    int insertSelective(MchAgentpayRecord record);

    List<MchAgentpayRecord> selectByExample(MchAgentpayRecordExample example);

    MchAgentpayRecord selectByPrimaryKey(String agentpayOrderId);

    int updateByExampleSelective(@Param("record") MchAgentpayRecord record, @Param("example") MchAgentpayRecordExample example);

    int updateByExample(@Param("record") MchAgentpayRecord record, @Param("example") MchAgentpayRecordExample example);

    int updateByPrimaryKeySelective(MchAgentpayRecord record);

    int updateByPrimaryKey(MchAgentpayRecord record);

    /**
     * 统计所有订单
     * @param param
     * @return
     */
    Map count4All(Map param);
}