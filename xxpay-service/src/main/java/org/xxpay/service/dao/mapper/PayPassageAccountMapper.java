package org.xxpay.service.dao.mapper;

import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.PayPassageAccount;
import org.xxpay.core.entity.PayPassageAccountExample;

import java.util.List;
import java.util.Map;

public interface PayPassageAccountMapper {
    int countByExample(PayPassageAccountExample example);

    int deleteByExample(PayPassageAccountExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(PayPassageAccount record);

    int insertSelective(PayPassageAccount record);

    List<PayPassageAccount> selectByExample(PayPassageAccountExample example);

    PayPassageAccount selectByPrimaryKey(Integer id);

    int updateByExampleSelective(@Param("record") PayPassageAccount record, @Param("example") PayPassageAccountExample example);

    int updateByExample(@Param("record") PayPassageAccount record, @Param("example") PayPassageAccountExample example);

    int updateByPrimaryKeySelective(PayPassageAccount record);

    int updateByPrimaryKeyAccount(PayPassageAccount record);

    int updateByPrimaryKey(PayPassageAccount record);

    /**
     * 查询可用子账户,按照子账号订单时间排序
     * @param param
     * @return
     */
    List<PayPassageAccount> selectAllByPassageId2(Map param);

}