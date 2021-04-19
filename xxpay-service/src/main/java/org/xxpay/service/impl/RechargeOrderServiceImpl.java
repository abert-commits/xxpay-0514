package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayOrderExample;
import org.xxpay.core.entity.RechargeOrder;
import org.xxpay.core.service.IMchAccountService;
import org.xxpay.core.service.IPayOrderService;
import org.xxpay.core.service.IRechargeOrderService;
import org.xxpay.service.dao.mapper.PayOrderMapper;
import org.xxpay.service.dao.mapper.RechargeOrderMapper;

import java.util.*;

/**
 * @author: dingzhiwei
 * @date: 17/9/8
 * @description:
 */
@Service(interfaceName = "org.xxpay.core.service.IRechargeOrderService", version = "1.0.0", retries = -1)
public class RechargeOrderServiceImpl implements IRechargeOrderService {

    @Autowired
    private RechargeOrderMapper rechargeOrderMapper;

    @Autowired
    private IMchAccountService mchAccountService;


    @Override
    public RechargeOrder find(RechargeOrder rechargeOrder) {
        return null;
    }

    @Override
    public RechargeOrder findByRechargeOrderId(String rechargeOrderId) {
        return null;
    }

    @Override
    public RechargeOrder findByMchIdAndRechargeOrderId(Long mchId, String rechargeOrderId) {
        return null;
    }

    @Override
    public List<RechargeOrder> select(int offset, int limit, RechargeOrder rechargeOrder, Date createTimeStart, Date createTimeEnd) {
        return null;
    }

    @Override
    public List<RechargeOrder> select(Long mchId, int offset, int limit, RechargeOrder rechargeOrder, Date createTimeStart, Date createTimeEnd) {
        return null;
    }

    @Override
    public Integer count(Long mchId, RechargeOrder rechargeOrder, Date createTimeStart, Date createTimeEnd) {
        return null;
    }

    @Override
    public Integer count(RechargeOrder rechargeOrder, Date createTimeStart, Date createTimeEnd) {
        return null;
    }

    @Override
    public Integer count(RechargeOrder rechargeOrder, List<Byte> statusList) {
        return null;
    }

    @Override
    public int updateByRechargeOrderId(String rechargeOrderId, RechargeOrder rechargeOrder) {
        return 0;
    }

    @Override
    public int updateStatus4Ing(String rechargeOrderId, String channelOrderNo) {
        return 0;
    }

    @Override
    public int updateStatus4Success(String rechargeOrderId) {
        return 0;
    }

    @Override
    public int updateStatus4Fail(String rechargeOrderId) {
        return 0;
    }

    @Override
    public int updateStatus4Success(String rechargeOrderId, String channelOrderNo) {
        return 0;
    }

    @Override
    public int updateStatus4Complete(String rechargeOrderId) {
        return 0;
    }

    @Override
    public int createRechargeOrder(RechargeOrder rechargeOrder) {
        return 0;
    }
}
