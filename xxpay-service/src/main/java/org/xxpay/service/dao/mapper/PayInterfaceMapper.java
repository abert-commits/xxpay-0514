package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.PayInterface;
import org.xxpay.core.entity.PayInterfaceExample;

import java.util.List;

public interface PayInterfaceMapper {
    int countByExample(PayInterfaceExample example);

    int deleteByExample(PayInterfaceExample example);

    int deleteByPrimaryKey(String ifCode);

    int insert(PayInterface record);

    int insertSelective(PayInterface record);

    List<PayInterface> selectByExample(PayInterfaceExample example);

    PayInterface selectByPrimaryKey(String ifCode);

    int updateByExampleSelective(@Param("record") PayInterface record, @Param("example") PayInterfaceExample example);

    int updateByExample(@Param("record") PayInterface record, @Param("example") PayInterfaceExample example);

    int updateByPrimaryKeySelective(PayInterface record);

    int updateByPrimaryKey(PayInterface record);
}