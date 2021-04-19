package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.CheckMistake;
import org.xxpay.core.entity.CheckMistakeExample;

import java.util.List;

public interface CheckMistakeMapper {
    int countByExample(CheckMistakeExample example);

    int deleteByExample(CheckMistakeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(CheckMistake record);

    int insertSelective(CheckMistake record);

    List<CheckMistake> selectByExample(CheckMistakeExample example);

    CheckMistake selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") CheckMistake record, @Param("example") CheckMistakeExample example);

    int updateByExample(@Param("record") CheckMistake record, @Param("example") CheckMistakeExample example);

    int updateByPrimaryKeySelective(CheckMistake record);

    int updateByPrimaryKey(CheckMistake record);
}