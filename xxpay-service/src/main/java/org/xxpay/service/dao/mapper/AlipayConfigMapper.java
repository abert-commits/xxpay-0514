package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.AlipayConfig;
import org.xxpay.core.entity.AlipayConfigExample;

import java.util.List;

public interface AlipayConfigMapper {
    int countByExample(AlipayConfigExample example);

    int deleteByExample(AlipayConfigExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(AlipayConfig record);

    int insertSelective(AlipayConfig record);

    List<AlipayConfig> selectByExampleWithBLOBs(AlipayConfigExample example);

    List<AlipayConfig> selectByExample(AlipayConfigExample example);

    AlipayConfig selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") AlipayConfig record, @Param("example") AlipayConfigExample example);

    int updateByExampleWithBLOBs(@Param("record") AlipayConfig record, @Param("example") AlipayConfigExample example);

    int updateByExample(@Param("record") AlipayConfig record, @Param("example") AlipayConfigExample example);

    int updateByPrimaryKeySelective(AlipayConfig record);

    int updateByPrimaryKeyWithBLOBs(AlipayConfig record);

    int updateByPrimaryKey(AlipayConfig record);
}