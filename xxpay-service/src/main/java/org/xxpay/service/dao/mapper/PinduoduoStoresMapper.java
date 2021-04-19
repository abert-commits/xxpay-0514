package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.PinduoduoStores;
import org.xxpay.core.entity.PinduoduoStoresExample;

import java.util.List;

public interface PinduoduoStoresMapper {
    int countByExample(PinduoduoStoresExample example);

    int deleteByExample(PinduoduoStoresExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(PinduoduoStores record);

    int insertSelective(PinduoduoStores record);

    List<PinduoduoStores> selectByExample(PinduoduoStoresExample example);

    PinduoduoStores selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") PinduoduoStores record, @Param("example") PinduoduoStoresExample example);

    int updateByExample(@Param("record") PinduoduoStores record, @Param("example") PinduoduoStoresExample example);

    int updateByPrimaryKeySelective(PinduoduoStores record);

    int updateByPrimaryKey(PinduoduoStores record);
}