package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.IapReceipt;
import org.xxpay.core.entity.IapReceiptExample;

import java.util.List;

public interface IapReceiptMapper {
    int countByExample(IapReceiptExample example);

    int deleteByExample(IapReceiptExample example);

    int deleteByPrimaryKey(String payOrderId);

    int insert(IapReceipt record);

    int insertSelective(IapReceipt record);

    List<IapReceipt> selectByExampleWithBLOBs(IapReceiptExample example);

    List<IapReceipt> selectByExample(IapReceiptExample example);

    IapReceipt selectByPrimaryKey(String payOrderId);

    int updateByExampleSelective(@Param("record") IapReceipt record, @Param("example") IapReceiptExample example);

    int updateByExampleWithBLOBs(@Param("record") IapReceipt record, @Param("example") IapReceiptExample example);

    int updateByExample(@Param("record") IapReceipt record, @Param("example") IapReceiptExample example);

    int updateByPrimaryKeySelective(IapReceipt record);

    int updateByPrimaryKeyWithBLOBs(IapReceipt record);

    int updateByPrimaryKey(IapReceipt record);
}