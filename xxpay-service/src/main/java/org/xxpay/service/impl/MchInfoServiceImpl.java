package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.util.StrUtil;
import org.xxpay.core.entity.MchAccount;
import org.xxpay.core.entity.MchInfo;
import org.xxpay.core.entity.MchInfoExample;
import org.xxpay.core.entity.PayDataStatistics;
import org.xxpay.core.service.IMchInfoService;
import org.xxpay.core.service.ISysConfigService;
import org.xxpay.service.dao.mapper.MchAccountMapper;
import org.xxpay.service.dao.mapper.MchInfoMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/9/8
 * @description:
 */
@Service(interfaceName = "org.xxpay.core.service.IMchInfoService", version = "1.0.0", retries = -1)
public class MchInfoServiceImpl implements IMchInfoService {

    @Autowired
    private MchInfoMapper mchInfoMapper;

    @Autowired
    private MchAccountMapper mchAccountMapper;

    @Autowired
    private ISysConfigService sysConfigService;

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int add(MchInfo mchInfo) {
        // 插入商户基本信息
        int result = mchInfoMapper.insertSelective(mchInfo);
        if(result != 1) return 0;
        // 插入商户账户信息
        MchAccount mchAccount = new MchAccount();
        mchAccount.setMchId(mchInfo.getMchId());
        mchAccount.setName(mchInfo.getName());
        mchAccount.setType(MchConstant.MCH_TYPE_PLATFORM);  // 目前只支持平台账户
        mchAccount.setBalance(BigDecimal.ZERO.longValue());
        mchAccount.setSecurityMoney(BigDecimal.ZERO.longValue());
        mchAccount.setSettAmount(BigDecimal.ZERO.longValue());
        mchAccount.setUnBalance(BigDecimal.ZERO.longValue());
        mchAccount.setTodayExpend(BigDecimal.ZERO.longValue());
        mchAccount.setTodayIncome(BigDecimal.ZERO.longValue());
        mchAccount.setTotalExpend(BigDecimal.ZERO.longValue());
        mchAccount.setTotalIncome(BigDecimal.ZERO.longValue());
        int count = mchAccountMapper.insertSelective(mchAccount);
        return count;
    }

    @Override
    public int register(MchInfo record) {
        return mchInfoMapper.insertSelective(record);
    }

    @Override
    public int update(MchInfo record) {
        return mchInfoMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public MchInfo find(MchInfo mchInfo) {
        MchInfoExample example = new MchInfoExample();
        MchInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchInfo);
        List<MchInfo> mchInfoList = mchInfoMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(mchInfoList)) return null;
        return mchInfoList.get(0);
    }

    @Override
    public MchInfo findByMchId(Long mchId) {
        return mchInfoMapper.selectByPrimaryKey(mchId);
    }

    @Override
    public MchInfo findByLoginName(String loginName) {
        if(StringUtils.isBlank(loginName)) return null;
        MchInfo mchInfo;
        if(StrUtil.checkEmail(loginName)) {
            mchInfo = findByEmail(loginName);
            if(mchInfo != null) return mchInfo;
        }
        if(StrUtil.checkMobileNumber(loginName)) {
            mchInfo = findByMobile(Long.parseLong(loginName));
            if(mchInfo != null) return mchInfo;
        }
        if(NumberUtils.isDigits(loginName)) {
            mchInfo = findByMchId(Long.parseLong(loginName));
            if(mchInfo != null) return mchInfo;
        }
        mchInfo = findByUserName(loginName);
        return mchInfo;
    }

    @Override
    public MchInfo findByMobile(Long mobile) {
        MchInfoExample example = new MchInfoExample();
        MchInfoExample.Criteria criteria = example.createCriteria();
        criteria.andMobileEqualTo(mobile);
        List<MchInfo> mchInfoList = mchInfoMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(mchInfoList)) return null;
        return mchInfoList.get(0);
    }

    @Override
    public MchInfo findByEmail(String email) {
        MchInfoExample example = new MchInfoExample();
        MchInfoExample.Criteria criteria = example.createCriteria();
        criteria.andEmailEqualTo(email);
        List<MchInfo> mchInfoList = mchInfoMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(mchInfoList)) return null;
        return mchInfoList.get(0);
    }

    @Override
    public MchInfo findByTag(String tag) {
        MchInfoExample example = new MchInfoExample();
        MchInfoExample.Criteria criteria = example.createCriteria();
        criteria.andTagEqualTo(tag);
        List<MchInfo> mchInfoList = mchInfoMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(mchInfoList)) return null;
        return mchInfoList.get(0);
    }

    @Override
    public MchInfo findByUserName(String userName) {
        MchInfoExample example = new MchInfoExample();
        MchInfoExample.Criteria criteria = example.createCriteria();
        criteria.andUserNameEqualTo(userName);
        List<MchInfo> mchInfoList = mchInfoMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(mchInfoList)) return null;
        return mchInfoList.get(0);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int auditOk(Long mchInd) {
        // 修改商户状态
        MchInfo updateMchInfo = new MchInfo();
        updateMchInfo.setStatus(MchConstant.STATUS_OK);
        updateMchInfo.setRole(MchConstant.MCH_ROLE_NORMAL);
        updateMchInfo.setPrivateKey(RandomStringUtils.randomAlphanumeric(128).toUpperCase());   // 设置商户私钥
        MchInfoExample example = new MchInfoExample();
        MchInfoExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchInd);
        List<Byte> status = new LinkedList<>();
        status.add(MchConstant.STATUS_AUDIT_ING);
        status.add(MchConstant.STATUS_AUDIT_NOT);
        criteria.andStatusIn(status);
        int count = mchInfoMapper.updateByExampleSelective(updateMchInfo, example);
        if(count != 1) return 0;
        MchInfo mchInfo = findByMchId(mchInd);
        // 插入商户账户信息
        MchAccount mchAccount = new MchAccount();
        mchAccount.setMchId(mchInfo.getMchId());
        mchAccount.setName(mchInfo.getName());
        mchAccount.setType(mchInfo.getType());
        mchAccount.setBalance(BigDecimal.ZERO.longValue());
        mchAccount.setSecurityMoney(BigDecimal.ZERO.longValue());
        mchAccount.setSettAmount(BigDecimal.ZERO.longValue());
        mchAccount.setUnBalance(BigDecimal.ZERO.longValue());
        mchAccount.setTodayExpend(BigDecimal.ZERO.longValue());
        mchAccount.setTodayIncome(BigDecimal.ZERO.longValue());
        mchAccount.setTotalExpend(BigDecimal.ZERO.longValue());
        mchAccount.setTotalIncome(BigDecimal.ZERO.longValue());
        count = mchAccountMapper.insertSelective(mchAccount);
        return count;
    }

    @Override
    public int auditNot(Long mchInd) {
        MchInfo mchInfo = new MchInfo();
        mchInfo.setStatus(MchConstant.STATUS_AUDIT_NOT);
        MchInfoExample example = new MchInfoExample();
        MchInfoExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchInd);
        criteria.andStatusEqualTo(MchConstant.STATUS_AUDIT_ING);
        int count = mchInfoMapper.updateByExampleSelective(mchInfo, example);
        return count;
    }

    @Override
    public List<MchInfo> select(int offset, int limit, MchInfo mchInfo) {
        MchInfoExample example = new MchInfoExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        MchInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchInfo);
        return mchInfoMapper.selectByExample(example);
    }

    @Override
    public List<MchInfo> select4Audit(int offset, int limit, MchInfo mchInfo) {
        MchInfoExample example = new MchInfoExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        MchInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchInfo);
        List<Byte> status = new LinkedList<>();
        status.add(MchConstant.STATUS_AUDIT_NOT);
        status.add(MchConstant.STATUS_AUDIT_ING);
        criteria.andStatusIn(status);
        return mchInfoMapper.selectByExample(example);
    }

    @Override
    public List<MchInfo> select4Normal(int offset, int limit, MchInfo mchInfo) {
        MchInfoExample example = new MchInfoExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        MchInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchInfo);
        List<Byte> status = new LinkedList<>();
        status.add(MchConstant.STATUS_STOP);
        status.add(MchConstant.STATUS_OK);
        criteria.andStatusIn(status);
        return mchInfoMapper.selectByExample(example);
    }

    @Override
    public Integer count(MchInfo mchInfo) {
        MchInfoExample example = new MchInfoExample();
        MchInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchInfo);
        return mchInfoMapper.countByExample(example);
    }

    @Override
    public Integer countsDataStatisies(PayDataStatistics payDataStatistics) {
        return mchInfoMapper.countsDataStatisies(payDataStatistics);
    }

    @Override
    public Integer merchantTopup(PayDataStatistics payDataStatistics) {
        return mchInfoMapper.merchantTopup(payDataStatistics);
    }

    @Override
    public Integer count4Audit(MchInfo mchInfo) {
        MchInfoExample example = new MchInfoExample();
        MchInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchInfo);
        List<Byte> status = new LinkedList<>();
        status.add(MchConstant.STATUS_AUDIT_NOT);
        status.add(MchConstant.STATUS_AUDIT_ING);
        criteria.andStatusIn(status);
        return mchInfoMapper.countByExample(example);
    }

    @Override
    public Integer count4Normal(MchInfo mchInfo) {
        MchInfoExample example = new MchInfoExample();
        MchInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, mchInfo);
        List<Byte> status = new LinkedList<>();
        status.add(MchConstant.STATUS_STOP);
        status.add(MchConstant.STATUS_OK);
        criteria.andStatusIn(status);
        return mchInfoMapper.countByExample(example);
    }

    @Override
    public Map count4Mch() {
        Map param = new HashMap<>();
        return mchInfoMapper.count4Mch(param);
    }

    @Override
    public MchInfo reBuildMchInfoSettConfig(MchInfo info) {
        // 继承系统,按系统配置设置结算属性
        if(info.getSettConfigMode() == 1) {
            JSONObject settObj = sysConfigService.getSysConfigObj("sett");
            info.setDrawFlag(settObj.getByte("drawFlag"));
            info.setAllowDrawWeekDay(settObj.getString("allowDrawWeekDay"));
            info.setDrawDayStartTime(settObj.getString("drawDayStartTime"));
            info.setDrawDayEndTime(settObj.getString("drawDayEndTime"));
            info.setDayDrawTimes(settObj.getInteger("dayDrawTimes"));
            info.setDrawMaxDayAmount(settObj.getLong("drawMaxDayAmount"));
            info.setMaxDrawAmount(settObj.getLong("maxDrawAmount"));
            info.setMinDrawAmount(settObj.getLong("minDrawAmount"));
            info.setFeeType(settObj.getByte("feeType"));
            info.setFeeRate(settObj.getBigDecimal("feeRate"));
            info.setFeeLevel(settObj.getString("feeLevel"));
            info.setDrawFeeLimit(settObj.getLong("drawFeeLimit"));
            info.setSettMode(settObj.getByte("settMode"));
        }
        return info;
    }

    void setCriteria(MchInfoExample.Criteria criteria, MchInfo mchInfo) {
        if(mchInfo != null) {
            if(mchInfo.getGroupId() != null && mchInfo.getGroupId().byteValue() != -99) criteria.andGroupIdEqualTo(mchInfo.getGroupId());
            if(mchInfo.getMchId() != null) criteria.andMchIdEqualTo(mchInfo.getMchId());
            if(mchInfo.getType() != null && mchInfo.getType().byteValue() != -99) criteria.andTypeEqualTo(mchInfo.getType());
            if(mchInfo.getEmail() != null) criteria.andEmailEqualTo(mchInfo.getEmail());
            if(mchInfo.getMobile() != null) criteria.andMobileEqualTo(mchInfo.getMobile());
            if(mchInfo.getAgentId() != null) criteria.andAgentIdEqualTo(mchInfo.getAgentId());
        }
    }

}
