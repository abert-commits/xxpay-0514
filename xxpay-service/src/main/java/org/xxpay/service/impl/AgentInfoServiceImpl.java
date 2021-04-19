package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xxpay.core.common.util.StrUtil;
import org.xxpay.core.entity.*;
import org.xxpay.core.service.IAgentInfoService;
import org.xxpay.core.service.ISysConfigService;
import org.xxpay.service.dao.mapper.AgentAccountMapper;
import org.xxpay.service.dao.mapper.AgentInfoMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 2018/4/27
 * @description:
 */
@Service(interfaceName = "org.xxpay.core.service.IAgentInfoService", version = "1.0.0", retries = -1)
public class AgentInfoServiceImpl implements IAgentInfoService {

    @Autowired
    private AgentInfoMapper agentInfoMapper;

    @Autowired
    private AgentAccountMapper agentAccountMapper;

    @Autowired
    private ISysConfigService sysConfigService;

    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    @Override
    public int add(AgentInfo agentInfo) {
        // 插入代理商基本信息
        agentInfoMapper.insertSelective(agentInfo);
        // 插入代理商账户信息
        AgentAccount agentAccount = new AgentAccount();
        agentAccount.setAgentId(agentInfo.getAgentId());
        agentAccount.setBalance(BigDecimal.ZERO.longValue());
        agentAccount.setSecurityMoney(BigDecimal.ZERO.longValue());
        agentAccount.setSettAmount(BigDecimal.ZERO.longValue());
        agentAccount.setUnBalance(BigDecimal.ZERO.longValue());
        agentAccount.setTodayExpend(BigDecimal.ZERO.longValue());
        agentAccount.setTodayIncome(BigDecimal.ZERO.longValue());
        agentAccount.setTotalExpend(BigDecimal.ZERO.longValue());
        agentAccount.setTotalIncome(BigDecimal.ZERO.longValue());
        return agentAccountMapper.insertSelective(agentAccount);
    }

    @Override
    public int update(AgentInfo agentInfo) {
        return agentInfoMapper.updateByPrimaryKeySelective(agentInfo);
    }

    @Override
    public AgentInfo find(AgentInfo agentInfo) {
        AgentInfoExample example = new AgentInfoExample();
        AgentInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentInfo);
        List<AgentInfo> agentInfoList = agentInfoMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(agentInfoList)) return null;
        return agentInfoList.get(0);
    }

    @Override
    public AgentInfo findByLoginName(String loginName) {
        if(StringUtils.isBlank(loginName)) return null;
        AgentInfo agentInfo;
        if(StrUtil.checkEmail(loginName)) {
            agentInfo = findByEmail(loginName);
            if(agentInfo != null) return agentInfo;
        }
        if(StrUtil.checkMobileNumber(loginName)) {
            agentInfo = findByMobile(Long.parseLong(loginName));
            if(agentInfo != null) return agentInfo;
        }
        if(NumberUtils.isDigits(loginName)) {
            agentInfo = findByAgentId(Long.parseLong(loginName));
            if(agentInfo != null) return agentInfo;
        }
        agentInfo = findByUserName(loginName);
        return agentInfo;
    }

    @Override
    public AgentInfo findByAgentId(Long agentId) {
        return agentInfoMapper.selectByPrimaryKey(agentId);
    }

    @Override
    public AgentInfo findByUserName(String userName) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setUserName(userName);
        return find(agentInfo);
    }

    @Override
    public AgentInfo findByMobile(Long mobile) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setMobile(mobile);
        return find(agentInfo);
    }

    @Override
    public AgentInfo findByEmail(String email) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setEmail(email);
        return find(agentInfo);
    }

    @Override
    public AgentInfo findByParentAgentId(Long parentAgentId) {
        AgentInfo agentInfo = new AgentInfo();
        agentInfo.setParentAgentId(parentAgentId);
        return find(agentInfo);
    }

    @Override
    public List<AgentInfo> select(int offset, int limit, AgentInfo agentInfo) {
        AgentInfoExample example = new AgentInfoExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        AgentInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentInfo);
        return agentInfoMapper.selectByExample(example);
    }

    @Override
    public Integer count(AgentInfo agentInfo) {
        AgentInfoExample example = new AgentInfoExample();
        AgentInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, agentInfo);
        return agentInfoMapper.countByExample(example);
    }

    @Override
    public Map count4Agent() {
        Map param = new HashMap<>();
        return agentInfoMapper.count4Agent(param);
    }

    @Override
    public AgentInfo reBuildAgentInfoSettConfig(AgentInfo info) {
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

    @Override
    public int getRiskDay(Long agentId) {
        AgentInfo agentInfo = findByAgentId(agentId);
        if(agentInfo == null) return 1;
        agentInfo = reBuildAgentInfoSettConfig(agentInfo);
        if(agentInfo.getSettMode() == 1) {
            return 0;
        }
        return 1;
    }

    @Override
    public List<AgentInfo> selectAll(AgentInfo agentInfo) {
        AgentInfoExample example = new AgentInfoExample();
        example.setOrderByClause("agentLevel ASC");
        AgentInfoExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria,agentInfo);
        return agentInfoMapper.selectByExample(example);
    }

    /**
     * 查询总代理下的二级代理列表
     */
    @Override
    public List<Map> selectInfoAndAccount(int page, int pageSize, AgentInfo agentInfo) {
        Map param = new HashMap();
        param.put("offset", page);
        param.put("limit", pageSize);
        if (agentInfo.getParentAgentId() !=null && agentInfo.getParentAgentId() != 0) param.put("parentAgentId", agentInfo.getParentAgentId());
        return agentInfoMapper.selectInfoAndAccount(param);
    }

    void setCriteria(AgentInfoExample.Criteria criteria, AgentInfo agentInfo) {
        if(agentInfo != null) {
            if(agentInfo.getAgentId() != null) criteria.andAgentIdEqualTo(agentInfo.getAgentId());
            if(agentInfo.getParentAgentId() != null) criteria.andParentAgentIdEqualTo(agentInfo.getParentAgentId());
            if(agentInfo.getEmail() != null) criteria.andEmailEqualTo(agentInfo.getEmail());
            if(agentInfo.getMobile() != null) criteria.andMobileEqualTo(agentInfo.getMobile());
            if(agentInfo.getUserName() != null) criteria.andUserNameEqualTo(agentInfo.getUserName());
            if(agentInfo.getStatus() != null && agentInfo.getStatus().byteValue() != -99) criteria.andStatusEqualTo(agentInfo.getStatus());
            if(agentInfo.getAgentLevel() != null) criteria.andAgentLevelEqualTo(agentInfo.getAgentLevel());
        }
    }
}
