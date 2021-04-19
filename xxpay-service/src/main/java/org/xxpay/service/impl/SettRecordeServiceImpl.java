package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.MySeq;
import org.xxpay.core.entity.*;
import org.xxpay.core.service.*;
import org.xxpay.service.dao.mapper.SettRecordMapper;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: dingzhiwei
 * @date: 2018/5/19
 * @description:
 */
@Service(interfaceName = "org.xxpay.core.service.ISettRecordService", version = "1.0.0", retries = -1)
public class SettRecordeServiceImpl implements ISettRecordService {

    @Autowired
    private SettRecordMapper settRecordMapper;

    @Autowired
    private IMchInfoService mchInfoService;

    @Autowired
    private IMchAccountService mchAccountService;

    @Autowired
    private IAgentInfoService agentInfoService;

    @Autowired
    private IAgentAccountService agentAccountService;

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int applySett(Byte infoType, Long infoId, Long settAmount, String bankName, String bankNetName, String accountName, String accountNo) {
        if(infoType == MchConstant.SETT_INFO_TYPE_AGENT) {
            return applySett4Agent(infoId, settAmount, bankName, bankNetName, accountName, accountNo);
        }else if(infoType == MchConstant.SETT_INFO_TYPE_MCH) {
            return applySett4Mch(infoId, settAmount, bankName, bankNetName, accountName, accountNo);
        }
        return 0;
    }

    @Override
    public List<SettRecord> select(int offset, int limit, SettRecord settRecord, JSONObject queryObj) {
        SettRecordExample example = new SettRecordExample();
        example.setOrderByClause("createTime desc");
        example.setOffset(offset);
        example.setLimit(limit);
        SettRecordExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, settRecord, queryObj);
        return settRecordMapper.selectByExample(example);
    }

    @Override
    public int count(SettRecord settRecord, JSONObject queryObj) {
        SettRecordExample example = new SettRecordExample();
        SettRecordExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, settRecord, queryObj);
        return settRecordMapper.countByExample(example);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int auditSett(Long id, Byte status, String remark) {
        SettRecord settRecord = settRecordMapper.selectByPrimaryKey(id);
        if(settRecord == null) {
            throw ServiceException.build(RetEnum.RET_SERVICE_SETT_RECORD_NOT_EXIST);
        }
        // 状态必须为审核中
        if(!settRecord.getSettStatus().equals(MchConstant.SETT_STATUS_AUDIT_ING)) {
            throw ServiceException.build(RetEnum.RET_SERVICE_SETT_STATUS_ERROR);
        }
        SettRecordExample example = new SettRecordExample();
        SettRecordExample.Criteria criteria = example.createCriteria();
        SettRecord updateSettRecord = new SettRecord();
        updateSettRecord.setSettStatus(status);
        updateSettRecord.setRemark(remark);
        criteria.andIdEqualTo(id);
        int updateCount = settRecordMapper.updateByExampleSelective(updateSettRecord, example);
        // 审核不通过,需要解冻金额(申请金额+手续费)
        if(updateCount == 1 && status == MchConstant.SETT_STATUS_AUDIT_NOT) {
            if(settRecord.getInfoType() == MchConstant.SETT_INFO_TYPE_AGENT) { // 代理商
                agentAccountService.unFreezeSettAmount(settRecord.getInfoId(), settRecord.getSettAmount() + settRecord.getSettFee());
            }else if(settRecord.getInfoType() == MchConstant.SETT_INFO_TYPE_MCH) {   // 商户
                mchAccountService.unFreezeSettAmount(settRecord.getInfoId(), settRecord.getSettAmount() + settRecord.getSettFee());
            }
        }
        return updateCount;
    }

    @Override
    public SettRecord find(SettRecord settRecord) {
        SettRecordExample example = new SettRecordExample();
        example.setOrderByClause("createTime desc");
        SettRecordExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, settRecord);
        List<SettRecord> settRecordList = settRecordMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(settRecordList)) return null;
        return settRecordList.get(0);
    }

    @Override
    public SettRecord findById(Long id) {
        return settRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    public SettRecord findByTransOrderId(String transOrderId) {
        SettRecord settRecord = new SettRecord();
        settRecord.setTransOrderId(transOrderId);
        return find(settRecord);
    }

    @Override
    public SettRecord findBySettOrderId(String settOrderId) {
        SettRecord settRecord = new SettRecord();
        settRecord.setSettOrderId(settOrderId);
        return find(settRecord);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int remit(Long id, Byte status, String remark, String remitRemark, String transOrderId, String transMsg) {
        SettRecord settRecord = settRecordMapper.selectByPrimaryKey(id);
        if(settRecord == null) {
            throw ServiceException.build(RetEnum.RET_SERVICE_SETT_RECORD_NOT_EXIST);
        }
        // 状态必须为审核成功|打款中
        if(!settRecord.getSettStatus().equals(MchConstant.SETT_STATUS_AUDIT_OK) &&
                !settRecord.getSettStatus().equals(MchConstant.SETT_STATUS_REMIT_ING)) {
            throw ServiceException.build(RetEnum.RET_SERVICE_SETT_STATUS_ERROR);
        }

        SettRecordExample example = new SettRecordExample();
        SettRecordExample.Criteria criteria = example.createCriteria();
        SettRecord updateSettRecord = new SettRecord();
        updateSettRecord.setSettStatus(status);
        updateSettRecord.setRemark(remark);
        updateSettRecord.setRemitRemark(remitRemark);
        updateSettRecord.setTransOrderId(transOrderId);
        updateSettRecord.setTransMsg(transMsg);
        criteria.andIdEqualTo(id);
        int updateCount = settRecordMapper.updateByExampleSelective(updateSettRecord, example);
        if(updateCount == 1) {
            if(settRecord.getInfoType() == MchConstant.SETT_INFO_TYPE_AGENT) { // 代理商
                if(status == MchConstant.SETT_STATUS_REMIT_FAIL) {
                    // 打款失败,解冻金额(申请金额+手续费)
                    agentAccountService.unFreezeSettAmount(settRecord.getInfoId(), settRecord.getSettAmount() + settRecord.getSettFee());
                }else if(status == MchConstant.SETT_STATUS_REMIT_SUCCESS) {
                    // 打款成功,解冻+减款(金额=申请金额+手续费)
                    agentAccountService.unFreezeAmount(settRecord.getInfoId(), settRecord.getSettAmount() + settRecord.getSettFee(), settRecord.getSettOrderId(), MchConstant.BIZ_TYPE_REMIT);
                }
            }else if(settRecord.getInfoType() == MchConstant.SETT_INFO_TYPE_MCH) {   // 商户
                if(status == MchConstant.SETT_STATUS_REMIT_FAIL) {
                    // 打款失败,解冻金额(申请金额+手续费)
                    mchAccountService.unFreezeSettAmount(settRecord.getInfoId(), settRecord.getSettAmount() + settRecord.getSettFee());
                }else if(status == MchConstant.SETT_STATUS_REMIT_SUCCESS) {
                    // 打款成功,解冻+减款(金额=申请金额+手续费)
                    mchAccountService.unFreezeAmount(settRecord.getInfoId(), settRecord.getSettAmount() + settRecord.getSettFee(),
                            settRecord.getSettAmount(), settRecord.getSettFee(), settRecord.getSettOrderId(), MchConstant.BIZ_TYPE_REMIT);
                }
            }
        }
        return updateCount;
    }

    @Override
    public int updateTrans(Long id, String transOrderId, String transMsg) {
        SettRecordExample example = new SettRecordExample();
        SettRecordExample.Criteria criteria = example.createCriteria();
        SettRecord updateSettRecord = new SettRecord();
        updateSettRecord.setTransOrderId(transOrderId);
        updateSettRecord.setTransMsg(transMsg);
        criteria.andIdEqualTo(id);
        return settRecordMapper.updateByExampleSelective(updateSettRecord, example);
    }

    @Override
    public List<SettRecord> select(int offset, int limit, List<Byte> settStatusList, SettRecord settRecord, JSONObject queryObj) {
        SettRecordExample example = new SettRecordExample();
        example.setOrderByClause("createTime desc");
        example.setOffset(offset);
        example.setLimit(limit);
        SettRecordExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, settRecord, queryObj);
        criteria.andSettStatusIn(settStatusList);
        return settRecordMapper.selectByExample(example);
    }

    @Override
    public int count(List<Byte> settStatusList, SettRecord settRecord, JSONObject queryObj) {
        SettRecordExample example = new SettRecordExample();
        SettRecordExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, settRecord, queryObj);
        criteria.andSettStatusIn(settStatusList);
        return settRecordMapper.countByExample(example);
    }

    @Override
    public Map count4Sett(String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if(StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if(StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return settRecordMapper.count4Sett(param);
    }

    /**
     * 计算结算手续费
     * @param settAmount 结算金额
     * @param feeType    结算类型   1-百分比收费,2-固定收费
     * @param feeRate    结算费率
     * @param feeLevel   每笔手续费
     * @param drawFeeLimit  手续费上限
     * @return
     */
    long calculationFee(Long settAmount, Byte feeType, BigDecimal feeRate, String feeLevel, Long drawFeeLimit) {
        if(feeType == 1) {
            return NumberUtils.min(new BigDecimal(settAmount).multiply(feeRate).divide(BigDecimal.valueOf(100), 2, BigDecimal.ROUND_HALF_UP).longValue(), drawFeeLimit);
        }else if(feeType == 2) {
            return NumberUtils.min(new BigDecimal(feeLevel).longValue(), drawFeeLimit);
        }
        throw new ServiceException(RetEnum.RET_COMM_UNKNOWN_ERROR);
    }

    /**
     * 发起代理商结算申请
     * @param infoId
     * @param settAmount
     * @return
     */
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    int applySett4Agent(Long infoId, Long settAmount, String bankName, String bankNetName, String accountName, String accountNo) {
        // 判断商户状态
        AgentInfo info = agentInfoService.findByAgentId(infoId);
        if(info == null) {
            // 账户不存在
            throw ServiceException.build(RetEnum.RET_SERVICE_AGENT_NOT_EXIST);
        }
        if(info.getStatus() != MchConstant.PUB_YES) {
            // 停止使用
            throw ServiceException.build(RetEnum.RET_AGENT_STATUS_STOP);
        }

        // 判断账户状态
        AgentAccount account = agentAccountService.findByAgentId(infoId);
        if(account == null) {
            // 账户不存在
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_NOT_EXIST);
        }
        if(account.getStatus() != MchConstant.PUB_YES) {
            // 账户停止
            throw ServiceException.build(RetEnum.RET_AGENT_STATUS_STOP);
        }

        // 重新设置商户结算信息
        info = agentInfoService.reBuildAgentInfoSettConfig(info);

        // 判断金额是否够结算
        long settFee = calculationFee(settAmount, info.getFeeType(), info.getFeeRate(), info.getFeeLevel(), info.getDrawFeeLimit());    // 手续费
        Long debitAmount = settAmount + settFee;                                                                // 扣减代理商金额 = 结算金额 + 手续费
        Long availableAmount = account.getAvailableSettAmount();
        if(debitAmount.compareTo(availableAmount) > 0) {
            // 金额超限
            throw ServiceException.build(RetEnum.RET_SERVICE_SETT_AMOUNT_NOT_SETT);
        }

        // 判断是否允许提现
        isAllowDraw(infoId, settAmount, info.getDrawFlag(), info.getAllowDrawWeekDay(), info.getDrawDayStartTime(), info.getDrawDayEndTime(),
                info.getDayDrawTimes(), info.getDrawMaxDayAmount(), info.getMaxDrawAmount(), info.getMinDrawAmount());

        // 冻结资金操作
        agentAccountService.freezeAmount(infoId, debitAmount);

        SettRecord settRecord = new SettRecord();
        settRecord.setSettOrderId(MySeq.getSett());
        settRecord.setInfoId(infoId);
        settRecord.setInfoType(MchConstant.SETT_INFO_TYPE_AGENT);   // 代理商
        settRecord.setSettType((byte) 1);       // 手工结算
        settRecord.setSettDate(new Date());     // 结算日期
        settRecord.setSettAmount(settAmount);   // 申请结算金额
        settRecord.setSettFee(settFee);             // 结算手续费
        settRecord.setRemitAmount(settAmount);      // 打款金额 = 结算金额
        if(StringUtils.isNotBlank(bankName) && StringUtils.isNotBlank(bankNetName) && StringUtils.isNotBlank(accountName) && StringUtils.isNotBlank(accountNo)) {
            settRecord.setAccountAttr((byte) 0);    // 对私
            settRecord.setAccountType((byte) 1);    // 银行卡转账
            settRecord.setBankName(bankName);
            settRecord.setBankNetName(bankNetName);
            settRecord.setAccountName(accountName);
            settRecord.setAccountNo(accountNo);
            settRecord.setProvince("");
            settRecord.setCity("");
        }else {
            settRecord.setAccountAttr(info.getAccountAttr());
            settRecord.setAccountType(info.getAccountType());
            settRecord.setBankName(info.getBankName());
            settRecord.setBankNetName(info.getBankNetName());
            settRecord.setAccountName(info.getAccountName());
            settRecord.setAccountNo(info.getAccountNo());
            settRecord.setProvince(info.getProvince());
            settRecord.setCity(info.getCity());
        }
        settRecord.setCity(info.getCity());
        settRecord.setSettStatus(MchConstant.SETT_STATUS_AUDIT_ING);    // 结算状态
        settRecord.setRemark("");
        settRecord.setRemitRemark("");
        int insertCount = settRecordMapper.insertSelective(settRecord);

        return insertCount;
    }

    /**
     * 发起商户结算申请
     * @param infoId
     * @param settAmount
     * @return
     */
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    int applySett4Mch(Long infoId, Long settAmount, String bankName, String bankNetName, String accountName, String accountNo) {
        // 判断商户状态
        MchInfo info = mchInfoService.findByMchId(infoId);
        if(info == null) {
            // 账户不存在
            throw ServiceException.build(RetEnum.RET_SERVICE_MCH_NOT_EXIST);
        }
        if(info.getStatus() != MchConstant.PUB_YES) {
            // 商户停止使用
            throw ServiceException.build(RetEnum.RET_MCH_STATUS_STOP);
        }

        // 判断账户状态
        MchAccount account = mchAccountService.findByMchId(infoId);
        if(account == null) {
            // 账户不存在
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_NOT_EXIST);
        }
        if(account.getStatus() != MchConstant.PUB_YES) {
            // 账户停止
            throw ServiceException.build(RetEnum.RET_MCH_STATUS_STOP);
        }

        // 重新设置商户结算信息
        info = mchInfoService.reBuildMchInfoSettConfig(info);

        // 判断金额是否够结算
        long settFee = calculationFee(settAmount, info.getFeeType(), info.getFeeRate(), info.getFeeLevel(), info.getDrawFeeLimit());    // 手续费
        Long debitAmount = settAmount + settFee;                                                                // 扣减商户金额=结算金额 + 手续费
        Long availableAmount = account.getAvailableSettAmount();
        if(debitAmount.compareTo(availableAmount) > 0) {
            // 金额超限
            throw ServiceException.build(RetEnum.RET_SERVICE_SETT_AMOUNT_NOT_SETT);
        }

        // 判断是否允许提现
        isAllowDraw(infoId, settAmount, info.getDrawFlag(), info.getAllowDrawWeekDay(), info.getDrawDayStartTime(), info.getDrawDayEndTime(),
                info.getDayDrawTimes(), info.getDrawMaxDayAmount(), info.getMaxDrawAmount(), info.getMinDrawAmount());

        // 冻结资金操作
        mchAccountService.freezeAmount(infoId, debitAmount);

        SettRecord settRecord = new SettRecord();
        settRecord.setSettOrderId(MySeq.getSett());
        settRecord.setInfoId(infoId);
        settRecord.setInfoType(MchConstant.SETT_INFO_TYPE_MCH);   // 商户
        settRecord.setSettType((byte) 1);   // 手工结算
        settRecord.setSettDate(new Date()); // 结算日期
        settRecord.setSettAmount(settAmount);   // 申请结算金额
        settRecord.setSettFee(settFee);             // 结算手续费
        settRecord.setRemitAmount(settAmount);      // 打款金额 = 结算金额
        if(StringUtils.isNotBlank(bankName) && StringUtils.isNotBlank(bankNetName) && StringUtils.isNotBlank(accountName) && StringUtils.isNotBlank(accountNo)) {
            settRecord.setAccountAttr((byte) 0);    // 对私
            settRecord.setAccountType((byte) 1);    // 银行卡转账
            settRecord.setBankName(bankName);
            settRecord.setBankNetName(bankNetName);
            settRecord.setAccountName(accountName);
            settRecord.setAccountNo(accountNo);
            settRecord.setProvince("");
            settRecord.setCity("");
        }else {
            settRecord.setAccountAttr(info.getAccountAttr());
            settRecord.setAccountType(info.getAccountType());
            settRecord.setBankName(info.getBankName());
            settRecord.setBankNetName(info.getBankNetName());
            settRecord.setAccountName(info.getAccountName());
            settRecord.setAccountNo(info.getAccountNo());
            settRecord.setProvince(info.getProvince());
            settRecord.setCity(info.getCity());
        }
        settRecord.setSettStatus(MchConstant.SETT_STATUS_AUDIT_ING);    // 结算状态
        settRecord.setRemark("");
        settRecord.setRemitRemark("");
        int insertCount = settRecordMapper.insertSelective(settRecord);

        return insertCount;
    }

    @Override
    public Map count4All(Long infoId, String accountName, String settOrderId, Byte settStatus, String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if(infoId != null) param.put("infoId", infoId);
        if(StringUtils.isNotBlank(settOrderId)) param.put("settOrderId", settOrderId);
        if(StringUtils.isNotBlank(accountName)) param.put("accountName", accountName);
        if(settStatus != null && settStatus != -99) param.put("settStatus", settStatus);
        if(StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if(StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return settRecordMapper.count4All(param);
    }

    void setCriteria(SettRecordExample.Criteria criteria, SettRecord settRecord) {
        setCriteria(criteria, settRecord, null);
    }

    void setCriteria(SettRecordExample.Criteria criteria, SettRecord settRecord, JSONObject queryObj) {
        if(settRecord != null) {
            if(settRecord.getInfoId() != null) criteria.andInfoIdEqualTo(settRecord.getInfoId());
            if(StringUtils.isNotBlank(settRecord.getSettOrderId())) criteria.andSettOrderIdEqualTo(settRecord.getSettOrderId());
            if(StringUtils.isNotBlank(settRecord.getTransOrderId())) criteria.andTransOrderIdEqualTo(settRecord.getTransOrderId());
            if(StringUtils.isNotBlank(settRecord.getAccountName())) criteria.andAccountNameEqualTo(settRecord.getAccountName());
            if(settRecord.getId() != null) criteria.andIdEqualTo(settRecord.getId());
            if(settRecord.getSettStatus() != null && settRecord.getSettStatus().byteValue() != -99) criteria.andSettStatusEqualTo(settRecord.getSettStatus());
            if(settRecord.getInfoType() != null && settRecord.getInfoType().byteValue() != -99) criteria.andInfoTypeEqualTo(settRecord.getInfoType());
        }
        if(queryObj != null) {
            if(queryObj.getDate("createTimeStart") != null) criteria.andCreateTimeGreaterThanOrEqualTo(queryObj.getDate("createTimeStart"));
            if(queryObj.getDate("createTimeEnd") != null) criteria.andCreateTimeLessThanOrEqualTo(queryObj.getDate("createTimeEnd"));
        }
    }

    public String isAllowApply(Byte drawFlag, String allowDrawWeekDay, String drawStartTime, String drawEndTime) {
        // 判断当前提现开关是否开启
        if(drawFlag != 1) {
            return "系统提现关闭,暂时不允许提现";
        }
        // 判断周几可以提现,1,2,3格式存储,7表示周日
        if(StringUtils.isNotBlank(allowDrawWeekDay)) {
            boolean allowDrawWeekFlag = false;
            String currentWeek = String.valueOf(DateUtil.getCurrentWeek());
            String[] weeks = allowDrawWeekDay.split(",");
            for(String week : weeks) {
                if(week.equals(currentWeek)) {
                    allowDrawWeekFlag = true;
                    break;
                }
            }
            // 当天不允许提现
            if(!allowDrawWeekFlag) {
                return "今天不允许提现,系统开放提现为星期:" + allowDrawWeekDay;
            }
        }
        // 提现时间是否满足
        String ymd = DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD);
        String drawDayStartTime = ymd + " " + drawStartTime;
        String drawDayEndTime = ymd + " " + drawEndTime;
        long startTime = DateUtil.str2date(drawDayStartTime).getTime();
        long endTime = DateUtil.str2date(drawDayEndTime).getTime();
        long currentTime = System.currentTimeMillis();
        if(currentTime < startTime || currentTime > endTime) {
            return "当前时间不允许提现,允许提现时间:" + drawStartTime + " - " + drawEndTime;
        }
        return "";
    }

    void isAllowDraw(Long infoId, Long settAmount, Byte drawFlag, String allowDrawWeekDay, String drawDayStartTime, String drawDayEndTime,
                     Integer dayDrawTimes, Long drawMaxDayAmount, Long maxDrawAmount, Long minDrawAmount) {

        // 1. 判断当前提现开关是否开启
        if(drawFlag != 1) {
            throw ServiceException.build(RetEnum.RET_SERVICE_DRAW_CLOSE);
        }
        // 2. 判断单笔金额是否允许
        if(minDrawAmount != null && minDrawAmount >= 0 &&  settAmount < minDrawAmount) {
            throw ServiceException.build(RetEnum.RET_SERVICE_DRAW_AMOUNT_MIN_LIMIT);
        }
        if(maxDrawAmount != null && maxDrawAmount >= 0 && settAmount > maxDrawAmount) {
            throw ServiceException.build(RetEnum.RET_SERVICE_DRAW_AMOUNT_MAX_LIMIT);
        }
        // 3. 判断周几可以提现,1,2,3格式存储,7表示周日
        if(StringUtils.isNotBlank(allowDrawWeekDay)) {
            boolean allowDrawWeekFlag = false;
            String currentWeek = String.valueOf(DateUtil.getCurrentWeek());
            String[] weeks = allowDrawWeekDay.split(",");
            for(String week : weeks) {
                if(week.equals(currentWeek)) {
                    allowDrawWeekFlag = true;
                    break;
                }
            }
            // 当天不允许提现
            if(!allowDrawWeekFlag) {
                throw ServiceException.build(RetEnum.RET_SERVICE_DRAW_DAY_NOT);
            }
        }
        // 4. 提现时间是否满足
        String ymd = DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD);
        drawDayStartTime = ymd + " " + drawDayStartTime;
        drawDayEndTime = ymd + " " + drawDayEndTime;
        long startTime = DateUtil.str2date(drawDayStartTime).getTime();
        long endTime = DateUtil.str2date(drawDayEndTime).getTime();
        long currentTime = System.currentTimeMillis();
        if(currentTime < startTime || currentTime > endTime) {
            throw ServiceException.build(RetEnum.RET_SERVICE_DRAW_TIME_LIMIT);
        }
        // 5. 是否超过每日提现次数
        if(dayDrawTimes != null && dayDrawTimes >= 0) {
            // 查询当天提现次数
            SettRecordExample example = new SettRecordExample();
            SettRecordExample.Criteria criteria = example.createCriteria();
            criteria.andInfoIdEqualTo(infoId);
            criteria.andSettDateEqualTo(new Date());
            int infoDrawTimes = settRecordMapper.countByExample(example);
            if(infoDrawTimes + 1 > dayDrawTimes) {
                throw ServiceException.build(RetEnum.RET_SERVICE_DRAW_TIMES_LIMIT);
            }
        }
        // 6. 判断是否超过当日可提现总额
        if(drawMaxDayAmount != null && drawMaxDayAmount > 0) {
            // 查询当天提现金额
            SettRecordExample example = new SettRecordExample();
            SettRecordExample.Criteria criteria = example.createCriteria();
            criteria.andInfoIdEqualTo(infoId);
            List<Byte> statusList = new LinkedList<>();
            statusList.add(MchConstant.SETT_STATUS_AUDIT_ING);      // 等待审核
            statusList.add(MchConstant.SETT_STATUS_AUDIT_OK);       // 已审核
            statusList.add(MchConstant.SETT_STATUS_REMIT_ING);      // 打款中
            statusList.add(MchConstant.SETT_STATUS_REMIT_SUCCESS);  // 打款成功
            criteria.andSettStatusIn(statusList);
            criteria.andSettDateEqualTo(new Date());
            long sumSettAmount = settRecordMapper.sumSettAmountByExample(example);
            if(sumSettAmount + settAmount > drawMaxDayAmount) {
                throw ServiceException.build(RetEnum.RET_SERVICE_DRAW_DAY_AMOUNT_MAX_LIMIT);
            }
        }
    }

    public static void main(String[] args) throws Exception {

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;

        System.out.println(w);


        String datetime = "2018-08-05";
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
        Date datet = f.parse(datetime);
        cal.setTime(datet);
        w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        System.out.println(w);
    }
}
