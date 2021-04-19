package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.MchSettDailyCollect;
import org.xxpay.core.entity.MchSettDailyCollectExample;

import java.util.List;

public interface MchSettDailyCollectMapper {
    int countByExample(MchSettDailyCollectExample example);

    int deleteByExample(MchSettDailyCollectExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MchSettDailyCollect record);

    int insertSelective(MchSettDailyCollect record);

    List<MchSettDailyCollect> selectByExample(MchSettDailyCollectExample example);

    MchSettDailyCollect selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MchSettDailyCollect record, @Param("example") MchSettDailyCollectExample example);

    int updateByExample(@Param("record") MchSettDailyCollect record, @Param("example") MchSettDailyCollectExample example);

    int updateByPrimaryKeySelective(MchSettDailyCollect record);

    int updateByPrimaryKey(MchSettDailyCollect record);
}