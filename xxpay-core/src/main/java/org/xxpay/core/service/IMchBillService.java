package org.xxpay.core.service;

import org.xxpay.core.entity.MchBill;
import org.xxpay.core.entity.MchNotify;
import org.xxpay.core.entity.MchQrCode;

import java.util.Date;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 18/02/06
 * @description: 商户对账单
 */
public interface IMchBillService {

    MchBill findById(Long id);

    MchBill findByMchIdAndId(Long mchId, Long id);

    List<MchBill> select(int offset, int limit, MchBill mchBill);

    Integer count(MchBill mchBill);

    MchBill findByMchIdAndBillDate(Long mchId, Date billDate);

    int add(MchBill mchBill);

    int updateComplete(Long mchId, Date billDate);

}
