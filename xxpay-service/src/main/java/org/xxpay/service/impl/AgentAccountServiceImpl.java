package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.util.DateUtils;
import org.xxpay.core.common.util.MySeq;
import org.xxpay.core.entity.AgentAccount;
import org.xxpay.core.entity.AgentAccountExample;
import org.xxpay.core.entity.AgentAccountHistory;
import org.xxpay.core.entity.AgentInfo;
import org.xxpay.core.service.IAgentAccountService;
import org.xxpay.service.dao.mapper.AgentAccountHistoryMapper;
import org.xxpay.service.dao.mapper.AgentAccountMapper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 2018/4/29
 * @description:
 */
@Service(interfaceName = "org.xxpay.core.service.IAgentAccountService", version = "1.0.0", retries = -1)
public class AgentAccountServiceImpl implements IAgentAccountService {

    @Autowired
    private AgentAccountMapper agentAccountMapper;

    @Autowired
    private AgentAccountHistoryMapper agentAccountHistoryMapper;

    @Override
    public List<AgentAccount> listAll(int offset, int limit) {
        AgentAccountExample example = new AgentAccountExample();
        example.setOrderByClause("createTime asc");
        example.setLimit(limit);
        example.setOffset(offset);
        return agentAccountMapper.selectByExample(example);
    }

    @Override
    public List<AgentAccount> findAllList() {
        return agentAccountMapper.findAllList();
    }


    @Override
    public AgentAccount findByAgentId(Long agentId) {
        return agentAccountMapper.selectByPrimaryKey(agentId);
    }

    /**
     * 更新代理商可结算金额(更新后可结算金额=已有可结算金额+totalAmount)
     * @param agentId
     * @param totalAmount
     * @return
     */
    @Override
    public int updateSettAmount(long agentId, long totalAmount) {
        Map param = new HashMap<>();
        param.put("agentId", agentId);
        param.put("totalAmount", totalAmount);
        return agentAccountMapper.updateSettAmount(param);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void creditToAccount(Long agentId, Long amount, Byte bizType) {
        creditToAccount(agentId, amount, bizType, "", null);

    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void creditToAccount(Long agentId, Long amount, Byte bizType, String bizItem, String orderId) {
        AgentAccount agentAccount = findAvailableAccount(agentId);

        AgentAccount updateAgentAccount = new AgentAccount();
        // 更新今日进出账,不为同一天,直接清零
        Date lastAccountUpdateDate = agentAccount.getAccountUpdateTime(); // 账户修改时间
        if(!DateUtils.isSameDayWithToday(lastAccountUpdateDate)) {
            updateAgentAccount.setTodayExpend(0l);
            updateAgentAccount.setTodayIncome(0l);
        }

        // 总收益累加和今日收益
        if (MchConstant.AGENT_BIZ_TYPE_PROFIT == bizType) {// 业务类型是分润
            updateAgentAccount.setTotalIncome(agentAccount.getTotalIncome() + amount);

            /***** 根据上次修改时间，统计今日收益 *******/
            if (DateUtils.isSameDayWithToday(lastAccountUpdateDate)) {
                // 如果是同一天
                updateAgentAccount.setTodayIncome(updateAgentAccount.getTodayIncome() + amount);
            } else {
                // 不是同一天
                updateAgentAccount.setTodayIncome(amount);
            }
            /************************************/
        }

        // 记录资金流水
        AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
        agentAccountHistory.setAgentId(agentId);
        agentAccountHistory.setAmount(amount);
        agentAccountHistory.setBalance(agentAccount.getBalance());
        agentAccountHistory.setAfterBalance(agentAccount.getBalance() + amount);
        agentAccountHistory.setBizType(bizType);
        agentAccountHistory.setBizItem(bizItem);
        agentAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_ADD);
        agentAccountHistory.setOrderId(orderId);

        // 账户余额
        updateAgentAccount.setBalance(agentAccount.getBalance() + amount);
        // 账户可结算金额
        updateAgentAccount.setSettAmount(agentAccount.getSettAmount() + amount);
        AgentAccountExample example = new AgentAccountExample();
        AgentAccountExample.Criteria criteria = example.createCriteria();
        criteria.andAgentIdEqualTo(agentId);
        criteria.andBalanceEqualTo(agentAccount.getBalance());
        criteria.andSettAmountEqualTo(agentAccount.getSettAmount());

        // 数据操作,保证事务
        updateAccountAmount4Transactional(agentAccountHistory, updateAgentAccount, example);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void debitToAccount(Long agentId, Long amount, Byte bizType) {
        debitToAccount(agentId, amount, bizType, "");
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void debitToAccount(Long agentId, Long amount, Byte bizType, String bizItem) {
        AgentAccount agentAccount = findAvailableAccount(agentId);

        Long availableBalance = agentAccount.getAvailableBalance();
        if(availableBalance.compareTo(amount) == -1) {
            // 余额不足减款
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_BALANCE_OUT_LIMIT);
        }

        AgentAccount updateAgentAccount = new AgentAccount();
        // 更新今日进出账,不为同一天,直接清零
        Date lastUpdateDate = agentAccount.getAccountUpdateTime(); // 应该修改为账户修改时间
        if(!DateUtils.isSameDayWithToday(lastUpdateDate)) {
            updateAgentAccount.setTodayExpend(amount);
            updateAgentAccount.setTodayIncome(0l);
        }else {
            updateAgentAccount.setTodayExpend(agentAccount.getTodayExpend() + amount);
        }
        updateAgentAccount.setTotalExpend(agentAccount.getTotalExpend() + amount);
        // 更新后账户余额
        updateAgentAccount.setBalance(agentAccount.getBalance() - amount);

        // 记录资金流水
        AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
        agentAccountHistory.setAgentId(agentId);
        agentAccountHistory.setAmount(amount);
        agentAccountHistory.setBalance(agentAccount.getBalance());
        agentAccountHistory.setAfterBalance(agentAccount.getBalance() - amount);
        agentAccountHistory.setBizType(bizType);
        agentAccountHistory.setBizItem(bizItem);
        agentAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_SUB);

        // 账户余额
        AgentAccountExample example = new AgentAccountExample();
        AgentAccountExample.Criteria criteria = example.createCriteria();
        criteria.andAgentIdEqualTo(agentId);
        criteria.andBalanceEqualTo(agentAccount.getBalance());

        // 数据操作,保证事务
        updateAccountAmount4Transactional(agentAccountHistory, updateAgentAccount, example);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void freezeAmount(Long agentId, Long freezeAmount) {
        AgentAccount agentAccount = findAvailableAccount(agentId);
        // 验证可用余额是否足够
        if(!agentAccount.availableBalanceIsEnough(freezeAmount)) {
            // 冻结金额超限
            throw ServiceException.build(RetEnum.RET_SERVICE_FREEZE_AMOUNT_OUT_LIMIT);
        }

        // 更新冻结金额时,保证更新时数据一致,使用update锁
        AgentAccountExample example = new AgentAccountExample();
        AgentAccountExample.Criteria criteria = example.createCriteria();
        Long oldUnBalance = agentAccount.getUnBalance();
        BigDecimal newUnBalance = new BigDecimal(oldUnBalance).add(new BigDecimal(freezeAmount));
        criteria.andAgentIdEqualTo(agentId);
        criteria.andUnBalanceEqualTo(oldUnBalance);

        AgentAccount updateAgentAccount = new AgentAccount();
        updateAgentAccount.setUnBalance(newUnBalance.longValue());
        int updateCount = agentAccountMapper.updateByExampleSelective(updateAgentAccount, example);
        if(updateCount != 1) {
            // 更新失败,抛出异常
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_FROZEN_AMOUNT_FAIL);
        }
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void unFreezeAmount(Long agentId, Long amount, String requestNo, Byte bizType) {
        AgentAccount agentAccount = findAvailableAccount(agentId);

        AgentAccount updateAgentAccount = new AgentAccount();
        // 判断解冻金额是否超限
        if(agentAccount.getUnBalance() < amount) {
            throw ServiceException.build(RetEnum.RET_SERVICE_UN_FREEZE_AMOUNT_OUT_LIMIT);
        }

        // 记录资金流水
        AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
        agentAccountHistory.setAgentId(agentId);
        agentAccountHistory.setAmount(amount);
        agentAccountHistory.setBalance(agentAccount.getBalance());
        agentAccountHistory.setAfterBalance(agentAccount.getBalance() - amount);
        agentAccountHistory.setBizType(bizType);
        agentAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_SUB);

        // 更新账户
        AgentAccountExample example = new AgentAccountExample();
        AgentAccountExample.Criteria criteria = example.createCriteria();
        criteria.andAgentIdEqualTo(agentId);
        criteria.andUnBalanceEqualTo(agentAccount.getUnBalance());
        criteria.andBalanceEqualTo(agentAccount.getBalance());
        criteria.andSettAmountEqualTo(agentAccount.getSettAmount());
        // 更新今日进出账,不为同一天,直接清零
        Date lastUpdateDate = agentAccount.getAccountUpdateTime(); // 账户修改时间
        if(!DateUtils.isSameDayWithToday(lastUpdateDate)) {
            agentAccount.setTodayExpend(amount);
            agentAccount.setTodayIncome(0l);
        }else {
            agentAccount.setTodayExpend(agentAccount.getTodayExpend() + amount);
        }
        updateAgentAccount.setBalance(agentAccount.getBalance() - amount);          // 减款
        updateAgentAccount.setUnBalance(agentAccount.getUnBalance() - amount);      // 解冻
        updateAgentAccount.setSettAmount(agentAccount.getSettAmount() - amount);    // 减少可结算金额

        // 数据操作,保证事务
        updateAccountAmount4Transactional(agentAccountHistory, updateAgentAccount, example);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void unFreezeSettAmount(Long agentId, Long amount) {
        AgentAccount agentAccount = findAvailableAccount(agentId);
        // 判断解冻金额是否超限
        if(agentAccount.getUnBalance() < amount) {
            throw ServiceException.build(RetEnum.RET_SERVICE_UN_FREEZE_AMOUNT_OUT_LIMIT);
        }
        AgentAccount updateAgentAccount = new AgentAccount();
        // 更新今日进出账,不为同一天,直接清零
        Date lastUpdateDate = agentAccount.getAccountUpdateTime(); // 账户修改时间
        if(!DateUtils.isSameDayWithToday(lastUpdateDate)) {
            updateAgentAccount.setTodayExpend(0l);
            updateAgentAccount.setTodayIncome(0l);
        }
        // 更新账户不可用金额
        AgentAccountExample example = new AgentAccountExample();
        AgentAccountExample.Criteria criteria = example.createCriteria();
        criteria.andAgentIdEqualTo(agentId);
        criteria.andUnBalanceEqualTo(agentAccount.getUnBalance());
        updateAgentAccount.setUnBalance(agentAccount.getUnBalance() - amount);
        int updateCount = agentAccountMapper.updateByExampleSelective(updateAgentAccount, example);
        if(updateCount != 1) {
            // 更新失败,抛出异常
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_AMOUNT_UPDATE_FAIL);
        }
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void credit2Account(Long agentId, Byte bizType, Long amount) {
        credit2Account(agentId, bizType, amount, "");
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void credit2Account(Long agentId, Byte bizType, Long amount, String bizItem) {
        AgentAccount agentAccount = findAvailableAccount(agentId);

        AgentAccount updateAgentAccount = new AgentAccount();
        // 记录资金流水
        AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
        agentAccountHistory.setAgentId(agentId);
        agentAccountHistory.setAmount(amount);
        agentAccountHistory.setBalance(agentAccount.getBalance());
        agentAccountHistory.setAfterBalance(agentAccount.getBalance() + amount);
        agentAccountHistory.setBizType(bizType);
        agentAccountHistory.setBizItem(bizItem);
        agentAccountHistory.setOrderId(MySeq.getChangeAccount());
        agentAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_ADD);

        // 账户余额
        updateAgentAccount.setBalance(agentAccount.getBalance() + amount);
        updateAgentAccount.setSettAmount(agentAccount.getSettAmount() + amount);
        AgentAccountExample example = new AgentAccountExample();
        AgentAccountExample.Criteria criteria = example.createCriteria();
        criteria.andAgentIdEqualTo(agentId);
        criteria.andBalanceEqualTo(agentAccount.getBalance());
        criteria.andSettAmountEqualTo(agentAccount.getSettAmount());

        // 数据操作,保证事务
        updateAccountAmount4Transactional(agentAccountHistory, updateAgentAccount, example);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void debit2Account(Long agentId, Byte bizType, Long amount) {
        debit2Account(agentId, bizType, amount, "");
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void debit2Account(Long agentId, Byte bizType, Long amount, String bizItem) {
        AgentAccount agentAccount = findAvailableAccount(agentId);

        Long availableBalance = agentAccount.getAvailableBalance();
        if(availableBalance.compareTo(amount) == -1) {
            // 余额不足减款
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_BALANCE_OUT_LIMIT);
        }

        AgentAccount updateAgentAccount = new AgentAccount();

        // 更新后账户余额
        updateAgentAccount.setBalance(agentAccount.getBalance() - amount);
        // 更新后可结算金额
        updateAgentAccount.setSettAmount(agentAccount.getSettAmount() - amount);

        // 记录资金流水
        AgentAccountHistory agentAccountHistory = new AgentAccountHistory();
        agentAccountHistory.setAgentId(agentId);
        agentAccountHistory.setAmount(amount);
        agentAccountHistory.setBalance(agentAccount.getBalance());
        agentAccountHistory.setAfterBalance(agentAccount.getBalance() - amount);
        agentAccountHistory.setBizType(bizType);
        agentAccountHistory.setBizItem(bizItem);
        agentAccountHistory.setOrderId(MySeq.getChangeAccount());
        agentAccountHistory.setFundDirection(MchConstant.FUND_DIRECTION_SUB);

        // 账户余额
        AgentAccountExample example = new AgentAccountExample();
        AgentAccountExample.Criteria criteria = example.createCriteria();
        criteria.andAgentIdEqualTo(agentId);
        criteria.andBalanceEqualTo(agentAccount.getBalance());
        criteria.andSettAmountEqualTo(agentAccount.getSettAmount());

        // 数据操作,保证事务
        updateAccountAmount4Transactional(agentAccountHistory, updateAgentAccount, example);
    }

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    private void updateAccountAmount4Transactional(AgentAccountHistory agentAccountHistory, AgentAccount agentAccount, AgentAccountExample example) {
        agentAccountHistoryMapper.insertSelective(agentAccountHistory);
        int updateCount = agentAccountMapper.updateByExampleSelective(agentAccount, example);
        if(updateCount != 1) {
            // 更新失败,抛出异常
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_AMOUNT_UPDATE_FAIL);
        }
    }

    /**
     * 查找可用账户(代理商账户存在且状态正常)
     * @param agentId
     * @return
     */
    private AgentAccount findAvailableAccount(Long agentId) {
        AgentAccount agentAccount = findByAgentId(agentId);
        if(agentAccount == null || agentAccount.getStatus() != MchConstant.PUB_YES) {
            // 账户不存在
            throw ServiceException.build(RetEnum.RET_AGENT_ACCOUNT_NOT_EXIST);
        }
        return agentAccount;
    }

    @Override
    public BigDecimal sumAgentBalance(AgentAccount record) {
        AgentAccountExample exa = new AgentAccountExample();
        AgentAccountExample.Criteria c = exa.createCriteria();
        if(record!= null && record.getAgentId() != null){
            c.andAgentIdEqualTo(record.getAgentId());
        }
        return agentAccountMapper.sumAgentBalance(exa);
    }

    /**
     * 查询总代理下的二级代理余额总和
     */
    @Override
    public BigDecimal sumBalanceByParentAgentId(AgentInfo agentInfo) {
        return agentAccountMapper.sumBalanceByParentAgentId(agentInfo);
    }


}
