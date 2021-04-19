package org.xxpay.service.dao.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import org.xxpay.core.entity.MchAccount;
import org.xxpay.core.entity.MchAccountExample;

import java.util.Map;

public interface MchAccountMapper {
    int countByExample(MchAccountExample example);

    int deleteByExample(MchAccountExample example);

    int deleteByPrimaryKey(Long mchId);

    int insert(MchAccount record);

    int insertSelective(MchAccount record);

    List<MchAccount> selectByExample(MchAccountExample example);

    MchAccount selectByPrimaryKey(Long mchId);

    int updateByExampleSelective(@Param("record") MchAccount record, @Param("example") MchAccountExample example);

    int updateByExample(@Param("record") MchAccount record, @Param("example") MchAccountExample example);

    int updateByPrimaryKeySelective(MchAccount record);

    int updateByPrimaryKey(MchAccount record);

    /**
     * 更新商户账户结算金额
     * @param map
     * @return
     */
    int updateSettAmount(Map map);
}