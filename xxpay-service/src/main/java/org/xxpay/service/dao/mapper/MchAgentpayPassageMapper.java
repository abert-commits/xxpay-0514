package org.xxpay.service.dao.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.MchAgentpayPassage;
import org.xxpay.core.entity.MchAgentpayPassageExample;

public interface MchAgentpayPassageMapper {
    int countByExample(MchAgentpayPassageExample example);

    int deleteByExample(MchAgentpayPassageExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(MchAgentpayPassage record);

    int insertSelective(MchAgentpayPassage record);

    List<MchAgentpayPassage> selectByExample(MchAgentpayPassageExample example);

    MchAgentpayPassage selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") MchAgentpayPassage record, @Param("example") MchAgentpayPassageExample example);

    int updateByExample(@Param("record") MchAgentpayPassage record, @Param("example") MchAgentpayPassageExample example);

    int updateByPrimaryKeySelective(MchAgentpayPassage record);

    int updateByPrimaryKey(MchAgentpayPassage record);
}