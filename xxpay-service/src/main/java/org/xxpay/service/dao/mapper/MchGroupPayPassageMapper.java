package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.MchGroupPayPassage;
import org.xxpay.core.entity.MchGroupPayPassageExample;

import java.util.List;

public interface MchGroupPayPassageMapper {
    int countByExample(MchGroupPayPassageExample example);

    int deleteByExample(MchGroupPayPassageExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(MchGroupPayPassage record);

    int insertSelective(MchGroupPayPassage record);

    List<MchGroupPayPassage> selectByExample(MchGroupPayPassageExample example);

    MchGroupPayPassage selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") MchGroupPayPassage record, @Param("example") MchGroupPayPassageExample example);

    int updateByExample(@Param("record") MchGroupPayPassage record, @Param("example") MchGroupPayPassageExample example);

    int updateByPrimaryKeySelective(MchGroupPayPassage record);

    int updateByPrimaryKey(MchGroupPayPassage record);
}
