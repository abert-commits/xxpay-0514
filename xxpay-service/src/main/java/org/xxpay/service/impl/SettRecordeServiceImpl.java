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
        // ????????????????????????
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
        // ???????????????,??????????????????(????????????+?????????)
        if(updateCount == 1 && status == MchConstant.SETT_STATUS_AUDIT_NOT) {
            if(settRecord.getInfoType() == MchConstant.SETT_INFO_TYPE_AGENT) { // ?????????
                agentAccountService.unFreezeSettAmount(settRecord.getInfoId(), settRecord.getSettAmount() + settRecord.getSettFee());
            }else if(settRecord.getInfoType() == MchConstant.SETT_INFO_TYPE_MCH) {   // ??????
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
        // ???????????????????????????|?????????
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
            if(settRecord.getInfoType() == MchConstant.SETT_INFO_TYPE_AGENT) { // ?????????
                if(status == MchConstant.SETT_STATUS_REMIT_FAIL) {
                    // ????????????,????????????(????????????+?????????)
                    agentAccountService.unFreezeSettAmount(settRecord.getInfoId(), settRecord.getSettAmount() + settRecord.getSettFee());
                }else if(status == MchConstant.SETT_STATUS_REMIT_SUCCESS) {
                    // ????????????,??????+??????(??????=????????????+?????????)
                    agentAccountService.unFreezeAmount(settRecord.getInfoId(), settRecord.getSettAmount() + settRecord.getSettFee(), settRecord.getSettOrderId(), MchConstant.BIZ_TYPE_REMIT);
                }
            }else if(settRecord.getInfoType() == MchConstant.SETT_INFO_TYPE_MCH) {   // ??????
                if(status == MchConstant.SETT_STATUS_REMIT_FAIL) {
                    // ????????????,????????????(????????????+?????????)
                    mchAccountService.unFreezeSettAmount(settRecord.getInfoId(), settRecord.getSettAmount() + settRecord.getSettFee());
                }else if(status == MchConstant.SETT_STATUS_REMIT_SUCCESS) {
                    // ????????????,??????+??????(??????=????????????+?????????)
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
     * ?????????????????????
     * @param settAmount ????????????
     * @param feeType    ????????????   1-???????????????,2-????????????
     * @param feeRate    ????????????
     * @param feeLevel   ???????????????
     * @param drawFeeLimit  ???????????????
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
     * ???????????????????????????
     * @param infoId
     * @param settAmount
     * @return
     */
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    int applySett4Agent(Long infoId, Long settAmount, String bankName, String bankNetName, String accountName, String accountNo) {
        // ??????????????????
        AgentInfo info = agentInfoService.findByAgentId(infoId);
        if(info == null) {
            // ???????????????
            throw ServiceException.build(RetEnum.RET_SERVICE_AGENT_NOT_EXIST);
        }
        if(info.getStatus() != MchConstant.PUB_YES) {
            // ????????????
            throw ServiceException.build(RetEnum.RET_AGENT_STATUS_STOP);
        }

        // ??????????????????
        AgentAccount account = agentAccountService.findByAgentId(infoId);
        if(account == null) {
            // ???????????????
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_NOT_EXIST);
        }
        if(account.getStatus() != MchConstant.PUB_YES) {
            // ????????????
            throw ServiceException.build(RetEnum.RET_AGENT_STATUS_STOP);
        }

        // ??????????????????????????????
        info = agentInfoService.reBuildAgentInfoSettConfig(info);

        // ???????????????????????????
        long settFee = calculationFee(settAmount, info.getFeeType(), info.getFeeRate(), info.getFeeLevel(), info.getDrawFeeLimit());    // ?????????
        Long debitAmount = settAmount + settFee;                                                                // ????????????????????? = ???????????? + ?????????
        Long availableAmount = account.getAvailableSettAmount();
        if(debitAmount.compareTo(availableAmount) > 0) {
            // ????????????
            throw ServiceException.build(RetEnum.RET_SERVICE_SETT_AMOUNT_NOT_SETT);
        }

        // ????????????????????????
        isAllowDraw(infoId, settAmount, info.getDrawFlag(), info.getAllowDrawWeekDay(), info.getDrawDayStartTime(), info.getDrawDayEndTime(),
                info.getDayDrawTimes(), info.getDrawMaxDayAmount(), info.getMaxDrawAmount(), info.getMinDrawAmount());

        // ??????????????????
        agentAccountService.freezeAmount(infoId, debitAmount);

        SettRecord settRecord = new SettRecord();
        settRecord.setSettOrderId(MySeq.getSett());
        settRecord.setInfoId(infoId);
        settRecord.setInfoType(MchConstant.SETT_INFO_TYPE_AGENT);   // ?????????
        settRecord.setSettType((byte) 1);       // ????????????
        settRecord.setSettDate(new Date());     // ????????????
        settRecord.setSettAmount(settAmount);   // ??????????????????
        settRecord.setSettFee(settFee);             // ???????????????
        settRecord.setRemitAmount(settAmount);      // ???????????? = ????????????
        if(StringUtils.isNotBlank(bankName) && StringUtils.isNotBlank(bankNetName) && StringUtils.isNotBlank(accountName) && StringUtils.isNotBlank(accountNo)) {
            settRecord.setAccountAttr((byte) 0);    // ??????
            settRecord.setAccountType((byte) 1);    // ???????????????
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
        settRecord.setSettStatus(MchConstant.SETT_STATUS_AUDIT_ING);    // ????????????
        settRecord.setRemark("");
        settRecord.setRemitRemark("");
        int insertCount = settRecordMapper.insertSelective(settRecord);

        return insertCount;
    }

    /**
     * ????????????????????????
     * @param infoId
     * @param settAmount
     * @return
     */
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    int applySett4Mch(Long infoId, Long settAmount, String bankName, String bankNetName, String accountName, String accountNo) {
        // ??????????????????
        MchInfo info = mchInfoService.findByMchId(infoId);
        if(info == null) {
            // ???????????????
            throw ServiceException.build(RetEnum.RET_SERVICE_MCH_NOT_EXIST);
        }
        if(info.getStatus() != MchConstant.PUB_YES) {
            // ??????????????????
            throw ServiceException.build(RetEnum.RET_MCH_STATUS_STOP);
        }

        // ??????????????????
        MchAccount account = mchAccountService.findByMchId(infoId);
        if(account == null) {
            // ???????????????
            throw ServiceException.build(RetEnum.RET_SERVICE_ACCOUNT_NOT_EXIST);
        }
        if(account.getStatus() != MchConstant.PUB_YES) {
            // ????????????
            throw ServiceException.build(RetEnum.RET_MCH_STATUS_STOP);
        }

        // ??????????????????????????????
        info = mchInfoService.reBuildMchInfoSettConfig(info);

        // ???????????????????????????
        long settFee = calculationFee(settAmount, info.getFeeType(), info.getFeeRate(), info.getFeeLevel(), info.getDrawFeeLimit());    // ?????????
        Long debitAmount = settAmount + settFee;                                                                // ??????????????????=???????????? + ?????????
        Long availableAmount = account.getAvailableSettAmount();
        if(debitAmount.compareTo(availableAmount) > 0) {
            // ????????????
            throw ServiceException.build(RetEnum.RET_SERVICE_SETT_AMOUNT_NOT_SETT);
        }

        // ????????????????????????
        isAllowDraw(infoId, settAmount, info.getDrawFlag(), info.getAllowDrawWeekDay(), info.getDrawDayStartTime(), info.getDrawDayEndTime(),
                info.getDayDrawTimes(), info.getDrawMaxDayAmount(), info.getMaxDrawAmount(), info.getMinDrawAmount());

        // ??????????????????
        mchAccountService.freezeAmount(infoId, debitAmount);

        SettRecord settRecord = new SettRecord();
        settRecord.setSettOrderId(MySeq.getSett());
        settRecord.setInfoId(infoId);
        settRecord.setInfoType(MchConstant.SETT_INFO_TYPE_MCH);   // ??????
        settRecord.setSettType((byte) 1);   // ????????????
        settRecord.setSettDate(new Date()); // ????????????
        settRecord.setSettAmount(settAmount);   // ??????????????????
        settRecord.setSettFee(settFee);             // ???????????????
        settRecord.setRemitAmount(settAmount);      // ???????????? = ????????????
        if(StringUtils.isNotBlank(bankName) && StringUtils.isNotBlank(bankNetName) && StringUtils.isNotBlank(accountName) && StringUtils.isNotBlank(accountNo)) {
            settRecord.setAccountAttr((byte) 0);    // ??????
            settRecord.setAccountType((byte) 1);    // ???????????????
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
        settRecord.setSettStatus(MchConstant.SETT_STATUS_AUDIT_ING);    // ????????????
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
        // ????????????????????????????????????
        if(drawFlag != 1) {
            return "??????????????????,?????????????????????";
        }
        // ????????????????????????,1,2,3????????????,7????????????
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
            // ?????????????????????
            if(!allowDrawWeekFlag) {
                return "?????????????????????,???????????????????????????:" + allowDrawWeekDay;
            }
        }
        // ????????????????????????
        String ymd = DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD);
        String drawDayStartTime = ymd + " " + drawStartTime;
        String drawDayEndTime = ymd + " " + drawEndTime;
        long startTime = DateUtil.str2date(drawDayStartTime).getTime();
        long endTime = DateUtil.str2date(drawDayEndTime).getTime();
        long currentTime = System.currentTimeMillis();
        if(currentTime < startTime || currentTime > endTime) {
            return "???????????????????????????,??????????????????:" + drawStartTime + " - " + drawEndTime;
        }
        return "";
    }

    void isAllowDraw(Long infoId, Long settAmount, Byte drawFlag, String allowDrawWeekDay, String drawDayStartTime, String drawDayEndTime,
                     Integer dayDrawTimes, Long drawMaxDayAmount, Long maxDrawAmount, Long minDrawAmount) {

        // 1. ????????????????????????????????????
        if(drawFlag != 1) {
            throw ServiceException.build(RetEnum.RET_SERVICE_DRAW_CLOSE);
        }
        // 2. ??????????????????????????????
        if(minDrawAmount != null && minDrawAmount >= 0 &&  settAmount < minDrawAmount) {
            throw ServiceException.build(RetEnum.RET_SERVICE_DRAW_AMOUNT_MIN_LIMIT);
        }
        if(maxDrawAmount != null && maxDrawAmount >= 0 && settAmount > maxDrawAmount) {
            throw ServiceException.build(RetEnum.RET_SERVICE_DRAW_AMOUNT_MAX_LIMIT);
        }
        // 3. ????????????????????????,1,2,3????????????,7????????????
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
            // ?????????????????????
            if(!allowDrawWeekFlag) {
                throw ServiceException.build(RetEnum.RET_SERVICE_DRAW_DAY_NOT);
            }
        }
        // 4. ????????????????????????
        String ymd = DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD);
        drawDayStartTime = ymd + " " + drawDayStartTime;
        drawDayEndTime = ymd + " " + drawDayEndTime;
        long startTime = DateUtil.str2date(drawDayStartTime).getTime();
        long endTime = DateUtil.str2date(drawDayEndTime).getTime();
        long currentTime = System.currentTimeMillis();
        if(currentTime < startTime || currentTime > endTime) {
            throw ServiceException.build(RetEnum.RET_SERVICE_DRAW_TIME_LIMIT);
        }
        // 5. ??????????????????????????????
        if(dayDrawTimes != null && dayDrawTimes >= 0) {
            // ????????????????????????
            SettRecordExample example = new SettRecordExample();
            SettRecordExample.Criteria criteria = example.createCriteria();
            criteria.andInfoIdEqualTo(infoId);
            criteria.andSettDateEqualTo(new Date());
            int infoDrawTimes = settRecordMapper.countByExample(example);
            if(infoDrawTimes + 1 > dayDrawTimes) {
                throw ServiceException.build(RetEnum.RET_SERVICE_DRAW_TIMES_LIMIT);
            }
        }
        // 6. ???????????????????????????????????????
        if(drawMaxDayAmount != null && drawMaxDayAmount > 0) {
            // ????????????????????????
            SettRecordExample example = new SettRecordExample();
            SettRecordExample.Criteria criteria = example.createCriteria();
            criteria.andInfoIdEqualTo(infoId);
            List<Byte> statusList = new LinkedList<>();
            statusList.add(MchConstant.SETT_STATUS_AUDIT_ING);      // ????????????
            statusList.add(MchConstant.SETT_STATUS_AUDIT_OK);       // ?????????
            statusList.add(MchConstant.SETT_STATUS_REMIT_ING);      // ?????????
            statusList.add(MchConstant.SETT_STATUS_REMIT_SUCCESS);  // ????????????
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
