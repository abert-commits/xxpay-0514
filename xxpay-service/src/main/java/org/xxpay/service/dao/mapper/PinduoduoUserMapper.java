package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.PinduoduoUser;
import org.xxpay.core.entity.PinduoduoUserExample;

import java.util.List;

public interface PinduoduoUserMapper {
    int countByExample(PinduoduoUserExample example);

    int deleteByExample(PinduoduoUserExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(PinduoduoUser record);

    int insertSelective(PinduoduoUser record);

    List<PinduoduoUser> selectByExample(PinduoduoUserExample example);

    PinduoduoUser selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") PinduoduoUser record, @Param("example") PinduoduoUserExample example);

    int updateByExample(@Param("record") PinduoduoUser record, @Param("example") PinduoduoUserExample example);

    int updateByPrimaryKeySelective(PinduoduoUser record);

    int updateByPrimaryKey(PinduoduoUser record);
}