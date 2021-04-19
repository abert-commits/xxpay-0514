package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.MchBankAccount;
import org.xxpay.core.entity.MchBankAccountExample;

import java.util.List;

public interface MchBankAccountMapper {
    int countByExample(MchBankAccountExample example);

    int deleteByExample(MchBankAccountExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MchBankAccount record);

    int insertSelective(MchBankAccount record);

    List<MchBankAccount> selectByExample(MchBankAccountExample example);

    MchBankAccount selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MchBankAccount record, @Param("example") MchBankAccountExample example);

    int updateByExample(@Param("record") MchBankAccount record, @Param("example") MchBankAccountExample example);

    int updateByPrimaryKeySelective(MchBankAccount record);

    int updateByPrimaryKey(MchBankAccount record);
}