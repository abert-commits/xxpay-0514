package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.MchQrCode;
import org.xxpay.core.entity.MchQrCodeExample;

import java.util.List;

public interface MchQrCodeMapper {
    int countByExample(MchQrCodeExample example);

    int deleteByExample(MchQrCodeExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MchQrCode record);

    int insertSelective(MchQrCode record);

    List<MchQrCode> selectByExample(MchQrCodeExample example);

    MchQrCode selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MchQrCode record, @Param("example") MchQrCodeExample example);

    int updateByExample(@Param("record") MchQrCode record, @Param("example") MchQrCodeExample example);

    int updateByPrimaryKeySelective(MchQrCode record);

    int updateByPrimaryKey(MchQrCode record);
}