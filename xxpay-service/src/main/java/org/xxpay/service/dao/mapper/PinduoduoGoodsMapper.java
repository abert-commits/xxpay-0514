package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.PinduoduoGoods;
import org.xxpay.core.entity.PinduoduoGoodsExample;

import java.util.List;

public interface PinduoduoGoodsMapper {
    int countByExample(PinduoduoGoodsExample example);

    int deleteByExample(PinduoduoGoodsExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(PinduoduoGoods record);

    int insertSelective(PinduoduoGoods record);

    List<PinduoduoGoods> selectByExample(PinduoduoGoodsExample example);

    PinduoduoGoods selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") PinduoduoGoods record, @Param("example") PinduoduoGoodsExample example);

    int updateByExample(@Param("record") PinduoduoGoods record, @Param("example") PinduoduoGoodsExample example);

    int updateByPrimaryKeySelective(PinduoduoGoods record);

    int updateByPrimaryKey(PinduoduoGoods record);
}