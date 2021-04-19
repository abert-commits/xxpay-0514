package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.MchApp;
import org.xxpay.core.entity.MchAppExample;

import java.util.List;

public interface MchAppMapper {
    int countByExample(MchAppExample example);

    int deleteByExample(MchAppExample example);

    int deleteByPrimaryKey(String appId);

    int insert(MchApp record);

    int insertSelective(MchApp record);

    List<MchApp> selectByExample(MchAppExample example);

    MchApp selectByPrimaryKey(String appId);

    int updateByExampleSelective(@Param("record") MchApp record, @Param("example") MchAppExample example);

    int updateByExample(@Param("record") MchApp record, @Param("example") MchAppExample example);

    int updateByPrimaryKeySelective(MchApp record);

    int updateByPrimaryKey(MchApp record);
}