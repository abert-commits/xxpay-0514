package org.xxpay.service.dao.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.MchInfo;
import org.xxpay.core.entity.MchInfoExample;
import org.xxpay.core.entity.PayDataStatistics;

public interface MchInfoMapper {
    int countByExample(MchInfoExample example);

    int deleteByExample(MchInfoExample example);

    int deleteByPrimaryKey(Long mchId);

    int insert(MchInfo record);

    int countsDataStatisies(PayDataStatistics payDataStatistics);

    int merchantTopup(PayDataStatistics payDataStatistics);

    int insertSelective(MchInfo record);

    List<MchInfo> selectByExample(MchInfoExample example);

    MchInfo selectByPrimaryKey(Long mchId);

    int updateByExampleSelective(@Param("record") MchInfo record, @Param("example") MchInfoExample example);

    int updateByExample(@Param("record") MchInfo record, @Param("example") MchInfoExample example);

    int updateByPrimaryKeySelective(MchInfo record);

    int updateByPrimaryKey(MchInfo record);

    /**
     * 统计商户信息
     * @param param
     * @return
     */
    Map count4Mch(Map param);
}