package org.xxpay.service.dao.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.BankCardBin;
import org.xxpay.core.entity.BankCardBinExample;

import java.util.List;
import java.util.Map;

public interface BankCardBinMapper {
    int countByExample(BankCardBinExample example);

    int deleteByExample(BankCardBinExample example);

    int deleteByPrimaryKey(Long id);

    int insert(BankCardBin record);

    int insertSelective(BankCardBin record);

    List<BankCardBin> selectByExample(BankCardBinExample example);

    BankCardBin selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") BankCardBin record, @Param("example") BankCardBinExample example);

    int updateByExample(@Param("record") BankCardBin record, @Param("example") BankCardBinExample example);

    int updateByPrimaryKeySelective(BankCardBin record);

    int updateByPrimaryKey(BankCardBin record);

    BankCardBin selectByCardNo(String cardNo);

    int insertBatch(List<BankCardBin> bankCardBinList);

    BankCardBin findByCardNoAndIfTypeCode(Map param);
}