package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.PayPassage;
import org.xxpay.core.entity.PayPassageExample;
import org.xxpay.core.entity.PayPassageStatistics;

import java.util.List;
import java.util.Map;

public interface PayPassageMapper {
    int countByExample(PayPassageExample example);

    int deleteByExample(PayPassageExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(PayPassage record);

    int insertSelective(PayPassage record);

    List<PayPassage> selectByExample(PayPassageExample example);

    PayPassage selectByPrimaryKey(Integer id);

    PayPassage findByPayInterfaceId(@Param("payInterfaceId") Integer payInterfaceId);

    int updateByExampleSelective(@Param("record") PayPassage record, @Param("example") PayPassageExample example);

    int updateByExample(@Param("record") PayPassage record, @Param("example") PayPassageExample example);

    int updateByPrimaryKeySelective(PayPassage record);

    int updatePassage(PayPassage record);

    int updateByPrimaryKey(PayPassage record);

    List<PayPassageStatistics> selectStatistics(PayPassageStatistics payPassageStatistics);

    Map<String, String> channelSum(PayPassageStatistics payPassageStatistics);
}