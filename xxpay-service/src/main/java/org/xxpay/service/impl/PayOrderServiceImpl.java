package org.xxpay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.LockUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.*;
import org.xxpay.core.service.IAgentInfoService;
import org.xxpay.core.service.IMchAccountService;
import org.xxpay.core.service.IPayOrderService;
import org.xxpay.service.dao.mapper.PayOrderMapper;

import java.util.*;

/**
 * @author: dingzhiwei
 * @date: 17/9/8
 * @description:
 */
@Service(interfaceName = "org.xxpay.core.service.IPayOrderService", version = "1.0.0", retries = -1)
public class PayOrderServiceImpl implements IPayOrderService {

    @Autowired
    private PayOrderMapper payOrderMapper;

    @Autowired
    private IMchAccountService mchAccountService;

    @Autowired
    private IAgentInfoService agentInfoService;

    private static final MyLog _log = MyLog.getLog(PayOrderServiceImpl.class);

    @Override
    public int createPayOrder(PayOrder payOrder) {
        return payOrderMapper.insertSelective(payOrder);
    }

    @Override
    public PayOrder selectPayOrder(String payOrderId) {
        return payOrderMapper.selectByPrimaryKey(payOrderId);
    }

    @Override
    public PayOrder selectByMchIdAndPayOrderId(Long mchId, String payOrderId) {
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andPayOrderIdEqualTo(payOrderId);
        List<PayOrder> payOrderList = payOrderMapper.selectByExample(example);
        return org.springframework.util.CollectionUtils.isEmpty(payOrderList) ? null : payOrderList.get(0);
    }

    @Override
    public PayOrder selectByMchIdAndMchOrderNo(Long mchId, String mchOrderNo) {
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        criteria.andMchIdEqualTo(mchId);
        criteria.andMchOrderNoEqualTo(mchOrderNo);
        List<PayOrder> payOrderList = payOrderMapper.selectByExample(example);
        return org.springframework.util.CollectionUtils.isEmpty(payOrderList) ? null : payOrderList.get(0);
    }

    @Override
    public int updateStatus4Ing(String payOrderId, String channelOrderNo) {
        return updateStatus4Ing(payOrderId, channelOrderNo, null);
    }

    @Override
    public int updateStatus4Ing(String payOrderId, String channelOrderNo, String channelAttach) {
        PayOrder payOrder = new PayOrder();
        payOrder.setStatus(PayConstant.PAY_STATUS_PAYING);
        if (channelOrderNo != null) payOrder.setChannelOrderNo(channelOrderNo);
        if (channelAttach != null) payOrder.setChannelAttach(channelAttach);
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        criteria.andPayOrderIdEqualTo(payOrderId);
        criteria.andStatusEqualTo(PayConstant.PAY_STATUS_INIT);
        return payOrderMapper.updateByExampleSelective(payOrder, example);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int updateStatus4Success(String payOrderId) {
        return updateStatus4Success(payOrderId, null);
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int updateStatus4Success(String payOrderId, String channelOrderNo) {
        PayOrder payOrder = new PayOrder();
        payOrder.setPayOrderId(payOrderId);
        payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
        payOrder.setPaySuccTime(new Date());
        if (StringUtils.isNotBlank(channelOrderNo)) payOrder.setChannelOrderNo(channelOrderNo);
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        criteria.andPayOrderIdEqualTo(payOrderId);
        criteria.andStatusEqualTo(PayConstant.PAY_STATUS_PAYING);
        return payOrderMapper.updateByExampleSelective(payOrder, example);
//        return updateSuccess4Transactional(payOrder, example);//老代码，暂时去掉
    }

    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int updateStatus4Success(String payOrderId, String channelOrderNo, String channelAttach) {
        PayOrder payOrder = new PayOrder();
        payOrder.setPayOrderId(payOrderId);
        payOrder.setStatus(PayConstant.PAY_STATUS_SUCCESS);
        payOrder.setPaySuccTime(new Date());
        if (StringUtils.isNotBlank(channelOrderNo)) payOrder.setChannelOrderNo(channelOrderNo);
        if (StringUtils.isNotBlank(channelAttach)) payOrder.setChannelAttach(channelAttach);
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        criteria.andPayOrderIdEqualTo(payOrderId);
        criteria.andStatusEqualTo(PayConstant.PAY_STATUS_PAYING);
        return payOrderMapper.updateByExampleSelective(payOrder, example);
//        return updateSuccess4Transactional(payOrder, example); 老代码，暂时去掉
    }


    @Override
    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    public int updateOrderSuccess(String payOrderId, String channelOrderNo) {
        PayOrder payOrder = new PayOrder();
        payOrder.setPayOrderId(payOrderId);
        payOrder.setStatus(PayConstant.PAY_STATUS_COMPLETE);
        if (StringUtils.isNotBlank(channelOrderNo)) payOrder.setChannelOrderNo(channelOrderNo);
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        criteria.andPayOrderIdEqualTo(payOrderId);
        criteria.andStatusEqualTo(PayConstant.PAY_STATUS_SUCCESS);
        return updateSuccess4Transactional(payOrder, example);
    }


    @Transactional(transactionManager = "transactionManager", rollbackFor = Exception.class)
    int updateSuccess4Transactional(PayOrder payOrder, PayOrderExample example) {
        int count = payOrderMapper.updateByExampleSelective(payOrder, example);
        // 更新成功且为平台账户,增加商户资金账户流水记录
        payOrder = selectPayOrder(payOrder.getPayOrderId());
        if (count == 1 && payOrder.getMchType() == MchConstant.MCH_TYPE_PLATFORM) {
            mchAccountService.creditToAccount(payOrder);
        }
        return count;
    }


    @Override
    public int updateStatus4Complete(String payOrderId) {
        PayOrder payOrder = new PayOrder();
        payOrder.setPayOrderId(payOrderId);
        payOrder.setStatus(PayConstant.PAY_STATUS_COMPLETE);
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        criteria.andPayOrderIdEqualTo(payOrderId);
        criteria.andStatusEqualTo(PayConstant.PAY_STATUS_SUCCESS);
        return payOrderMapper.updateByExampleSelective(payOrder, example);
    }

    // 更新为失败
    @Override
    public int updateStatus4Fail(String payOrderId) {
        PayOrder payOrder = new PayOrder();
        payOrder.setPayOrderId(payOrderId);
        payOrder.setStatus(PayConstant.PAY_STATUS_FAILED);
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        criteria.andPayOrderIdEqualTo(payOrderId);
        criteria.andStatusEqualTo(PayConstant.PAY_STATUS_PAYING);
        return payOrderMapper.updateByExampleSelective(payOrder, example);
    }

    @Override
    public void updateCodeAndErrorMessage(PayOrder payOrder) {
        payOrderMapper.updateCodeAndErrorMessage(payOrder);
    }

    public int updateNotify(String payOrderId, byte count) {
        PayOrder newPayOrder = new PayOrder();
        newPayOrder.setPayOrderId(payOrderId);
        return payOrderMapper.updateByPrimaryKeySelective(newPayOrder);
    }

    @Override
    public PayOrder find(PayOrder payOrder) {
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payOrder);
        List<PayOrder> payOrderList = payOrderMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(payOrderList)) return null;
        return payOrderList.get(0);
    }

    @Override
    public PayOrder findByPayOrderId(String payOrderId) {
        return payOrderMapper.selectByPrimaryKey(payOrderId);
    }

    @Override
    public PayOrder findByMchIdAndPayOrderId(Long mchId, String payOrderId) {
        return selectByMchIdAndPayOrderId(mchId, payOrderId);
    }

    @Override
    public PayOrder findByMchOrderNo(String mchOrderNo) {
        PayOrder payOrder = new PayOrder();
        payOrder.setMchOrderNo(mchOrderNo);
        return find(payOrder);
    }

    @Override
    public List<PayOrder> select(Long mchId, int offset, int limit, PayOrder payOrder, Date createTimeStart, Date createTimeEnd, int highest, Date paySuccessTimeStart, Date paySuccessTimeEnd) {
        PayOrderExample example = new PayOrderExample();
        example.setOrderByClause("createTime DESC");
        example.setOffset(offset);
        example.setLimit(limit);
        example.setPassageId(payOrder.getPassageId());
        PayOrderExample.Criteria criteria = example.createCriteria();
        if (mchId != null) criteria.andMchIdEqualTo(mchId);
        setCriteria(criteria, payOrder, createTimeStart, createTimeEnd, highest, paySuccessTimeStart, paySuccessTimeEnd);
        return payOrderMapper.selectByExample(example);
    }

    @Override
    public List<PayOrder> select(int offset, int limit, PayOrder payOrder, Date createTimeStart, Date createTimeEnd, int highest, Date paySuccessTimeStart, Date paySuccessTimeEnd) {
        return select(null, offset, limit, payOrder, createTimeStart, createTimeEnd, highest, paySuccessTimeStart, paySuccessTimeEnd);
    }

    @Override
    public List<PayOrderExport> exportOrder(int offset, int limit, PayOrderExport payOrderExport) {
        return payOrderMapper.exportOrder(offset, limit, payOrderExport);
    }

    @Override
    public Integer count(Long mchId, PayOrder payOrder, Date createTimeStart, Date createTimeEnd, int highest, Date paySuccessTimeStart, Date paySuccessTimeEnd) {
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        if (mchId != null) criteria.andMchIdEqualTo(mchId);
        setCriteria(criteria, payOrder, createTimeStart, createTimeEnd, highest, paySuccessTimeStart, paySuccessTimeEnd);
        return payOrderMapper.countByExample(example);
    }

    @Override
    public Integer count(PayOrder payOrder, Date createTimeStart, Date createTimeEnd, int highest, Date paySuccessTimeStart, Date paySuccessTimeEnd) {
        return count(null, payOrder, createTimeStart, createTimeEnd, highest, paySuccessTimeStart, paySuccessTimeEnd);
    }

    @Override
    public Integer count(PayOrder payOrder, List<Byte> statusList) {
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payOrder);
        if (CollectionUtils.isNotEmpty(statusList)) criteria.andStatusIn(statusList);
        return payOrderMapper.countByExample(example);
    }

    @Override
    public int updateByPayOrderId(String payOrderId, PayOrder payOrder) {
        payOrder.setPayOrderId(payOrderId);
        return payOrderMapper.updateByPrimaryKeySelective(payOrder);


    }

    @Override
    public Long sumAmount(PayOrder payOrder, List<Byte> statusList) {
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        setCriteria(criteria, payOrder);
        if (CollectionUtils.isNotEmpty(statusList)) criteria.andStatusIn(statusList);
        return payOrderMapper.sumAmountByExample(example);
    }

    @Override
    public List<PayOrder> select(String channelMchId, String billDate, List<Byte> statusList) {
        PayOrderExample example = new PayOrderExample();
        example.setOrderByClause("createTime DESC");
        PayOrderExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(channelMchId)) criteria.andChannelMchIdEqualTo(channelMchId);
        if (CollectionUtils.isNotEmpty(statusList)) criteria.andStatusIn(statusList);
        if (StringUtils.isNotBlank(billDate)) {
            Date beginDate = DateUtil.str2date(billDate + " 00:00:00");
            Date endDate = DateUtil.str2date(billDate + " 23:59:59");
            criteria.andCreateTimeBetween(beginDate, endDate);
        }
        return payOrderMapper.selectByExample(example);
    }

    @Override
    public List<PayOrder> selectAllBill(int offset, int limit, String billDate) {
        PayOrderExample example = new PayOrderExample();
        example.setOrderByClause("mchId ASC");
        example.setLimit(limit);
        example.setOffset(offset);
        PayOrderExample.Criteria criteria = example.createCriteria();
        List<Byte> statusList = new LinkedList<>();
        // 查询成功或处理完成
        statusList.add(PayConstant.PAY_STATUS_SUCCESS);
        statusList.add(PayConstant.PAY_STATUS_COMPLETE);
        criteria.andStatusIn(statusList);
        if (StringUtils.isNotBlank(billDate)) {
            Date beginDate = DateUtil.str2date(billDate + " 00:00:00");
            Date endDate = DateUtil.str2date(billDate + " 23:59:59");
            criteria.andCreateTimeBetween(beginDate, endDate);
        }
        return payOrderMapper.selectByExample(example);
    }

    @Override
    public Map count4Income(Long agentId, Long mchId, Byte productType, String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if (agentId != null) param.put("agentId", agentId);
        if (mchId != null) param.put("mchId", mchId);
        if (productType != null) param.put("productType", productType);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return payOrderMapper.count4Income(param);
    }

    @Override
    public List<Map> count4MchTop(Long agentId, Long mchId, Byte productType, String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if (agentId != null) param.put("agentId", agentId);
        if (mchId != null) param.put("mchId", mchId);
        if (productType != null && productType != -99) param.put("productType", productType);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return payOrderMapper.count4MchTop(param);
    }

    @Override
    public List<Map> count4Pay(String idName, String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if (StringUtils.isBlank(idName)) return null;
        param.put("idName", idName);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return payOrderMapper.count4Pay(param);
    }

    @Override
    public List<Map> count4PayProduct(String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return payOrderMapper.count4PayProduct(param);
    }

    @Override
    public Long count4PayProducts(String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return payOrderMapper.count4PayProducts(param);
    }

    @Override
    public List<PayDataStatistics> count5DataStatistics(int offset, int limit, PayDataStatistics payDataStatistics) {
        PayDataStatistics payDataStatistics1 = new PayDataStatistics();
        if (StringUtils.isNotBlank(payDataStatistics.getCreateTimeStart())) {
            payDataStatistics1.setCreateTimeStart(payDataStatistics.getCreateTimeStart());
        }
        if (StringUtils.isNotBlank(payDataStatistics.getCreateTimeEnd())) {
            payDataStatistics1.setCreateTimeEnd(payDataStatistics.getCreateTimeEnd());
        }
        payDataStatistics1.setOffset(offset);
        payDataStatistics1.setLimit(limit);
        payDataStatistics1.setMchId(payDataStatistics.getMchId());
        payDataStatistics1.setPassageId(payDataStatistics.getPassageId());
        return payOrderMapper.count5DataStatistics(payDataStatistics1);
    }

    @Override
    public Long countDataStatisAmount(int offset, int limit, PayDataStatistics payDataStatistics) {
        PayDataStatistics payDataStatistics1 = new PayDataStatistics();
        if (StringUtils.isNotBlank(payDataStatistics.getCreateTimeStart())) {
            payDataStatistics1.setCreateTimeStart(payDataStatistics.getCreateTimeStart());
        }
        if (StringUtils.isNotBlank(payDataStatistics.getCreateTimeEnd())) {
            payDataStatistics1.setCreateTimeEnd(payDataStatistics.getCreateTimeEnd());
        }
        payDataStatistics1.setOffset(offset);
        payDataStatistics1.setLimit(limit);
        payDataStatistics1.setMchId(payDataStatistics.getMchId());
        payDataStatistics1.setPassageId(payDataStatistics.getPassageId());
        return payOrderMapper.countDataStatisAmount(payDataStatistics1);
    }

    @Override
    public List<PayDataStatistics> merchanttopupdata(int offset, int limit, PayDataStatistics payDataStatistics) {
        PayDataStatistics payDataStatistics1 = new PayDataStatistics();
        if (StringUtils.isNotBlank(payDataStatistics.getCreateTimeStart())) {
            payDataStatistics1.setCreateTimeStart(payDataStatistics.getCreateTimeStart());
        }
        if (StringUtils.isNotBlank(payDataStatistics.getCreateTimeEnd())) {
            payDataStatistics1.setCreateTimeEnd(payDataStatistics.getCreateTimeEnd());
        }
        payDataStatistics1.setOffset(offset);
        payDataStatistics1.setLimit(limit);
        payDataStatistics1.setMchId(payDataStatistics.getMchId());
        payDataStatistics1.setProductId(payDataStatistics.getProductId());
        return payOrderMapper.merchanttopupdata(payDataStatistics1);
    }


    @Override
    public Long sumAmount4PayPassageAccount(int payPassageAccountId, Date creatTimeStart, Date createTimeEnd) {
        PayOrderExample example = new PayOrderExample();
        PayOrderExample.Criteria criteria = example.createCriteria();
        criteria.andPassageAccountIdEqualTo(payPassageAccountId);
        if (creatTimeStart != null) {
            criteria.andCreateTimeGreaterThanOrEqualTo(creatTimeStart);
        }
        if (createTimeEnd != null) {
            criteria.andCreateTimeLessThanOrEqualTo(createTimeEnd);
        }

        List<Byte> listStatus = new LinkedList<Byte>();
        listStatus.add(PayConstant.PAY_STATUS_SUCCESS);
        listStatus.add(PayConstant.PAY_STATUS_COMPLETE);
        criteria.andStatusIn(listStatus);
        return payOrderMapper.sumAmountByExample(example);
    }

    @Override
    public Map count4All(Long agentId, Long mchId, Long productId, String payOrderId, String mchOrderNo, Byte productType, String createTimeStart, String createTimeEnd, String paySuccessTimeStart, String paySuccessTimeEnd, Long passageId, Long minAmount, Long maxAmount) {
        Map param = new HashMap<>();
        if (agentId != null) param.put("agentId", agentId);
        if (mchId != null) param.put("mchId", mchId);
        if (productId != null && productId != -99) param.put("productId", productId);
        if (passageId != null && passageId != -99) param.put("passageId", passageId);
        if (StringUtils.isNotBlank(payOrderId)) param.put("payOrderId", payOrderId);
        if (StringUtils.isNotBlank(mchOrderNo)) param.put("mchOrderNo", mchOrderNo);
        if (productType != null && productType != -99) param.put("productType", productType);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        if (StringUtils.isNotBlank(paySuccessTimeStart)) param.put("paySuccessTimeStart", paySuccessTimeStart);
        if (StringUtils.isNotBlank(paySuccessTimeEnd)) param.put("paySuccessTimeEnd", paySuccessTimeEnd);
        if (minAmount != null && minAmount != -99) param.put("minAmount", minAmount);
        if (maxAmount != null && maxAmount != -99) param.put("maxAmount", maxAmount);
        return payOrderMapper.count4All(param);
    }

    @Override
    public Map count4Success(Long agentId, Long mchId, Long productId, String payOrderId, String mchOrderNo, Byte productType, String createTimeStart, String createTimeEnd, String paySuccessTimeStart, String paySuccessTimeEnd, Long passageId, Long minAmount, Long maxAmount) {
        Map param = new HashMap<>();
        if (agentId != null) param.put("agentId", agentId);
        if (mchId != null) param.put("mchId", mchId);
        if (productId != null && productId != -99) param.put("productId", productId);
        if (passageId != null && passageId != -99) param.put("passageId", passageId);
        if (StringUtils.isNotBlank(payOrderId)) param.put("payOrderId", payOrderId);
        if (StringUtils.isNotBlank(mchOrderNo)) param.put("mchOrderNo", mchOrderNo);
        if (productType != null && productType != -99) param.put("productType", productType);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        if (StringUtils.isNotBlank(paySuccessTimeStart)) param.put("paySuccessTimeStart", paySuccessTimeStart);
        if (StringUtils.isNotBlank(paySuccessTimeEnd)) param.put("paySuccessTimeEnd", paySuccessTimeEnd);
        if (minAmount != null && minAmount != -99) param.put("minAmount", minAmount);
        if (maxAmount != null && maxAmount != -99) param.put("maxAmount", maxAmount);
        return payOrderMapper.count4Success(param);
    }

    @Override
    public Map mchCount4Success(Long agentId, Long mchId, Long productId, String payOrderId, String mchOrderNo, Byte productType, String createTimeStart, String createTimeEnd, String paySuccessTimeStart, String paySuccessTimeEnd, Long passageId, Long minAmount, Long maxAmount) {
        Map param = new HashMap<>();
        if (agentId != null) param.put("agentId", agentId);
        if (mchId != null) param.put("mchId", mchId);
        if (productId != null && productId != -99) param.put("productId", productId);
        if (passageId != null && passageId != -99) param.put("passageId", passageId);
        if (StringUtils.isNotBlank(payOrderId)) param.put("payOrderId", payOrderId);
        if (StringUtils.isNotBlank(mchOrderNo)) param.put("mchOrderNo", mchOrderNo);
        if (productType != null && productType != -99) param.put("productType", productType);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        if (StringUtils.isNotBlank(paySuccessTimeStart)) param.put("paySuccessTimeStart", paySuccessTimeStart);
        if (StringUtils.isNotBlank(paySuccessTimeEnd)) param.put("paySuccessTimeEnd", paySuccessTimeEnd);
        if (minAmount != null && minAmount != -99) param.put("minAmount", minAmount);
        if (maxAmount != null && maxAmount != -99) param.put("maxAmount", maxAmount);
        return payOrderMapper.mchCount4Success(param);
    }

    @Override
    public Map count4Fail(Long agentId, Long mchId, Long productId, String payOrderId, String mchOrderNo, Byte productType, String createTimeStart, String createTimeEnd, String paySuccessTimeStart, String paySuccessTimeEnd, Long passageId, Long minAmount, Long maxAmount) {
        Map param = new HashMap<>();
        if (agentId != null) param.put("agentId", agentId);
        if (mchId != null) param.put("mchId", mchId);
        if (productId != null && productId != -99) param.put("productId", productId);
        if (passageId != null && passageId != -99) param.put("passageId", passageId);
        if (StringUtils.isNotBlank(payOrderId)) param.put("payOrderId", payOrderId);
        if (StringUtils.isNotBlank(mchOrderNo)) param.put("mchOrderNo", mchOrderNo);
        if (productType != null && productType != -99) param.put("productType", productType);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        if (StringUtils.isNotBlank(paySuccessTimeStart)) param.put("paySuccessTimeStart", paySuccessTimeStart);
        if (StringUtils.isNotBlank(paySuccessTimeEnd)) param.put("paySuccessTimeEnd", paySuccessTimeEnd);
        if (minAmount != null && minAmount != -99) param.put("minAmount", minAmount);
        if (maxAmount != null && maxAmount != -99) param.put("maxAmount", maxAmount);
        return payOrderMapper.count4Fail(param);
    }


    @Override
    public List<Map> daySuccessRate(int offset, int limit, String createTimeStart, String createTimeEnd, Long mchId) {
        Map param = new HashMap<>();
        param.put("offset", offset);
        param.put("limit", limit);
        if (mchId != null) param.put("mchId", mchId);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return payOrderMapper.daySuccessRate(param);
    }

    @Override
    public List<Map> hourSuccessRate(int offset, int limit, String createTimeStart, String createTimeEnd, Long mchId) {
        Map param = new HashMap<>();
        param.put("offset", offset);
        param.put("limit", limit);
        if (mchId != null) param.put("mchId", mchId);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return payOrderMapper.hourSuccessRate(param);
    }

    @Override
    public Map<String, Object> countDaySuccessRate(String createTimeStart, String createTimeEnd, Long mchId) {
        Map param = new HashMap<>();
        if (mchId != null) param.put("mchId", mchId);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return payOrderMapper.countDaySuccessRate(param);
    }

    @Override
    public Map<String, Object> countHourSuccessRate(String createTimeStart, String createTimeEnd, Long mchId) {
        Map param = new HashMap<>();
        if (mchId != null) param.put("mchId", mchId);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return payOrderMapper.countHourSuccessRate(param);
    }

    @Override
    public Map dateRate(String dayStart, String dayEnd) {
        Map param = new HashMap<>();
        if (StringUtils.isNotBlank(dayStart)) param.put("createTimeStart", dayStart);
        if (StringUtils.isNotBlank(dayEnd)) param.put("createTimeEnd", dayEnd);
        return payOrderMapper.dateRate(param);
    }

    @Override
    public Map hourRate(String dayStart, String dayEnd) {
        Map param = new HashMap<>();
        if (StringUtils.isNotBlank(dayStart)) param.put("createTimeStart", dayStart);
        if (StringUtils.isNotBlank(dayEnd)) param.put("createTimeEnd", dayEnd);
        return payOrderMapper.hourRate(param);
    }

    @Override
    public Map orderDayAmount(Long mchId, String dayStart, String dayEnd) {
        Map param = new HashMap<>();
        if (mchId != null) param.put("mchId", mchId);
        if (StringUtils.isNotBlank(dayStart)) param.put("createTimeStart", dayStart);
        if (StringUtils.isNotBlank(dayEnd)) param.put("createTimeEnd", dayEnd);
        return payOrderMapper.orderDayAmount(param);
    }

    @Override
    public Long getOrderTimeLeft(String payOrderId, Long timeOut) {
        Map param = new HashMap<>();
        param.put("payOrderId", payOrderId);
        param.put("timeOut", timeOut);
        return payOrderMapper.getOrderTimeLeft(param);
    }

    @Override
    public Long getAvailableAmount(PayOrder payOrder, Long payTimeOut, Long incrRange, Long incrStep) {
        Map param = new HashMap<>();
        Long amount = payOrder.getAmount();
        param.put("channelMchId", payOrder.getChannelMchId());
        param.put("minAmount", amount);
        param.put("maxAmount", amount + incrRange);
        param.put("timeOut", payTimeOut);
        List<Map> orderList = payOrderMapper.selectAmountRange(param);
        if (CollectionUtils.isEmpty(orderList)) return amount;
        Map<String, Long> dbAmountMap = new HashMap<>();
        for (Map map : orderList) {
            dbAmountMap.put(map.get("amount").toString(), Long.parseLong(map.get("amount").toString()));
        }
        // 从amount增加到amount+amountRange,如果db中没有则返回
        for (long i = amount; i <= amount + incrRange; i = i + incrStep) {
            if (dbAmountMap.get(i + "") == null) {
                return i;
            }
        }
        return null;
    }

    @Override
    public PayOrder findByAmount(Long amount, String rightCardNo, Long payTimeOut) {
        Map param = new HashMap<>();
        param.put("amount", amount);
        param.put("rightCardNo", rightCardNo);
        param.put("timeOut", payTimeOut);
        List<PayOrder> orderList = payOrderMapper.selectByAmount(param);
        if (CollectionUtils.isEmpty(orderList)) return null;
        if (orderList.size() > 1) {
            _log.info("根据金额查订单有多个,不能确定唯一订单，返回空,orderList={}", orderList);
            return null;
        }
        return orderList.get(0);
    }


    /**
     * 统计码商代理下企业号的订单
     *
     * @param
     * @return
     */
    public Map count4AllByAppId(Long agentId, String appId, String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if (agentId != null) param.put("agentId", agentId);
        if (appId != null) param.put("appId", appId);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return payOrderMapper.count4AllByPID(param);
    }


    /**
     * 统计码商代理下企业号的订单
     *
     * @param
     * @return
     */
    public Map count4SuccessByAppId(Long agentId, String appId, String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if (agentId != null) param.put("agentId", agentId);
        if (appId != null) param.put("appId", appId);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return payOrderMapper.count4SuccessByAppId(param);
    }


    /**
     * 统计码商代理下企业号的订单
     *
     * @param
     * @return
     */
    public Map count4FailByAppId(Long agentId, String appId, String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if (agentId != null) param.put("agentId", agentId);
        if (appId != null) param.put("appId", appId);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return payOrderMapper.count4FailByPID(param);
    }


    /**
     * 商户收入统计
     *
     * @param agentId
     * @param mchId
     * @param productType
     * @param createTimeStart
     * @param createTimeEnd
     * @return
     */
    @Override
    public Map mchCount4Income(Long agentId, Long mchId, Byte productType, String createTimeStart, String createTimeEnd) {
        Map param = new HashMap<>();
        if (agentId != null) param.put("agentId", agentId);
        if (mchId != null) param.put("mchId", mchId);
        if (productType != null) param.put("productType", productType);
        if (StringUtils.isNotBlank(createTimeStart)) param.put("createTimeStart", createTimeStart);
        if (StringUtils.isNotBlank(createTimeEnd)) param.put("createTimeEnd", createTimeEnd);
        return payOrderMapper.mchCount4Income(param);
    }


    void setCriteria(PayOrderExample.Criteria criteria, PayOrder payOrder, Date createTimeStart, Date createTimeEnd, int highest, Date paySuccessTimeStart, Date paySuccessTimeEnd) {
        if (payOrder != null) {
            if (payOrder.getMchId() != null) criteria.andMchIdEqualTo(payOrder.getMchId());
            if (payOrder.getProductId() != null && payOrder.getProductId() != -99)
                criteria.andProductIdEqualTo(payOrder.getProductId());
            if ((payOrder.getAgentId() != null)) criteria.andAgentIdEqualTo(payOrder.getAgentId());
            if (payOrder.getMinAmount() != null) criteria.andAmountEqualToMinAmount(payOrder.getMinAmount());
            if (payOrder.getMaxAmount() != null) criteria.andAmountEqualToMaxAmount(payOrder.getMaxAmount());
            if (payOrder.getPaySuccTime() != null) criteria.andPaySuccTimeEqualTo(payOrder.getPaySuccTime());
            if (payOrder.getPassageId() != null && payOrder.getPassageId() != -99)
                criteria.andPassageIdEqualTo(payOrder.getPassageId());
            if (StringUtils.isNotBlank(payOrder.getPayOrderId()))
                criteria.andPayOrderIdEqualTo(payOrder.getPayOrderId());
            if (StringUtils.isNotBlank(payOrder.getMchOrderNo()))
                criteria.andMchOrderNoEqualTo(payOrder.getMchOrderNo());
            if (StringUtils.isNotBlank(payOrder.getChannelOrderNo()))
                criteria.andChannelOrderNoEqualTo(payOrder.getChannelOrderNo());
            if (payOrder.getStatus() != null && payOrder.getStatus() != -99)
                criteria.andStatusEqualTo(payOrder.getStatus());
            if (payOrder.getProductType() != null && payOrder.getProductType() != -99)
                criteria.andProductTypeEqualTo(payOrder.getProductType());
            if (StringUtils.isNotBlank(payOrder.getChannelMchId()))
                criteria.andChannelMchIdEqualTo(payOrder.getChannelMchId());
        }

        if (paySuccessTimeStart != null) {
            criteria.andPaySuccessTimeGreaterThanOrEqualTo(paySuccessTimeStart);
        }
        if (paySuccessTimeEnd != null) {
            criteria.andpaySuccTimeLessThanOrEqualTo(paySuccessTimeEnd);
        }

        if (highest > 0) {
            criteria.andHighest();
        } else {
            if (createTimeStart != null) {
                criteria.andCreateTimeGreaterThanOrEqualTo(createTimeStart);
            }
            if (createTimeEnd != null) {
                criteria.andCreateTimeLessThanOrEqualTo(createTimeEnd);
            }
        }
    }


    /**
     * 统计退款订单
     *
     * @param
     * @return
     */
    @Override
    public Map count4Refund(Map map) {

        Map param = new HashMap();
        if (map.containsKey("payOrderId") && map.get("payOrderId") != null && StringUtils.isNotBlank(map.get("payOrderId").toString())) {
            param.put("payOrderId", map.get("payOrderId").toString());
        }

        if (map.containsKey("passageAccountId") && map.get("passageAccountId") != null && !map.get("passageAccountId").toString().equals("-99")) {
            param.put("passageAccountId", map.get("passageAccountId").toString());
        }

        if (map.get("createTimeStart") != null) {
            param.put("createTimeStart", map.get("createTimeStart"));
        }

        if (map.get("createTimeEnd") != null) {
            param.put("createTimeEnd", map.get("createTimeEnd"));
        }

        return payOrderMapper.count4Refund(param);
    }

    void setCriteria(PayOrderExample.Criteria criteria, PayOrder payOrder) {
        setCriteria(criteria, payOrder, null, null, 0, null, null);
    }

}
