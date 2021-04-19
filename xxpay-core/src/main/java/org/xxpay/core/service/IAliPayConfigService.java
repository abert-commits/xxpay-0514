package org.xxpay.core.service;



import org.xxpay.core.entity.AlipayConfig;

import java.util.Date;
import java.util.List;

public interface IAliPayConfigService {


    int insert(AlipayConfig record);

    int insertSelective(AlipayConfig record);

    AlipayConfig selectById(Integer Id);

    int updateByPrimaryKeySelective(AlipayConfig record);

    int updateByPrimaryKey(AlipayConfig record);

    List<AlipayConfig> select(int offset, int limit, AlipayConfig record, Date createTimeStart, Date createTimeEnd);

    Integer count(AlipayConfig record,Date createTimeStart, Date createTimeEnd);

    AlipayConfig findAliPayConfig(AlipayConfig record);

}
