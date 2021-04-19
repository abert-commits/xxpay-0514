package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.entity.MchAccount;
import org.xxpay.core.entity.MchAgentpayRecord;
import org.xxpay.core.entity.MchAgentpayRecordExample;
import org.xxpay.core.service.IMchAccountService;
import org.xxpay.core.service.IMchAgentpayService;
import org.xxpay.service.dao.mapper.MchAgentpayRecordMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 2018/4/21
 * @description:
 */
@Service(interfaceName = "org.xxpay.core.service.IMchAgentpayService", version = "1.0.0", retries = -1)
public class MchAgentpayServiceImpl implements IMchAgentpayService {

    @Autowired
    private IMchAccountService mchAccountService;

    @Autowired
    private MchAgentpayRecordMapper mchAgentpayRecordMapper;

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int applyAgentpay(MchAgentpayRecord mchAgentpayRecord) {
        Long mchId = mchAgentpayRecord.getMchId();
        // 查询商户账户信息,判断金额是否够结算
        MchAccount mchAccount = mchAccountService.findByMchId(mchId);
        if(mchAccount == null) {
            // 账户不存在
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_NOT_EXIST);
        }
        Long agentpayAmount = mchAgentpayRecord.getAmount() + mchAgentpayRecord.getFee();

        // 确定出款账户,判断是否足够出款
        if(MchConstant.AGENTPAY_OUT_BALANCE == 1) { // 从收款账户余额出款
            // 可结算金额
            Long availableAmount = mchAccount.getAvailableSettAmount();
            if(agentpayAmount.compareTo(availableAmount) > 0) {
                // 金额超限
                throw ServiceException.build(RetEnum.RET_SERVICE_SETT_AMOUNT_NOT_SETT);
            }
            // 冻结资金操作
            mchAccountService.freezeAmount(mchId, agentpayAmount);
        }else if(MchConstant.AGENTPAY_OUT_BALANCE == 2) { // 从代付账户余额出款
            // 可用代付余额
            Long AvailableAgentpayBalance = mchAccount.getAvailableAgentpayBalance();
            if(agentpayAmount.compareTo(AvailableAgentpayBalance) > 0) {
                // 金额超限
                throw ServiceException.build(RetEnum.RET_SERVICE_SETT_AMOUNT_NOT_SETT);
            }
            // 冻结资金操作
            mchAccountService.freeze4Agentpay(mchId, agentpayAmount);
        }else {
            throw ServiceException.build(RetEnum.RET_SERVICE_AGENTPAY_OUT_ERROR);
        }
        // 插入代付记录
        return mchAgentpayRecordMapper.insertSelective(mchAgentpayRecord);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int applyAgentpayBatch(Long mchId, Long agentpayAmount, List<MchAgentpayRecord> mchAgentpayRecordList) {
        // 查询商户账户信息,判断金额是否够结算
        MchAccount mchAccount = mchAccountService.findByMchId(mchId);
        if(mchAccount == null) {
            // 账户不存在
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_NOT_EXIST);
        }
        // 确定出款账户,判断是否足够出款
        if(MchConstant.AGENTPAY_OUT_BALANCE == 1) { // 从收款账户余额出款
            // 可结算金额
            Long availableAmount = mchAccount.getAvailableSettAmount();
            if(agentpayAmount.compareTo(availableAmount) > 0) {
                // 金额超限
                throw ServiceException.build(RetEnum.RET_SERVICE_SETT_AMOUNT_NOT_SETT);
            }
            // 冻结资金操作
            mchAccountService.freezeAmount(mchId, agentpayAmount);
        }else if(MchConstant.AGENTPAY_OUT_BALANCE == 2) { // 从代付账户余额出款
            // 可用代付余额
            Long AvailableAgentpayBalance = mchAccount.getAvailableAgentpayBalance();
            if(agentpayAmount.compareTo(AvailableAgentpayBalance) > 0) {
                // 金额超限
                throw ServiceException.build(RetEnum.RET_SERVICE_SETT_AMOUNT_NOT_SETT);
            }
            // 冻结资金操作
            mchAccountService.freeze4Agentpay(mchId, agentpayAmount);
        }else {
            throw ServiceException.build(RetEnum.RET_SERVICE_AGENTPAY_OUT_ERROR);
        }
        int batchCount = 0;
        for(MchAgentpayRecord mchAgentpayRecord : mchAgentpayRecordList) {
            batchCount += mchAgentpayRecordMapper.insertSelective(mchAgentpayRecord);
        }
        return batchCount;
    }

    @Override
    public int updateStatus4Ing(String agentpayOrderId, String transOrderId) {
        MchAgentpayRecord mchAgentpayRecord = new MchAgentpayRecord();
        if(StringUtils.isNotBlank(transOrderId)) mchAgentpayRecord.setTransOrderId(transOrderId);
        mchAgentpayRecord.setStatus(PayConstant.AGENTPAY_STATUS_ING);
        MchAgentpayRecordExample example = new MchAgentpayRecordExample();
        MchAgentpayRecordExample.Criteria criteria = example.createCriteria();
        criteria.andAgentpayOrderIdEqualTo(agentpayOrderId);
        criteria.andStatusEqualTo(PayConstant.AGENTPAY_STATUS_INIT);
        return mchAgentpayRecordMapper.updateByExampleSelective(mchAgentpayRecord, example);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int updateStatus4Success(String agentpayOrderId, String transOrderId, Integer agentpayPassageId) {
        MchAgentpayRecord updateMchAgentpayRecord = new MchAgentpayRecord();
        if(StringUtils.isNotBlank(transOrderId)) updateMchAgentpayRecord.setTransOrderId(transOrderId);
        updateMchAgentpayRecord.setStatus(PayConstant.AGENTPAY_STATUS_SUCCESS);
        MchAgentpayRecordExample example = new MchAgentpayRecordExample();
        MchAgentpayRecordExample.Criteria criteria = example.createCriteria();
        criteria.andAgentpayOrderIdEqualTo(agentpayOrderId);
        criteria.andStatusEqualTo(PayConstant.AGENTPAY_STATUS_ING);
        int result =  mchAgentpayRecordMapper.updateByExampleSelective(updateMchAgentpayRecord, example);
        if(result == 1) {
            // 查询代付记录
            MchAgentpayRecord queryMchAgentpayRecord = new MchAgentpayRecord();
            queryMchAgentpayRecord.setAgentpayOrderId(agentpayOrderId);
            MchAgentpayRecord mchAgentpayRecord = find(queryMchAgentpayRecord);
            // 解冻+减款
            mchAccountService.unFreeze4AgentpaySuccess(mchAgentpayRecord.getMchId(), mchAgentpayRecord.getSubAmount(),
                    mchAgentpayRecord.getAmount(), mchAgentpayRecord.getFee(), mchAgentpayRecord.getAgentpayOrderId(), MchConstant.BIZ_TYPE_AGENTPAY, agentpayPassageId);
        }
        return result;
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int updateStatus4Fail(String agentpayOrderId, String transOrderId, String transMsg) {
        MchAgentpayRecord updateMchAgentpayRecord = new MchAgentpayRecord();
        if(StringUtils.isNotBlank(transOrderId)) updateMchAgentpayRecord.setTransOrderId(transOrderId);
        if(StringUtils.isNotBlank(transMsg)) updateMchAgentpayRecord.setTransMsg(transMsg);
        updateMchAgentpayRecord.setStatus(PayConstant.AGENTPAY_STATUS_FAIL);
        MchAgentpayRecordExample example = new MchAgentpayRecordExample();
        MchAgentpayRecordExample.Criteria criteria = example.createCriteria();
        criteria.andAgentpayOrderIdEqualTo(agentpayOrderId);
        criteria.andStatusEqualTo(PayConstant.AGENTPAY_STATUS_ING);
        // 更新状态
        int result = mchAgentpayRecordMapper.updateByExampleSelective(updateMchAgentpayRecord, example);
        if(result == 1) {
            // 查询代付记录
            MchAgentpayRecord queryMchAgentpayRecord = new MchAgentpayRecord();
            queryMchAgentpayRecord.setAgentpayOrderId(agentpayOrderId);
            MchAgentpayRecord mchAgentpayRecord = find(queryMchAgentpayRecord);
            // 解冻金额
            mchAccountService.unFreeze4AgentpayFail(mchAgentpayRecord.getMchId(), mchAgentpayRecord.getAmount() + mchAgentpayRecord.getFee());
        }
        return result;
    }

    @Override
    public List<MchAgentpayRecord> select(int offset, int limit, MchAgentpayRecord mchAgentpayRecord, JSONObject queryObj) {
        MchAgentpayRecordExample example = new MchAgentpayRecordExample();
        example.setOrderByClause("createTime desc");
        example.setOffset(offset);
        example.setLimit(limit);
        MchAgentpayRecordExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchAgentpayRecord, queryObj);
        return mchAgentpayRecordMapper.selectByExample(example);
    }

    @Override
    public int updateTrans(String agentpayOrderId, String transOrderId, String transMsg) {
        MchAgentpayRecord updateMchAgentpayRecord = new MchAgentpayRecord();
        if(StringUtils.isNotBlank(transOrderId)) updateMchAgentpayRecord.setTransOrderId(transOrderId);
        if(StringUtils.isNotBlank(transMsg)) updateMchAgentpayRecord.setTransMsg(transMsg);
        MchAgentpayRecordExample example = new MchAgentpayRecordExample();
        MchAgentpayRecordExample.Criteria criteria = example.createCriteria();
        criteria.andAgentpayOrderIdEqualTo(agentpayOrderId);
        return mchAgentpayRecordMapper.updateByExampleSelective(updateMchAgentpayRecord, example);
    }

    @Override
    public int count(MchAgentpayRecord mchAgentpayRecord, JSONObject queryObj) {
        MchAgentpayRecordExample example = new MchAgentpayRecordExample();
        MchAgentpayRecordExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchAgentpayRecord, queryObj);
        return mchAgentpayRecordMapper.countByExample(example);
    }

    @Override
    public MchAgentpayRecord find(MchAgentpayRecord mchAgentpayRecord) {
        MchAgentpayRecordExample example = new MchAgentpayRecordExample();
        MchAgentpayRecordExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchAgentpayRecord);
        List<MchAgentpayRecord> mchAgentpayRecordList = mchAgentpayRecordMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(mchAgentpayRecordList)) return null;
        return mchAgentpayRecordList.get(0);
    }

    @Override
    public MchAgentpayRecord findByTransOrderId(String transOrderId) {
        MchAgentpayRecord mchAgentpayRecord = new MchAgentpayRecord();
        mchAgentpayRecord.setTransOrderId(transOrderId);
        return find(mchAgentpayRecord);
    }

    @Override
    public MchAgentpayRecord findByAgentpayOrderId(String agentpayOrderId) {
        return mchAgentpayRecordMapper.selectByPrimaryKey(agentpayOrderId);
    }

    @Override
    public MchAgentpayRecord findByMchIdAndAgentpayOrderId(Long mchId, String agentpayOrderId) {
        MchAgentpayRecord mchAgentpayRecord = new MchAgentpayRecord();
        mchAgentpayRecord.setMchId(mchId);
        mchAgentpayRecord.setAgentpayOrderId(agentpayOrderId);
        return find(mchAgentpayRecord);
    }

    @Override
    public MchAgentpayRecord findByMchIdAndMchOrderNo(Long mchId, String mchOrderNo) {
        MchAgentpayRecord mchAgentpayRecord = new MchAgentpayRecord();
        mchAgentpayRecord.setMchId(mchId);
        mchAgentpayRecord.setMchOrderNo(mchOrderNo);
        return find(mchAgentpayRecord);
    }

    @Override
    public List<MchAgentpayRecord> select(int offset, int limit, List<Byte> statusList, MchAgentpayRecord mchAgentpayRecord) {
        MchAgentpayRecordExample example = new MchAgentpayRecordExample();
        example.setOrderByClause("createTime desc");
        example.setOffset(offset);
        example.setLimit(limit);
        MchAgentpayRecordExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchAgentpayRecord);
        criteria.andStatusIn(statusList);
        return mchAgentpayRecordMapper.selectByExample(example);
    }

    @Override
    public int count(List<Byte> statusList, MchAgentpayRecord mchAgentpayRecord) {
        MchAgentpayRecordExample example = new MchAgentpayRecordExample();
        MchAgentpayRecordExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchAgentpayRecord);
        criteria.andStatusIn(statusList);
        return mchAgentpayRecordMapper.countByExample(example);
    }

    @Override
    public Map count4All(Long mchId, String accountName, String agentpayOrderId, String transOrderId, Byte status, Byte agentpayChannel, String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if(mchId != null) param.put("mchId", mchId);
        if(StringUtils.isNotBlank(agentpayOrderId)) param.put("agentpayOrderId", agentpayOrderId);
        if(StringUtils.isNotBlank(transOrderId)) param.put("transOrderId", transOrderId);
        if(StringUtils.isNotBlank(accountName)) param.put("accountName", accountName);
        if(status != null && status != -99) param.put("status", status);
        if(agentpayChannel != null && agentpayChannel != -99) param.put("agentpayChannel", agentpayChannel);
        if(StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if(StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return mchAgentpayRecordMapper.count4All(param);
    }

    void setCriteria(MchAgentpayRecordExample.Criteria criteria, MchAgentpayRecord mchAgentpayRecord) {
        setCriteria(criteria, mchAgentpayRecord, null);
    }

    void setCriteria(MchAgentpayRecordExample.Criteria criteria, MchAgentpayRecord mchAgentpayRecord, JSONObject queryObj) {
        if(mchAgentpayRecord != null) {
            if(mchAgentpayRecord.getMchId() != null) criteria.andMchIdEqualTo(mchAgentpayRecord.getMchId());
            if(StringUtils.isNotBlank(mchAgentpayRecord.getTransOrderId())) criteria.andTransOrderIdEqualTo(mchAgentpayRecord.getTransOrderId());
            if(StringUtils.isNotBlank(mchAgentpayRecord.getAgentpayOrderId())) criteria.andAgentpayOrderIdEqualTo(mchAgentpayRecord.getAgentpayOrderId());
            if(StringUtils.isNotBlank(mchAgentpayRecord.getMchOrderNo())) criteria.andMchOrderNoEqualTo(mchAgentpayRecord.getMchOrderNo());
            if(StringUtils.isNotBlank(mchAgentpayRecord.getAccountName())) criteria.andAccountNameEqualTo(mchAgentpayRecord.getAccountName());
            if(mchAgentpayRecord.getStatus() != null && mchAgentpayRecord.getStatus().byteValue() != -99) criteria.andStatusEqualTo(mchAgentpayRecord.getStatus());
            if(mchAgentpayRecord.getAgentpayChannel() != null && mchAgentpayRecord.getAgentpayChannel().byteValue() != 99) criteria.andAgentpayChannelEqualTo(mchAgentpayRecord.getAgentpayChannel());
        }
        if(queryObj != null) {
            if(queryObj.getDate("createTimeStart") != null) criteria.andCreateTimeGreaterThanOrEqualTo(queryObj.getDate("createTimeStart"));
            if(queryObj.getDate("createTimeEnd") != null) criteria.andCreateTimeLessThanOrEqualTo(queryObj.getDate("createTimeEnd"));
        }
    }

}
