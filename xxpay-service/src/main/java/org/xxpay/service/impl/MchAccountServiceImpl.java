package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.util.DateUtils;
import org.xxpay.core.common.util.MySeq;
import org.xxpay.core.entity.*;
import org.xxpay.core.service.*;
import org.xxpay.service.dao.mapper.MchAccountHistoryMapper;
import org.xxpay.service.dao.mapper.MchAccountMapper;
import org.xxpay.service.dao.mapper.MchInfoMapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/12/4
 * @description:
 */
@Service(interfaceName = "org.xxpay.core.service.IMchAccountService", version = "1.0.0", retries = -1)
public class MchAccountServiceImpl implements IMchAccountService {

    @Autowired
    private MchInfoMapper mchInfoMapper;

    @Autowired
    private MchAccountMapper mchAccountMapper;

    @Autowired
    private MchAccountHistoryMapper mchAccountHistoryMapper;

    @Autowired
    private IMchInfoService mchInfoService;

    @Autowired
    private IAgentAgentpayPassageService agentAgentpayPassageService;

    @Autowired
    private IAgentAccountService agentAccountService;

    @Autowired
    private IAgentInfoService agentInfoService;


    /**
     * 查询商户账户列表
     *
     * @param offset
     * @param limit
     * @return
     */
    public List<MchAccount> listAll(int offset, int limit) {
        MchAccountExample example = new MchAccountExample();
        example.setOrderByClause("createTime asc");
        example.setLimit(limit);
        example.setOffset(offset);
        return mchAccountMapper.selectByExample(example);
    }

    /**
     * 更新用户可结算金额
     *
     * @param mchId
     * @param totalAmount
     * @return
     */
    public int updateSettAmount(long mchId, long totalAmount) {
        Map param = new HashMap<>();
        param.put("mchId", mchId);
        param.put("totalAmount", totalAmount);
        return mchAccountMapper.updateSettAmount(param);
    }

    @Override
    public MchAccount findByMchId(Long mchId) {
        return mchAccountMapper.selectByPrimaryKey(mchId);
    }

    /**
     * 当订单支付成功时,给商户账户加款,并记录资金流水
     *
     * @param payOrder
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void creditToAccount(PayOrder payOrder) {
        if (payOrder.getProductType() == MchConstant.PRODUCT_TYPE_PAY) {
            creditToAccount(payOrder.getMchId(), payOrder.getMchIncome(), payOrder.getAgentId(), payOrder.getParentAgentId(), payOrder.getAmount(),
                    payOrder.getAgentProfit(), payOrder.getParentAgentProfit(), payOrder.getPlatProfit(), payOrder.getChannelCost(), payOrder.getPayOrderId(),
                    payOrder.getChannelOrderNo(), MchConstant.BIZ_TYPE_TRANSACT);
        } else if (payOrder.getProductType() == MchConstant.PRODUCT_TYPE_RECHARGE) {
            recharge4Agentpay(payOrder.getMchId(), payOrder.getAgentId(), payOrder.getMchIncome(), payOrder.getAmount(),
                    payOrder.getAgentProfit(), payOrder.getParentAgentProfit(), payOrder.getPlatProfit(), payOrder.getChannelCost(), payOrder.getPayOrderId(),
                    payOrder.getChannelOrderNo(), MchConstant.BIZ_TYPE_RECHARGE, MchConstant.BIZ_ITEM_ONLINE);
        }
    }

    /**
     * 给商户账户加款,记录资金流水
     *
     * @param mchId             商户ID
     * @param mchIncome         商户入账
     * @param agentId           代理商ID
     * @param agentProfit       二级代理商利润
     * @param parentAgentProfit 一级代理商利润
     * @param orderAmount       订单金额
     * @param platProfit        平台利润
     * @param channelCost       渠道成本
     * @param requestNo         订单号
     * @param channelOrderNo    渠道订单号
     * @param bizType           业务类型
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void creditToAccount(Long mchId, Long mchIncome, Long agentId, Long parentAgentId, Long orderAmount, Long agentProfit, Long parentAgentProfit, Long platProfit, Long channelCost, String requestNo, String channelOrderNo, Byte bizType) {
        MchInfo mchInfo = findAvailableInfo(mchId);
        MchAccount mchAccount = findAvailableAccount(mchId);
        MchAccount updateMchAccount = new MchAccount();
        // 更新今日进出账,不为同一天,直接清零
        Date lastAccountUpdateDate = mchAccount.getAccountUpdateTime(); // 账户修改时间
        if (!DateUtils.isSameDayWithToday(lastAccountUpdateDate)) {
            updateMchAccount.setTodayExpend(0l);
            updateMchAccount.setTodayIncome(0l);
        }

        // 总收益累加和今日收益
        if (MchConstant.BIZ_TYPE_TRANSACT == bizType) {// 业务类型是交易
            updateMchAccount.setTotalIncome(mchAccount.getTotalIncome() + mchIncome);

            /***** 根据上次修改时间，统计今日收益 *******/
            if (DateUtils.isSameDayWithToday(lastAccountUpdateDate)) {
                // 如果是同一天
                updateMchAccount.setTodayIncome(mchAccount.getTodayIncome() + mchIncome);
            } else {
                // 不是同一天
                updateMchAccount.setTodayIncome(mchIncome);
            }
            /************************************/
        }

        // 记录商户资金流水
        MchAccountHistory mchAccountHistory = new MchAccountHistory();
        mchAccountHistory.setMchId(mchId);
        mchAccountHistory.setAmount(mchIncome);
        mchAccountHistory.setAgentId(agentId);
        mchAccountHistory.setParentAgentId(parentAgentId);
        mchAccountHistory.setOrderAmount(orderAmount);
        mchAccountHistory.setFee(orderAmount - mchIncome);
        mchAccountHistory.setAgentProfit(agentProfit);
        mchAccountHistory.setParentAgentProfit(parentAgentProfit);
        mchAccountHistory.setPlatProfit(platProfit);
        mchAccountHistory.setChannelCost(channelCost);
        mchAccountHistory.setBalance(mchAccount.getBalance());
        mchAccountHistory.setAfterBalance(mchAccount.getBalance() + mchIncome);
        mchAccountHistory.setBizType(bizType);
        mchAccountHistory.setBizItem(MchConstant.BIZ_ITEM_PAY);
        mchAccountHistory.setOrderId(requestNo);
        mchAccountHistory.setChannelOrderNo(channelOrderNo);
        mchAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_ADD);
        mchAccountHistory.setIsAllowSett(MchConstant.PUB_YES);
        mchAccountHistory.setMchSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setAgentSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setParentAgentSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setRiskDay(0);
        mchAccountHistory.setAgentRiskDay(agentInfoService.getRiskDay(agentId));  // 设置代理商风险预存期

        MchAccountExample example = new MchAccountExample();
        MchAccountExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andBalanceEqualTo(mchAccount.getBalance());

        // 账户余额
        updateMchAccount.setBalance(mchAccount.getBalance() + mchIncome);

        // 重新得到商户的结算方式
        mchInfo = mchInfoService.reBuildMchInfoSettConfig(mchInfo);


        // 如果为实时结算,则增加账户可结算金额
        if (mchInfo.getSettMode() == MchConstant.SETT_TYPE_D0) { // 如果是D0到账
            mchAccountHistory.setMchSettStatus(MchConstant.PUB_YES); // 设置为商户已结算,保证结算任务不去结算该笔
            updateMchAccount.setSettAmount(mchAccount.getSettAmount() + mchIncome);
            criteria.andSettAmountEqualTo(mchAccount.getSettAmount());
        }

        // 数据操作,保证事务
        updateAccountAmount4Transactional(mchAccountHistory, updateMchAccount, example);

//        if (agentId != null && agentProfit > 0) {
//            agentAccountService.creditToAccount(agentId, agentProfit, MchConstant.AGENT_BIZ_TYPE_PROFIT, MchConstant.BIZ_ITEM_PAY, requestNo);
//        }

    }

    // 给账户减款
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void debitToAccount(Long mchId, Long amount, String requestNo, String channelOrderNo, Byte bizType) {
        MchAccount mchAccount = findAvailableAccount(mchId);

        Long availableBalance = mchAccount.getAvailableBalance();
        if (availableBalance.compareTo(amount) == -1) {
            // 余额不足减款
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_BALANCE_OUT_LIMIT);
        }

        MchAccount updateMchAccount = new MchAccount();

        // 更新今日进出账,不为同一天,直接清零
        Date lastUpdateDate = mchAccount.getAccountUpdateTime(); // 账户修改时间
        if (!DateUtils.isSameDayWithToday(lastUpdateDate)) {
            updateMchAccount.setTodayExpend(amount);
            updateMchAccount.setTodayIncome(0l);
        } else {
            updateMchAccount.setTodayExpend(mchAccount.getTodayExpend() + amount);
        }
        updateMchAccount.setTotalExpend(mchAccount.getTotalExpend() + amount);
        // 更新后账户余额
        updateMchAccount.setBalance(mchAccount.getBalance() - amount);

        // 记录资金流水
        MchAccountHistory mchAccountHistory = new MchAccountHistory();
        mchAccountHistory.setMchId(mchId);
        mchAccountHistory.setAmount(amount);
        mchAccountHistory.setBalance(mchAccount.getBalance());
        mchAccountHistory.setAfterBalance(mchAccount.getBalance() - amount);
        mchAccountHistory.setBizType(bizType);
        mchAccountHistory.setOrderId(requestNo);
        mchAccountHistory.setChannelOrderNo(channelOrderNo);
        mchAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_SUB);
        mchAccountHistory.setIsAllowSett(MchConstant.PUB_YES);
        mchAccountHistory.setMchSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setAgentSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setParentAgentSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setRiskDay(0);

        // 账户余额
        MchAccountExample example = new MchAccountExample();
        MchAccountExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andBalanceEqualTo(mchAccount.getBalance());

        // 数据操作,保证事务
        updateAccountAmount4Transactional(mchAccountHistory, updateMchAccount, example);
    }

    // 冻结用户金额(增加用户不可用金额)
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void freezeAmount(Long mchId, Long freezeAmount) {
        MchAccount mchAccount = findAvailableAccount(mchId);
        // 验证是否够冻结
        if (!mchAccount.availableBalanceIsEnough(freezeAmount)) {
            // 冻结金额超限
            throw ServiceException.build(RetEnum.RET_SERVICE_FREEZE_AMOUNT_OUT_LIMIT);
        }

        // 更新冻结金额时,保证更新时数据一致,使用update锁
        MchAccountExample example = new MchAccountExample();
        MchAccountExample.Criteria criteria = example.createCriteria();
        Long oldUnBalance = mchAccount.getUnBalance();
        BigDecimal newUnBalance = new BigDecimal(oldUnBalance).add(new BigDecimal(freezeAmount));
        criteria.andMchIdEqualTo(mchId);
        criteria.andUnBalanceEqualTo(oldUnBalance);

        mchAccount = new MchAccount();
        mchAccount.setUnBalance(newUnBalance.longValue());
        mchAccount.setAccountUpdateTime(new Date());
        int updateCount = mchAccountMapper.updateByExampleSelective(mchAccount, example);
        if (updateCount != 1) {
            // 更新失败,抛出异常
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_FROZEN_AMOUNT_FAIL);
        }
    }

    // 解冻用户金额+减款(减少用户余额,减少用户不可用余额,减少用户可结算金额)
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void unFreezeAmount(Long mchId, Long amount, Long orderAmount, Long fee, String requestNo, Byte bizType) {
        MchAccount mchAccount = findAvailableAccount(mchId);
        MchAccount updateMchAccount = new MchAccount();

        // 判断解冻金额是否超限
        if (mchAccount.getUnBalance() < amount) {
            throw ServiceException.build(RetEnum.RET_SERVICE_UN_FREEZE_AMOUNT_OUT_LIMIT);
        }

        // 记录资金流水
        MchAccountHistory mchAccountHistory = new MchAccountHistory();
        mchAccountHistory.setMchId(mchId);
        mchAccountHistory.setAmount(amount);
        mchAccountHistory.setBalance(mchAccount.getBalance());
        mchAccountHistory.setAfterBalance(mchAccount.getBalance() - amount);
        if (orderAmount != null) mchAccountHistory.setOrderAmount(orderAmount);
        if (fee != null) mchAccountHistory.setFee(fee);
        mchAccountHistory.setBizType(bizType);
        mchAccountHistory.setOrderId(requestNo);
        mchAccountHistory.setChannelOrderNo(null);
        mchAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_SUB);
        mchAccountHistory.setIsAllowSett(MchConstant.PUB_NO);
        mchAccountHistory.setMchSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setAgentSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setParentAgentSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setRiskDay(0);

        // 更新账户
        MchAccountExample example = new MchAccountExample();
        MchAccountExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andUnBalanceEqualTo(mchAccount.getUnBalance());
        criteria.andBalanceEqualTo(mchAccount.getBalance());
        criteria.andSettAmountEqualTo(mchAccount.getSettAmount());
        // 更新今日进出账,不为同一天,直接清零
        Date lastUpdateDate = mchAccount.getAccountUpdateTime(); // 账户修改时间
        if (!DateUtils.isSameDayWithToday(lastUpdateDate)) {
            updateMchAccount.setTodayExpend(amount);
            updateMchAccount.setTodayIncome(0l);
        } else {
            updateMchAccount.setTodayExpend(mchAccount.getTodayExpend() + amount);
        }
        updateMchAccount.setBalance(mchAccount.getBalance() - amount);          // 减款
        updateMchAccount.setUnBalance(mchAccount.getUnBalance() - amount);      // 解冻
        updateMchAccount.setSettAmount(mchAccount.getSettAmount() - amount);    // 减少可结算金额

        // 数据操作,保证事务
        updateAccountAmount4Transactional(mchAccountHistory, updateMchAccount, example);

    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    void updateAccountAmount4Transactional(MchAccountHistory mchAccountHistory, MchAccount updateMchAccount, MchAccountExample example) {
        mchAccountHistoryMapper.insertSelective(mchAccountHistory);
        updateMchAccount.setAccountUpdateTime(new Date());
        int updateCount = mchAccountMapper.updateByExampleSelective(updateMchAccount, example);
        if (updateCount != 1) {
            // 更新失败,抛出异常
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_AMOUNT_UPDATE_FAIL);
        }
    }

    // 解冻用户金额(减少用户不可用金额)
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void unFreezeSettAmount(Long mchId, Long amount) {
        MchAccount mchAccount = findAvailableAccount(mchId);
        // 判断解冻金额是否超限
        if (mchAccount.getUnBalance() < amount) {
            throw ServiceException.build(RetEnum.RET_SERVICE_UN_FREEZE_AMOUNT_OUT_LIMIT);
        }
        MchAccount updateMchAccount = new MchAccount();
        // 更新今日进出账,不为同一天,直接清零
        Date lastUpdateDate = mchAccount.getAccountUpdateTime(); // 账户修改时间
        if (!DateUtils.isSameDayWithToday(lastUpdateDate)) {
            updateMchAccount.setTodayExpend(0l);
            updateMchAccount.setTodayIncome(0l);
        }
        // 更新账户不可用金额
        MchAccountExample example = new MchAccountExample();
        MchAccountExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andUnBalanceEqualTo(mchAccount.getUnBalance());
        updateMchAccount.setUnBalance(mchAccount.getUnBalance() - amount);
        updateMchAccount.setAccountUpdateTime(new Date());
        int updateCount = mchAccountMapper.updateByExampleSelective(updateMchAccount, example);
        if (updateCount != 1) {
            // 更新失败,抛出异常
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_AMOUNT_UPDATE_FAIL);
        }
    }

    // 冻结用户金额(增加用户不可用代付余额)
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void freeze4Agentpay(Long mchId, Long freezeAmount) {
        MchAccount mchAccount = findAvailableAccount(mchId);
        // 验证代付余额是否够冻结
        if (!mchAccount.availableAgentpayBalanceIsEnough(freezeAmount)) {
            // 冻结金额超限
            throw ServiceException.build(RetEnum.RET_SERVICE_FREEZE_AMOUNT_OUT_LIMIT);
        }

        // 更新冻结金额时,保证更新时数据一致,使用update锁
        MchAccountExample example = new MchAccountExample();
        MchAccountExample.Criteria criteria = example.createCriteria();
        Long oldUnAgentpayBalance = mchAccount.getUnAgentpayBalance();
        BigDecimal newUnAgentpayBalance = new BigDecimal(oldUnAgentpayBalance).add(new BigDecimal(freezeAmount));
        criteria.andMchIdEqualTo(mchId);
        criteria.andUnAgentpayBalanceEqualTo(oldUnAgentpayBalance);

        mchAccount = new MchAccount();
        mchAccount.setUnAgentpayBalance(newUnAgentpayBalance.longValue());
        mchAccount.setAccountUpdateTime(new Date());
        int updateCount = mchAccountMapper.updateByExampleSelective(mchAccount, example);
        if (updateCount != 1) {
            // 更新失败,抛出异常
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_FROZEN_AMOUNT_FAIL);
        }
    }

    // 代付成功,解冻+减少代付余额
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void unFreeze4AgentpaySuccess(Long mchId, Long amount, Long orderAmount, Long fee, String requestNo, Byte bizType, Integer agentpayPassageId) {
        MchInfo mchInfo = findAvailableInfo(mchId);
        MchAccount mchAccount = findAvailableAccount(mchId);
        MchAccount updateMchAccount = new MchAccount();

        // 判断解冻金额是否超限
        if (MchConstant.AGENTPAY_OUT_BALANCE == 1) { // 从收款账户余额出款
            if (mchAccount.getUnBalance() < amount) {
                throw ServiceException.build(RetEnum.RET_SERVICE_UN_FREEZE_AMOUNT_OUT_LIMIT);
            }
        } else if (MchConstant.AGENTPAY_OUT_BALANCE == 2) { // 从代付账户余额出款
            if (mchAccount.getUnAgentpayBalance() < amount) {
                throw ServiceException.build(RetEnum.RET_SERVICE_UN_FREEZE_AMOUNT_OUT_LIMIT);
            }
        } else {
            throw ServiceException.build(RetEnum.RET_SERVICE_AGENTPAY_OUT_ERROR);
        }

        // 如果商户属于代理商,给代理商增加分润
        Long agentProfit = 0l;
        Long parentAgentProfit = 0l;
        Long agentId = mchInfo.getAgentId();                    // 二级代理商ID
        Long parentAgentId = mchInfo.getParentAgentId();        // 一级代理商ID
        if (agentId != null) {
            AgentAgentpayPassage agentAgentpayPassage = agentAgentpayPassageService.findByAgentIdAndPassageId(agentId, agentpayPassageId);
            if (agentAgentpayPassage != null) {
                Long agentFee = agentAgentpayPassage.getFeeEvery();
                // 二级代理分润
                agentProfit = (fee - agentFee) < 0 ? 0 : (fee - agentFee);
                // 处理一级代理商
                if (parentAgentId != null && parentAgentId != 0) {
                    AgentAgentpayPassage parentAgentAgentpayPassage = agentAgentpayPassageService.findByAgentIdAndPassageId(parentAgentId, agentpayPassageId);
                    if (agentAgentpayPassage != null && parentAgentAgentpayPassage != null) {
                        Long parentAgentFee = parentAgentAgentpayPassage.getFeeEvery();
                        // 一级代理分润
                        parentAgentProfit = (agentFee - parentAgentFee) < 0 ? 0 : (agentFee - parentAgentFee);
                    }
                }
            }
        }

        // 平台利润
        Long platProfit = fee - agentProfit - parentAgentProfit;

        // 记录资金流水
        MchAccountHistory mchAccountHistory = new MchAccountHistory();
        mchAccountHistory.setMchId(mchId);
        mchAccountHistory.setParentAgentId(parentAgentId);
        mchAccountHistory.setAgentId(agentId);
        mchAccountHistory.setAmount(amount);
        mchAccountHistory.setBalance(mchAccount.getAgentpayBalance());
        mchAccountHistory.setAfterBalance(mchAccount.getAgentpayBalance() - amount);
        mchAccountHistory.setAgentProfit(agentProfit);  // 代理商利润
        mchAccountHistory.setParentAgentProfit(parentAgentProfit);  // 一级代理利润
        mchAccountHistory.setPlatProfit(platProfit);    // 平台利润
        if (orderAmount != null) mchAccountHistory.setOrderAmount(orderAmount);
        if (fee != null) mchAccountHistory.setFee(fee);
        mchAccountHistory.setBizType(bizType);
        mchAccountHistory.setBizItem(MchConstant.BIZ_ITEM_AGENTPAY);
        mchAccountHistory.setOrderId(requestNo);
        mchAccountHistory.setChannelOrderNo(null);
        mchAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_SUB);
        // 设置为允许结算,商户已结算,代理商未结算(代理商结算任务会去处理)
        mchAccountHistory.setIsAllowSett(MchConstant.PUB_YES);
        mchAccountHistory.setMchSettStatus(MchConstant.PUB_YES);
        mchAccountHistory.setAgentSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setParentAgentSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setRiskDay(0);
        mchAccountHistory.setAgentRiskDay(agentInfoService.getRiskDay(agentId));  // 设置代理商风险预存期

        // 更新账户
        MchAccountExample example = new MchAccountExample();
        MchAccountExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);

        // 根据代付出款类型,设置账户扣减后金额
        if (MchConstant.AGENTPAY_OUT_BALANCE == 1) { // 从收款账户余额出款
            criteria.andUnBalanceEqualTo(mchAccount.getUnBalance());
            criteria.andBalanceEqualTo(mchAccount.getBalance());
            criteria.andSettAmountEqualTo(mchAccount.getSettAmount());

            updateMchAccount.setBalance(mchAccount.getBalance() - amount);          // 减款
            updateMchAccount.setUnBalance(mchAccount.getUnBalance() - amount);      // 解冻
            updateMchAccount.setSettAmount(mchAccount.getSettAmount() - amount);    // 减少可结算金额
        } else if (MchConstant.AGENTPAY_OUT_BALANCE == 2) { // 从代付账户余额出款
            criteria.andAgentpayBalanceEqualTo(mchAccount.getAgentpayBalance());        // 代付余额
            criteria.andUnAgentpayBalanceEqualTo(mchAccount.getUnAgentpayBalance());    // 不可用代付余额

            updateMchAccount.setAgentpayBalance(mchAccount.getAgentpayBalance() - amount);          // 代付余额减款
            updateMchAccount.setUnAgentpayBalance(mchAccount.getUnAgentpayBalance() - amount);      // 不可用代付余额解冻
        }

        // 更新今日进出账,不为同一天,直接清零
        /*Date lastUpdateDate = mchAccount.getAccountUpdateTime(); // 账户修改时间
        if(!DateUtils.isSameDayWithToday(lastUpdateDate)) {
            updateMchAccount.setTodayExpend(amount);
            updateMchAccount.setTodayIncome(0l);
        }else {
            updateMchAccount.setTodayExpend(mchAccount.getTodayExpend() + amount);
        }*/


        // 数据操作,保证事务
        updateAccountAmount4Transactional(mchAccountHistory, updateMchAccount, example);
    }

    // 代付失败解冻(减少用户不可用代付余额)
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void unFreeze4AgentpayFail(Long mchId, Long amount) {
        MchAccount mchAccount = findAvailableAccount(mchId);
        // 判断解冻金额是否超限
        if (MchConstant.AGENTPAY_OUT_BALANCE == 1) { // 从收款账户余额出款
            if (mchAccount.getUnBalance() < amount) {
                throw ServiceException.build(RetEnum.RET_SERVICE_UN_FREEZE_AMOUNT_OUT_LIMIT);
            }
        } else if (MchConstant.AGENTPAY_OUT_BALANCE == 2) { // 从代付账户余额出款
            if (mchAccount.getUnAgentpayBalance() < amount) {
                throw ServiceException.build(RetEnum.RET_SERVICE_UN_FREEZE_AMOUNT_OUT_LIMIT);
            }
        } else {
            throw ServiceException.build(RetEnum.RET_SERVICE_AGENTPAY_OUT_ERROR);
        }

        MchAccount updateMchAccount = new MchAccount();
        // 更新今日进出账,不为同一天,直接清零
        /*Date lastUpdateDate = mchAccount.getAccountUpdateTime(); // 账户修改时间
        if(!DateUtils.isSameDayWithToday(lastUpdateDate)) {
            updateMchAccount.setTodayExpend(0l);
            updateMchAccount.setTodayIncome(0l);
        }*/
        // 更新账户不可用代付余额
        MchAccountExample example = new MchAccountExample();
        MchAccountExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);

        // 根据代付出款类型,设置账户扣减后金额
        if (MchConstant.AGENTPAY_OUT_BALANCE == 1) { // 从收款账户余额出款
            criteria.andUnBalanceEqualTo(mchAccount.getUnBalance());
            updateMchAccount.setUnBalance(mchAccount.getUnBalance() - amount);
        } else if (MchConstant.AGENTPAY_OUT_BALANCE == 2) { // 从代付账户余额出款
            criteria.andUnAgentpayBalanceEqualTo(mchAccount.getUnAgentpayBalance());
            updateMchAccount.setUnAgentpayBalance(mchAccount.getUnAgentpayBalance() - amount);
        }

        updateMchAccount.setAccountUpdateTime(new Date());
        int updateCount = mchAccountMapper.updateByExampleSelective(updateMchAccount, example);
        if (updateCount != 1) {
            // 更新失败,抛出异常
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_AMOUNT_UPDATE_FAIL);
        }
    }

    /**
     * 给商户账户加钱(一般用于调账操作)
     *
     * @param mchId
     * @param bizType
     * @param amount
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void credit2Account(Long mchId, Byte bizType, Long amount, String bizItem, String remark) {
        credit2Account(mchId, bizType, amount, MySeq.getChangeAccount(), null, null, bizItem, remark);
    }

    /**
     * 给商户账户加钱(一般用于调账操作)
     *
     * @param mchId
     * @param bizType
     * @param amount
     * @param orderId
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void credit2Account(Long mchId, Byte bizType, Long amount, String orderId, Long orderAmount, Long fee, String bizItem, String remark) {
        MchInfo mchInfo = findAvailableInfo(mchId);
        MchAccount mchAccount = findAvailableAccount(mchId);
        MchAccount updateMchAccount = new MchAccount();

        // 记录资金流水
        MchAccountHistory mchAccountHistory = new MchAccountHistory();
        mchAccountHistory.setMchId(mchId);
        mchAccountHistory.setAgentId(mchInfo.getAgentId());
        mchAccountHistory.setAmount(amount);
        mchAccountHistory.setBizType(bizType);
        mchAccountHistory.setBizItem(bizItem);
        mchAccountHistory.setOrderId(orderId);
        mchAccountHistory.setOrderAmount(orderAmount);
        mchAccountHistory.setFee(fee);
        mchAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_ADD);
        mchAccountHistory.setIsAllowSett(MchConstant.PUB_NO);
        mchAccountHistory.setMchSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setAgentSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setParentAgentSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setRiskDay(0);
        mchAccountHistory.setRemark(remark);

        // 账户信息
        MchAccountExample example;
        MchAccountExample.Criteria criteria;
        switch (bizItem) {
            case MchConstant.BIZ_ITEM_BALANCE:
                mchAccountHistory.setBalance(mchAccount.getBalance());
                mchAccountHistory.setAfterBalance(mchAccount.getBalance() + amount);
                // 账户余额
                updateMchAccount.setBalance(mchAccount.getBalance() + amount);
                updateMchAccount.setSettAmount(mchAccount.getSettAmount() + amount);
                example = new MchAccountExample();
                criteria = example.createCriteria();
                criteria.andMchIdEqualTo(mchId);
                criteria.andBalanceEqualTo(mchAccount.getBalance());
                criteria.andSettAmountEqualTo(mchAccount.getSettAmount());
                break;
            case MchConstant.BIZ_ITEM_AGENTPAY_BALANCE:
                mchAccountHistory.setBalance(mchAccount.getAgentpayBalance());
                mchAccountHistory.setAfterBalance(mchAccount.getAgentpayBalance() + amount);
                // 代付余额
                updateMchAccount.setAgentpayBalance(mchAccount.getAgentpayBalance() + amount);
                example = new MchAccountExample();
                criteria = example.createCriteria();
                criteria.andMchIdEqualTo(mchId);
                criteria.andAgentpayBalanceEqualTo(mchAccount.getAgentpayBalance());
                break;
            case MchConstant.BIZ_ITEM_FROZEN_MONEY:
                mchAccountHistory.setBalance(mchAccount.getFrozenMoney());
                mchAccountHistory.setAfterBalance(mchAccount.getFrozenMoney() + amount);
                // 冻结金额
                updateMchAccount.setFrozenMoney(mchAccount.getFrozenMoney() + amount);
                example = new MchAccountExample();
                criteria = example.createCriteria();
                criteria.andMchIdEqualTo(mchId);
                criteria.andFrozenMoneyEqualTo(mchAccount.getFrozenMoney());
                break;
            case MchConstant.BIZ_ITEM_SECURITY_MONEY:
                mchAccountHistory.setBalance(mchAccount.getSecurityMoney());
                mchAccountHistory.setAfterBalance(mchAccount.getSecurityMoney() + amount);
                // 保证金额
                updateMchAccount.setSecurityMoney(mchAccount.getSecurityMoney() + amount);
                example = new MchAccountExample();
                criteria = example.createCriteria();
                criteria.andMchIdEqualTo(mchId);
                criteria.andSecurityMoneyEqualTo(mchAccount.getSecurityMoney());
                break;
            default:
                throw new ServiceException(RetEnum.RET_SERVICE_ACCOUNT_TYPE_NOT_EXIST);
        }
        // 数据操作,保证事务
        updateAccountAmount4Transactional(mchAccountHistory, updateMchAccount, example);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void recharge4Agentpay(Long mchId, Long agentId, Long mchIncome, Long orderAmount, Long agentProfit, Long parentAgentProfit, Long platProfit, Long channelCost, String requestNo, String channelOrderNo, Byte bizType, String bizItem) {

        MchInfo mchInfo = findAvailableInfo(mchId);
        MchAccount mchAccount = findAvailableAccount(mchId);
        MchAccount updateMchAccount = new MchAccount();

        // 1. 记录商户资金流水
        MchAccountHistory mchAccountHistory = new MchAccountHistory();
        mchAccountHistory.setMchId(mchId);
        mchAccountHistory.setAgentId(mchInfo.getAgentId());
        mchAccountHistory.setParentAgentId(mchInfo.getParentAgentId());
        mchAccountHistory.setAmount(mchIncome);
        mchAccountHistory.setBalance(mchAccount.getAgentpayBalance());
        mchAccountHistory.setAfterBalance(mchAccount.getAgentpayBalance() + mchIncome);
        mchAccountHistory.setAgentProfit(agentProfit);  // 代理商分润
        mchAccountHistory.setParentAgentProfit(parentAgentProfit);  // 一级代理商分润
        mchAccountHistory.setPlatProfit(platProfit);    // 平台利润
        mchAccountHistory.setChannelCost(channelCost);
        mchAccountHistory.setBizType(bizType);
        mchAccountHistory.setBizItem(bizItem);
        mchAccountHistory.setOrderId(requestNo);
        mchAccountHistory.setChannelOrderNo(channelOrderNo);
        mchAccountHistory.setOrderAmount(orderAmount);
        mchAccountHistory.setFee(orderAmount - mchIncome);
        mchAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_ADD);
        mchAccountHistory.setIsAllowSett(MchConstant.PUB_YES);
        mchAccountHistory.setMchSettStatus(MchConstant.PUB_YES);
        mchAccountHistory.setAgentSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setParentAgentSettStatus(MchConstant.PUB_NO);
        mchAccountHistory.setRiskDay(0);
        mchAccountHistory.setAgentRiskDay(agentInfoService.getRiskDay(agentId));  // 设置代理商风险预存期

        // 2. 增加商户代付余额
        updateMchAccount.setAgentpayBalance(mchAccount.getAgentpayBalance() + mchIncome);
        MchAccountExample example = new MchAccountExample();
        MchAccountExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andAgentpayBalanceEqualTo(mchAccount.getAgentpayBalance());
        // 数据操作,保证事务
        updateAccountAmount4Transactional(mchAccountHistory, updateMchAccount, example);

    }

    /**
     * 给商户账户减钱(一般用于调账操作)
     *
     * @param mchId
     * @param bizType
     * @param amount
     */
    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void debit2Account(Long mchId, Byte bizType, Long amount, String bizItem) {
        MchInfo mchInfo = findAvailableInfo(mchId);
        MchAccount mchAccount = findAvailableAccount(mchId);
        Long balance;
        MchAccount updateMchAccount;
        MchAccountHistory mchAccountHistory;
        MchAccountExample example;
        MchAccountExample.Criteria criteria;
        switch (bizItem) {
            case MchConstant.BIZ_ITEM_BALANCE:
                balance = mchAccount.getAvailableBalance();
                if (balance.compareTo(amount) == -1) {
                    // 余额不足减款
                    throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_BALANCE_OUT_LIMIT);
                }
                updateMchAccount = new MchAccount();
                // 更新后账户余额
                updateMchAccount.setBalance(mchAccount.getBalance() - amount);
                // 更新后可结算金额
                updateMchAccount.setSettAmount(mchAccount.getSettAmount() - amount);
                // 记录资金流水
                mchAccountHistory = new MchAccountHistory();
                mchAccountHistory.setMchId(mchId);
                mchAccountHistory.setAgentId(mchInfo.getAgentId());
                mchAccountHistory.setAmount(amount);
                mchAccountHistory.setBalance(mchAccount.getBalance());
                mchAccountHistory.setAfterBalance(mchAccount.getBalance() - amount);
                mchAccountHistory.setBizType(bizType);
                mchAccountHistory.setBizItem(bizItem);
                mchAccountHistory.setOrderId(MySeq.getChangeAccount());
                mchAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_SUB);
                mchAccountHistory.setIsAllowSett(MchConstant.PUB_NO);
                mchAccountHistory.setMchSettStatus(MchConstant.PUB_NO);
                mchAccountHistory.setAgentSettStatus(MchConstant.PUB_NO);
                mchAccountHistory.setParentAgentSettStatus(MchConstant.PUB_NO);
                mchAccountHistory.setRiskDay(0);
                // 账户余额
                example = new MchAccountExample();
                criteria = example.createCriteria();
                criteria.andMchIdEqualTo(mchId);
                criteria.andBalanceEqualTo(mchAccount.getBalance());
                criteria.andSettAmountEqualTo(mchAccount.getSettAmount());
                // 数据操作,保证事务
                updateAccountAmount4Transactional(mchAccountHistory, updateMchAccount, example);
                break;
            case MchConstant.BIZ_ITEM_AGENTPAY_BALANCE:
                balance = mchAccount.getAvailableAgentpayBalance();
                if (balance.compareTo(amount) == -1) {
                    // 余额不足减款
                    throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_BALANCE_OUT_LIMIT);
                }
                updateMchAccount = new MchAccount();
                // 更新后代付余额
                updateMchAccount.setAgentpayBalance(mchAccount.getAgentpayBalance() - amount);
                // 记录资金流水
                mchAccountHistory = new MchAccountHistory();
                mchAccountHistory.setMchId(mchId);
                mchAccountHistory.setAgentId(mchInfo.getAgentId());
                mchAccountHistory.setAmount(amount);
                mchAccountHistory.setBalance(mchAccount.getAgentpayBalance());
                mchAccountHistory.setAfterBalance(mchAccount.getAgentpayBalance() - amount);
                mchAccountHistory.setBizType(bizType);
                mchAccountHistory.setBizItem(bizItem);
                mchAccountHistory.setOrderId(MySeq.getChangeAccount());
                mchAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_SUB);
                mchAccountHistory.setIsAllowSett(MchConstant.PUB_NO);
                mchAccountHistory.setMchSettStatus(MchConstant.PUB_NO);
                mchAccountHistory.setAgentSettStatus(MchConstant.PUB_NO);
                mchAccountHistory.setParentAgentSettStatus(MchConstant.PUB_NO);
                mchAccountHistory.setRiskDay(0);
                // 账户余额
                example = new MchAccountExample();
                criteria = example.createCriteria();
                criteria.andMchIdEqualTo(mchId);
                criteria.andBalanceEqualTo(mchAccount.getBalance());
                criteria.andSettAmountEqualTo(mchAccount.getSettAmount());
                // 数据操作,保证事务
                updateAccountAmount4Transactional(mchAccountHistory, updateMchAccount, example);
                break;
            case MchConstant.BIZ_ITEM_FROZEN_MONEY:
                balance = mchAccount.getFrozenMoney();
                if (balance.compareTo(amount) == -1) {
                    // 余额不足减款
                    throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_BALANCE_OUT_LIMIT);
                }
                updateMchAccount = new MchAccount();
                // 更新后冻结金额
                updateMchAccount.setFrozenMoney(mchAccount.getFrozenMoney() - amount);
                // 记录资金流水
                mchAccountHistory = new MchAccountHistory();
                mchAccountHistory.setMchId(mchId);
                mchAccountHistory.setAgentId(mchInfo.getAgentId());
                mchAccountHistory.setAmount(amount);
                mchAccountHistory.setBalance(mchAccount.getFrozenMoney());
                mchAccountHistory.setAfterBalance(mchAccount.getFrozenMoney() - amount);
                mchAccountHistory.setBizType(bizType);
                mchAccountHistory.setBizItem(bizItem);
                mchAccountHistory.setOrderId(MySeq.getChangeAccount());
                mchAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_SUB);
                mchAccountHistory.setIsAllowSett(MchConstant.PUB_NO);
                mchAccountHistory.setMchSettStatus(MchConstant.PUB_NO);
                mchAccountHistory.setAgentSettStatus(MchConstant.PUB_NO);
                mchAccountHistory.setParentAgentSettStatus(MchConstant.PUB_NO);
                mchAccountHistory.setRiskDay(0);
                // 冻结金额
                example = new MchAccountExample();
                criteria = example.createCriteria();
                criteria.andMchIdEqualTo(mchId);
                criteria.andFrozenMoneyEqualTo(mchAccount.getFrozenMoney());
                // 数据操作,保证事务
                updateAccountAmount4Transactional(mchAccountHistory, updateMchAccount, example);
                break;
            case MchConstant.BIZ_ITEM_SECURITY_MONEY:
                balance = mchAccount.getSecurityMoney();
                if (balance.compareTo(amount) == -1) {
                    // 余额不足减款
                    throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_BALANCE_OUT_LIMIT);
                }
                updateMchAccount = new MchAccount();
                // 更新后保证金额
                updateMchAccount.setSecurityMoney(mchAccount.getSecurityMoney() - amount);
                // 记录资金流水
                mchAccountHistory = new MchAccountHistory();
                mchAccountHistory.setMchId(mchId);
                mchAccountHistory.setAgentId(mchInfo.getAgentId());
                mchAccountHistory.setAmount(amount);
                mchAccountHistory.setBalance(mchAccount.getSecurityMoney());
                mchAccountHistory.setAfterBalance(mchAccount.getSecurityMoney() - amount);
                mchAccountHistory.setBizType(bizType);
                mchAccountHistory.setBizItem(bizItem);
                mchAccountHistory.setOrderId(MySeq.getChangeAccount());
                mchAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_SUB);
                mchAccountHistory.setIsAllowSett(MchConstant.PUB_NO);
                mchAccountHistory.setMchSettStatus(MchConstant.PUB_NO);
                mchAccountHistory.setAgentSettStatus(MchConstant.PUB_NO);
                mchAccountHistory.setParentAgentSettStatus(MchConstant.PUB_NO);
                mchAccountHistory.setRiskDay(0);
                // 冻结保证金额
                example = new MchAccountExample();
                criteria = example.createCriteria();
                criteria.andMchIdEqualTo(mchId);
                criteria.andSecurityMoneyEqualTo(mchAccount.getSecurityMoney());
                // 数据操作,保证事务
                updateAccountAmount4Transactional(mchAccountHistory, updateMchAccount, example);
                break;
            default:
                throw new ServiceException(RetEnum.RET_SERVICE_ACCOUNT_TYPE_NOT_EXIST);
        }


    }

    /**
     * 查找可用账户(商户账户存在且状态正常)
     *
     * @param mchId
     * @return
     */
    private MchAccount findAvailableAccount(Long mchId) {
        MchAccount mchAccount = findByMchId(mchId);
        if (mchAccount == null || mchAccount.getStatus() != MchConstant.PUB_YES) {
            // 账户不存在
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_NOT_EXIST);
        }
        return mchAccount;
    }

    /**
     * 查找可用商户(商户信息存在且状态正常)
     *
     * @param mchId
     * @return
     */
    private MchInfo findAvailableInfo(Long mchId) {
        MchInfo mchInfo = mchInfoMapper.selectByPrimaryKey(mchId);
        if (mchInfo == null || mchInfo.getStatus() != MchConstant.PUB_YES) {
            // 账户不存在
            throw ServiceException.build(RetEnum.RET_SERVICE_MCH_NOT_EXIST);
        }
        return mchInfo;
    }

}
