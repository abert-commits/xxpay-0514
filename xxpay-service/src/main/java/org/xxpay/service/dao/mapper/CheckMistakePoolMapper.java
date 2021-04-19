package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.CheckMistakePool;
import org.xxpay.core.entity.CheckMistakePoolExample;

import java.util.List;

public interface CheckMistakePoolMapper {
    int countByExample(CheckMistakePoolExample example);

    int deleteByExample(CheckMistakePoolExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CheckMistakePool record);

    int insertSelective(CheckMistakePool record);

    List<CheckMistakePool> selectByExample(CheckMistakePoolExample example);

    CheckMistakePool selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CheckMistakePool record, @Param("example") CheckMistakePoolExample example);

    int updateByExample(@Param("record") CheckMistakePool record, @Param("example") CheckMistakePoolExample example);

    int updateByPrimaryKeySelective(CheckMistakePool record);

    int updateByPrimaryKey(CheckMistakePool record);
}