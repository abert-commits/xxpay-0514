package org.xxpay.task.common.service;

import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;
import org.xxpay.core.service.*;

/**
 * @author: dingzhiwei
 * @date: 17/12/04
 * @description:
 */
@Service
public class RpcCommonService {

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchInfoService rpcMchInfoService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchAccountService rpcMchAccountService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchAccountHistoryService rpcMchAccountHistoryService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchSettDailyCollectService rpcMchSettDailyCollectService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IAgentInfoService rpcAgentInfoService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IAgentAccountService rpcAgentAccountService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IAgentAccountHistoryService rpcAgentAccountHistoryService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IAgentSettDailyCollectService rpcAgentSettDailyCollectService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IPayOrderService rpcPayOrderService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public ICheckService rpcCheckService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchBillService rpcMchBillService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IPayProductService rpcPayProductService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IMchBalanceService rpcMchBalanceService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IPinduoduoPreOrderService rpcIPinduoduoPreOrderService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IPinduoduoUserService rpcIPinduoduoUserService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IPinduoduoGoodsService rpcIPinduoduoGoodsService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IPinduoduoOrderService rpcIPinduoduoOrderService;

    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public IPinduoduoStoresService rpcIPinduoduoStoresService;


    @Reference(version = "1.0.0", timeout = 10000, retries = -1)
    public INginxService rpcINginxService;
}
