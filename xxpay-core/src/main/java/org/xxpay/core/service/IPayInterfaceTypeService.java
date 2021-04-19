package org.xxpay.core.service;

import org.xxpay.core.entity.PayInterfaceType;

import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 18/5/3
 * @description: 支付接口类型
 */
public interface IPayInterfaceTypeService {

    int add(PayInterfaceType payInterfaceType);

    int update(PayInterfaceType payInterfaceType);

    PayInterfaceType findByCode(String ifTypeCode);

    List<PayInterfaceType> select(int offset, int limit, PayInterfaceType payInterfaceType);

    Integer count(PayInterfaceType payInterfaceType);

    List<PayInterfaceType> selectAll(PayInterfaceType payInterfaceType);

}
