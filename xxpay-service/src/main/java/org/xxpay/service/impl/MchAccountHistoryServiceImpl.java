package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.MchAccountHistory;
import org.xxpay.core.entity.MchAccountHistoryExample;
import org.xxpay.core.service.IAgentAccountService;
import org.xxpay.core.service.IAgentInfoService;
import org.xxpay.core.service.IMchAccountHistoryService;
import org.xxpay.service.dao.mapper.MchAccountHistoryMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/12/4
 * @description:
 */
@Service(interfaceName = "org.xxpay.core.service.IMchAccountHistoryService", version = "1.0.0", retries = -1)
public class MchAccountHistoryServiceImpl implements IMchAccountHistoryService {

    @Autowired
    private MchAccountHistoryMapper mchAccountHistoryMapper;

    @Autowired
    private IAgentAccountService agentAccountService;

    @Autowired
    private IAgentInfoService agentInfoService;

    public static final MyLog _log = MyLog.getLog(MchAccountHistoryServiceImpl.class);

    @Override
    public Map selectSettDailyCollect4Mch(Long mchId, String collDate, byte fundDirection, int riskDay) {
        Map param = new HashMap<>();
        param.put("mchId", mchId);
        param.put("collDate", collDate);
        param.put("fundDirection", fundDirection);
        param.put("riskDay", riskDay);
        return mchAccountHistoryMapper.selectSettDailyCollect4Mch(param);
    }

    @Override
    public void updateCompleteSett4Mch(Long mchId, String collDate, int riskDay) {
        Map param = new HashMap<>();
        param.put("mchId", mchId);
        param.put("collDate", collDate);
        param.put("riskDay", riskDay);
        mchAccountHistoryMapper.updateCompleteSett4Mch(param);
    }

    @Override
    public Map selectSettDailyCollect4Agent(Long agentId, String collDate, byte fundDirection, int riskDay, Byte bizType, String bizItem) {
        Map param = new HashMap<>();
        param.put("agentId", agentId);
        param.put("collDate", collDate);
        param.put("fundDirection", fundDirection);
        param.put("riskDay", riskDay);
        param.put("bizType", bizType);
        param.put("bizItem", bizItem);
        return mchAccountHistoryMapper.selectSettDailyCollect4Agent(param);
    }

    @Override
    public void updateCompleteSett4Agent(Long agentId, String collDate, int riskDay, Byte bizType, String bizItem) {
        Map param = new HashMap<>();
        param.put("agentId", agentId);
        param.put("collDate", collDate);
        param.put("riskDay", riskDay);
        param.put("bizType", bizType);
        param.put("bizItem", bizItem);
        mchAccountHistoryMapper.updateCompleteSett4Agent(param);
    }

    @Override
    public List<MchAccountHistory> selectNotSettCollect4Agent2(Long agentId, String collDate) {
        Map param = new HashMap<>();
        param.put("agentId", agentId);
        param.put("collDate", collDate);
        return mchAccountHistoryMapper.selectNotSettCollect4Agent2(param);
    }

    @Override
    public List<MchAccountHistory> selectNotSettCollect4Agent1(Long parentAgentId, String collDate) {
        Map param = new HashMap<>();
        param.put("parentAgentId", parentAgentId);
        param.put("collDate", collDate);
        return mchAccountHistoryMapper.selectNotSettCollect4Agent1(param);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void updateCompleteSett4Agent1(MchAccountHistory mchAccountHistory) {
        MchAccountHistoryExample example = new MchAccountHistoryExample();
        MchAccountHistoryExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(mchAccountHistory.getId());
        criteria.andParentAgentSettStatusEqualTo(MchConstant.PUB_NO);
        criteria.andIsAllowSettEqualTo(MchConstant.PUB_YES);
        MchAccountHistory updateMchAccountHistory = new MchAccountHistory();
        updateMchAccountHistory.setParentAgentSettStatus(MchConstant.PUB_YES);    // 设置代理商结算完成
        int updateResult = mchAccountHistoryMapper.updateByExampleSelective(updateMchAccountHistory, example);
        if(updateResult != 1) {
            _log.warn("[更新代理商结算状态异常,id={},updateResult={}]", mchAccountHistory.getId(), updateResult);
            return;
        }
        if(mchAccountHistory.getParentAgentProfit() > 0) {
            // 更新一级代理商余额和可结算金额
            agentAccountService.creditToAccount(mchAccountHistory.getParentAgentId(),
                    mchAccountHistory.getParentAgentProfit(), MchConstant.AGENT_BIZ_TYPE_PROFIT, mchAccountHistory.getBizItem(), mchAccountHistory.getOrderId());
        }
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public void updateCompleteSett4Agent2(MchAccountHistory mchAccountHistory) {
        MchAccountHistoryExample example = new MchAccountHistoryExample();
        MchAccountHistoryExample.Criteria criteria = example.createCriteria();
        criteria.andIdEqualTo(mchAccountHistory.getId());
        criteria.andAgentSettStatusEqualTo(MchConstant.PUB_NO);
        criteria.andIsAllowSettEqualTo(MchConstant.PUB_YES);
        MchAccountHistory updateMchAccountHistory = new MchAccountHistory();
        updateMchAccountHistory.setAgentSettStatus(MchConstant.PUB_YES);    // 设置代理商结算完成
        int updateResult = mchAccountHistoryMapper.updateByExampleSelective(updateMchAccountHistory, example);
        if(updateResult != 1) {
            _log.warn("[更新代理商结算状态异常,id={},updateResult={}]", mchAccountHistory.getId(), updateResult);
            return;
        }
        if(mchAccountHistory.getAgentProfit() > 0) {
            // 更新二级代理商余额和可结算金额
            agentAccountService.creditToAccount(mchAccountHistory.getAgentId(),
                    mchAccountHistory.getAgentProfit(), MchConstant.AGENT_BIZ_TYPE_PROFIT, mchAccountHistory.getBizItem(), mchAccountHistory.getOrderId());
        }
    }

    @Override
    public List<MchAccountHistory> select(Long mchId, int offset, int limit, MchAccountHistory mchAccountHistory, JSONObject queryObj) {
        MchAccountHistoryExample example = new MchAccountHistoryExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        MchAccountHistoryExample.Criteria criteria = example.createCriteria();
        if(mchId != null) criteria.andMchIdEqualTo(mchId);
        setCriteria(criteria, mchAccountHistory, queryObj);
        return mchAccountHistoryMapper.selectByExample(example);
    }

    @Override
    public int count(Long mchId, MchAccountHistory mchAccountHistory, JSONObject queryObj) {
        MchAccountHistoryExample example = new MchAccountHistoryExample();
        MchAccountHistoryExample.Criteria criteria = example.createCriteria();
        if(mchId != null) criteria.andMchIdEqualTo(mchId);
        setCriteria(criteria, mchAccountHistory, queryObj);
        return mchAccountHistoryMapper.countByExample(example);
    }

    @Override
    public List<MchAccountHistory> select(int offset, int limit, MchAccountHistory mchAccountHistory, JSONObject queryObj) {
        return select(null, offset, limit, mchAccountHistory, queryObj);
    }

    @Override
    public int count(MchAccountHistory mchAccountHistory, JSONObject queryObj) {
        return count(null, mchAccountHistory, queryObj);
    }

    @Override
    public MchAccountHistory findById(Long id) {
        return mchAccountHistoryMapper.selectByPrimaryKey(id);
    }

    @Override
    public MchAccountHistory findById(Long mchId, Long id) {
        MchAccountHistoryExample example = new MchAccountHistoryExample();
        MchAccountHistoryExample.Criteria criteria = example.createCriteria();
        if(mchId != null) criteria.andMchIdEqualTo(mchId);
        criteria.andIdEqualTo(id);
        List<MchAccountHistory> mchAccountHistoryList = mchAccountHistoryMapper.selectByExample(example);
        if(CollectionUtils.isNotEmpty(mchAccountHistoryList)) return mchAccountHistoryList.get(0);
        return null;
    }

    @Override
    public MchAccountHistory findByOrderId(String orderId) {
        MchAccountHistoryExample example = new MchAccountHistoryExample();
        MchAccountHistoryExample.Criteria criteria = example.createCriteria();
        criteria.andOrderIdEqualTo(orderId);
        List<MchAccountHistory> mchAccountHistoryList = mchAccountHistoryMapper.selectByExample(example);
        if(CollectionUtils.isNotEmpty(mchAccountHistoryList)) return mchAccountHistoryList.get(0);
        return null;
    }

    @Override
    public Map count4Data(Byte bizType) {
        Map param = new HashMap<>();
        if(bizType != null) param.put("bizType", bizType);
        return mchAccountHistoryMapper.count4Data(param);
    }

    @Override
	public Map count4Data2(Long mchId,Long agentId, String orderId, Byte bizType, String createTimeStart,
			String createTimeEnd) {
		Map param = new HashMap<>();
		if(mchId != null) param.put("mchId", mchId);
		if(agentId != null) param.put("agentId", agentId);
		if(StringUtils.isNotBlank(orderId)) param.put("orderId", orderId);
		if(bizType != null) param.put("bizType", bizType);
		if(StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
		if(StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
		return mchAccountHistoryMapper.count4Data2(param);
	}

    @Override
    public List<Map> count4AgentTop(Long agentId, String bizType, String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if(agentId != null) param.put("agentId", agentId);
        if(StringUtils.isNotBlank(bizType)) param.put("bizType", bizType);
        if(StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if(StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return mchAccountHistoryMapper.count4AgentProfitTop(param);
    }

    void setCriteria(MchAccountHistoryExample.Criteria criteria, MchAccountHistory mchAccountHistory) {
        setCriteria(criteria, mchAccountHistory, null);
    }

    void setCriteria(MchAccountHistoryExample.Criteria criteria, MchAccountHistory mchAccountHistory, JSONObject queryObj) {
        if(mchAccountHistory != null) {
            if(mchAccountHistory.getId() != null) criteria.andIdEqualTo(mchAccountHistory.getId());
            if(mchAccountHistory.getMchId() != null) criteria.andMchIdEqualTo(mchAccountHistory.getMchId());
            if(StringUtils.isNotBlank(mchAccountHistory.getOrderId())) criteria.andOrderIdEqualTo(mchAccountHistory.getOrderId());
            if(StringUtils.isNotBlank(mchAccountHistory.getRemark())) criteria.andRemarkEqualTo(mchAccountHistory.getRemark());
            if(mchAccountHistory.getFundDirection() != null && !"-99".equals(mchAccountHistory.getFundDirection())) criteria.andFundDirectionEqualTo(mchAccountHistory.getFundDirection());
            if(mchAccountHistory.getBizType() != null && !"-99".equals(mchAccountHistory.getBizType())) criteria.andBizTypeEqualTo(mchAccountHistory.getBizType());
        }
        if(queryObj != null) {
            if(queryObj.getDate("createTimeStart") != null) criteria.andCreateTimeGreaterThanOrEqualTo(queryObj.getDate("createTimeStart"));
            if(queryObj.getDate("createTimeEnd") != null) criteria.andCreateTimeLessThanOrEqualTo(queryObj.getDate("createTimeEnd"));
        }
    }

}
