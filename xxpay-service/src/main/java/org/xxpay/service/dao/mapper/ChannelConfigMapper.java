package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.ChannelConfig;
import org.xxpay.core.entity.ChannelConfigExample;

import java.util.List;

public interface ChannelConfigMapper {
    int countByExample(ChannelConfigExample example);

    int deleteByExample(ChannelConfigExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(ChannelConfig record);

    int insertSelective(ChannelConfig record);

    List<ChannelConfig> selectByExample(ChannelConfigExample example);

    ChannelConfig selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") ChannelConfig record, @Param("example") ChannelConfigExample example);

    int updateByExample(@Param("record") ChannelConfig record, @Param("example") ChannelConfigExample example);

    int updateByPrimaryKeySelective(ChannelConfig record);

    int updateByPrimaryKey(ChannelConfig record);
}