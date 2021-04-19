package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.PinduoduoAddress;
import org.xxpay.core.entity.PinduoduoAddressExample;

import java.util.List;

public interface PinduoduoAddressMapper {
    int countByExample(PinduoduoAddressExample example);

    int deleteByExample(PinduoduoAddressExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(PinduoduoAddress record);

    int insertSelective(PinduoduoAddress record);

    List<PinduoduoAddress> selectByExample(PinduoduoAddressExample example);

    PinduoduoAddress selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") PinduoduoAddress record, @Param("example") PinduoduoAddressExample example);

    int updateByExample(@Param("record") PinduoduoAddress record, @Param("example") PinduoduoAddressExample example);

    int updateByPrimaryKeySelective(PinduoduoAddress record);

    int updateByPrimaryKey(PinduoduoAddress record);
}