package org.xxpay.mch.common.service;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;
import org.xxpay.core.service.*;

/**
 * @author: dingzhiwei
 * @date: 17/12/05
 * @description:
 */
@Service
public class RpcCommonService {

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IPayPassageService rpcPayPassageService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchInfoService rpcMchInfoService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchAccountService rpcMchAccountService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchAccountHistoryService rpcMchAccountHistoryService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchBankAccountService rpcMchBankAccountService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public ISettRecordService rpcSettRecordService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchSettBatchRecordService rpcMchSettBatchRecordService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IPayOrderService rpcPayOrderService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public ITransOrderService rpcTransOrderService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IRefundOrderService rpcRefundOrderService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchAppService rpcMchAppService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IChannelConfigService rpcChannelConfigService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchTradeOrderService rpcMchTradeOrderService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchQrCodeService rpcMchQrCodeService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public ISysMessageService rpcSysMessageService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchBillService rpcMchBillService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IUserAccountService rpcUserAccountService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchAgentpayService rpcMchAgentpayService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public ISysService rpcSysService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchAgentpayPassageService rpcMchAgentpayPassageService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchPayPassageService rpcMchPayPassageService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IAgentpayPassageService rpcAgentpayPassageService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IPayProductService rpcPayProductService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IPayPassageAccountService rpcPayPassageAccountService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public ICommonService rpcCommonService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IXxPayAgentpayService rpcXxPayAgentpayService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public ISysLogService rpcSysLogService;

}
