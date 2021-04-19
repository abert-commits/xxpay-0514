package org.xxpay.pay.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import javafx.util.Pair;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.WeightRandom;
import org.xxpay.core.entity.*;
import org.xxpay.pay.mq.BaseNotify4MchPay;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author: dingzhiwei
 * @date: 17/9/9
 * @description:
 */
@Service
public class PayOrderService {

    private static final MyLog _log = MyLog.getLog(PayOrderService.class);

    @Autowired
    private RpcCommonService rpcCommonService;

    @Autowired
    public BaseNotify4MchPay baseNotify4MchPay;


    public PayOrder query(Long mchId, String payOrderId, String mchOrderNo, boolean executeNotify) {
        PayOrder payOrder;
        if (StringUtils.isNotBlank(payOrderId)) {
            payOrder = rpcCommonService.rpcPayOrderService.selectByMchIdAndPayOrderId(mchId, payOrderId);
        } else {
            payOrder = rpcCommonService.rpcPayOrderService.selectByMchIdAndMchOrderNo(mchId, mchOrderNo);
        }
        if (payOrder == null) return null;

        if (executeNotify && (PayConstant.PAY_STATUS_SUCCESS == payOrder.getStatus() || PayConstant.PAY_STATUS_COMPLETE == payOrder.getStatus())) {
            baseNotify4MchPay.doNotify(payOrder, false);
            _log.info("业务查单完成,并再次发送业务支付通知.发送完成");
        }
        return payOrder;
    }

    public PayOrder query(Long mchId, String payOrderId, String mchOrderNo) {
        return query(mchId, payOrderId, mchOrderNo, false);
    }

    /**
     * 获取支付通道子账户
     *
     * @param mchPayPassage
     * @param riskLog
     * @param amountL
     * @return
     */
    public Object getPayPassageAccount(MchPayPassage mchPayPassage, String riskLog, long amountL) {
        PayPassageAccount payPassageAccount = null;
        if (mchPayPassage == null) {
            return "商户未设置该通道";
        }
        Integer productId = mchPayPassage.getProductId();
        Long mchId = mchPayPassage.getMchId();
        if (mchPayPassage == null || mchPayPassage.getStatus() == MchConstant.PUB_NO) {
            return "通道[productId=" + productId + ",mchId=" + mchId + "],已维护!";
        }

        if (mchPayPassage.getIfMode() == 1) {    // 单独接口
            Integer payPassageId = mchPayPassage.getPayPassageId();
            if (mchPayPassage.getPayPassageAccountId() != null && mchPayPassage.getPayPassageAccountId() > 0) {
                payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(mchPayPassage.getPayPassageAccountId());
                PayPassage payPassage = rpcCommonService.rpcPayPassageService.findById(payPassageId);

                // 确定账号是否可用（风控）
                payPassageAccount = getAvailablePPA(riskLog, amountL, payPassage, payPassageAccount);
            } else {
                Object obj = getPPA(riskLog, payPassageId, amountL);
                if (obj instanceof String) {
                    return obj;
                }
                if (obj instanceof PayPassageAccount) {
                    payPassageAccount = (PayPassageAccount) obj;
                }
            }
        } else if (mchPayPassage.getIfMode() == 2) {  // 轮询接口
            // 轮询通道参数
            String pollParam = mchPayPassage.getPollParam();
            if (StringUtils.isBlank(pollParam)) {
                return "没有配置轮询通道[productId=" + productId + ",mchId=" + mchId + "]";
            }

            JSONArray array = JSONArray.parseArray(pollParam);
            if (CollectionUtils.isEmpty(array)) {
                return "配置的轮询通道为空[productId=" + productId + ",mchId=" + mchId + "]";
            }
            //在这里做筛选金额的操作
            // [{"payPassageId":1111,"weight":1},{"payPassageId":222,"weight":2}]
            List<Pair> pollPayPassList = new LinkedList<>();
            for (int i = 0; i < array.size(); i++) {
                JSONObject object = array.getJSONObject(i);
                int pid = object.getInteger("payPassageId");
                int weight = object.getInteger("weight");
                PayPassage model = rpcCommonService.rpcPayPassageService.findById(pid);
                if (model != null && model.getStatus() == 0) {
                    //判断通道状态是否关闭，如果关闭，直接跳下一次循环
                    continue;
                }
                if (!StringUtils.isBlank(model.getRemark())) {
                    JSONObject jsonObject = JSONObject.parseObject(model.getRemark());
                    if (jsonObject.getString("Type").equals("1")) {
                        List<String> listAmount = Arrays.asList(jsonObject.getString("Value").split("-"));
                        if (listAmount.contains(String.format(new BigDecimal(amountL).divide(new BigDecimal(100)).toString(), "f0"))) {
                            Pair pair = new Pair(pid, weight);
                            pollPayPassList.add(pair);
                            continue;
                        }
                    } else {
                        List<String> listAmount = Arrays.asList(jsonObject.getString("Value").split("-"));
                        if (new BigDecimal(amountL / 100).intValue() >= new BigDecimal(listAmount.get(0)).intValue()
                                && new BigDecimal(amountL / 100).intValue() <= new BigDecimal(listAmount.get(1)).intValue()) {
                            Pair pair = new Pair(pid, weight);
                            pollPayPassList.add(pair);
                            continue;
                        }
                    }
                } else {
                    Pair pair = new Pair(pid, weight);
                    pollPayPassList.add(pair);
                }
            }

            // 递归取得可用通道账户
            Object obj = recursiveGetPPA(riskLog, pollPayPassList, amountL);
            if (obj instanceof String) {
                return obj;
            }
            if (obj instanceof PayPassageAccount) {
                payPassageAccount = (PayPassageAccount) obj;
            }
        }

        return payPassageAccount;
    }


    public Object getGroupPayPassageAccount(MchGroupPayPassage mchGroupPayPassage, String riskLog, long amountL) {
        if (mchGroupPayPassage == null) {
            return "商户所在组未设置该支付渠道!";
        }

        PayPassageAccount payPassageAccount = null;
        Integer productId = mchGroupPayPassage.getProductId();
        Long mchId = mchGroupPayPassage.getMchGroupId();
        if (mchGroupPayPassage.getStatus() == MchConstant.PUB_NO) {
            return "商户组通道[productId=" + productId + ",mchId=" + mchId + "],已维护!";
        }

        if (mchGroupPayPassage.getIfMode() == 1) {    // 单独接口
            Integer payPassageId = mchGroupPayPassage.getPayPassageId();
            if (mchGroupPayPassage.getPayPassageAccountId() != null && mchGroupPayPassage.getPayPassageAccountId() > 0) {
                payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(mchGroupPayPassage.getPayPassageAccountId());
                PayPassage payPassage = rpcCommonService.rpcPayPassageService.findById(payPassageId);
                if (payPassage == null || payPassage.getStatus() == MchConstant.PUB_NO) {
                    return "商户组轮询通道[productId=" + productId + ",mchId=" + mchId + "],已维护!";
                }
                // 确定账号是否可用
                payPassageAccount = getAvailablePPA(riskLog, amountL, payPassage, payPassageAccount);
            } else {
                Object obj = getPPA(riskLog, payPassageId, amountL);
                if (obj instanceof String) {
                    return obj;
                }
                if (obj instanceof PayPassageAccount) {
                    payPassageAccount = (PayPassageAccount) obj;
                }
            }
        } else if (mchGroupPayPassage.getIfMode() == 2) {  // 轮询接口
            // 轮询通道参数
            String pollParam = mchGroupPayPassage.getPollParam();
            if (StringUtils.isBlank(pollParam)) {
                return "商户组没有配置轮询通道[productId=" + productId + ",mchId=" + mchId + "]";
            }

            JSONArray array = JSONArray.parseArray(pollParam);
            if (CollectionUtils.isEmpty(array)) {
                return "商户组配置的轮询通道为空[productId=" + productId + ",mchId=" + mchId + "]";
            }

            //在这里做筛选金额的操作
            // [{"payPassageId":1111,"weight":1},{"payPassageId":222,"weight":2}]
            List<Pair> pollPayPassList = new LinkedList<>();
            for (int i = 0; i < array.size(); i++) {
                JSONObject object = array.getJSONObject(i);
                int pid = object.getInteger("payPassageId");
                int weight = object.getInteger("weight");
                PayPassage model = rpcCommonService.rpcPayPassageService.findById(pid);
                if (model != null && model.getStatus() == 0) {
                    //判断通道状态是否关闭，如果关闭，直接跳下一次循环
                    continue;
                }

                if (!StringUtils.isBlank(model.getRemark())) {
                    JSONObject jsonObject = JSONObject.parseObject(model.getRemark());
                    if (jsonObject.getString("Type").equals("1")) {
                        List<String> listAmount = Arrays.asList(jsonObject.getString("Value").split("-"));
                        if (listAmount.contains(String.format(new BigDecimal(amountL).divide(new BigDecimal(100)).toString(), "f0"))) {
                            Pair pair = new Pair(pid, weight);
                            pollPayPassList.add(pair);
                            continue;
                        }
                    } else {
                        List<String> listAmount = Arrays.asList(jsonObject.getString("Value").split("-"));
                        if (new BigDecimal(amountL / 100).intValue() >= new BigDecimal(listAmount.get(0)).intValue()
                                && new BigDecimal(amountL / 100).intValue() <= new BigDecimal(listAmount.get(1)).intValue()) {
                            Pair pair = new Pair(pid, weight);
                            pollPayPassList.add(pair);
                            continue;
                        }
                    }
                } else {
                    Pair pair = new Pair(pid, weight);
                    pollPayPassList.add(pair);
                }
            }

            // 递归取得可用通道账户
            Object obj = recursiveGetPPA(riskLog, pollPayPassList, amountL);
            if (obj instanceof String) {
                return obj;
            }
            if (obj instanceof PayPassageAccount) {
                payPassageAccount = (PayPassageAccount) obj;
            }
        }

        return payPassageAccount;
    }


    /**
     * 递归取子账户
     *
     * @param riskLog
     * @param pollPayPassList
     * @param amountL
     * @return
     */
    private Object recursiveGetPPA(String riskLog, List<Pair> pollPayPassList, long amountL) {
        // 根据权重,随机取得支付通道
        if (CollectionUtils.isEmpty(pollPayPassList)) return null;
        WeightRandom weightRandom = new WeightRandom(pollPayPassList);
        int pid = (Integer) weightRandom.random();
        Object obj = getPPA(riskLog, pid, amountL);
        if (null != obj) return obj;
        // 如果没有取到有效子账户,则从集合中删除,然后递归取
        final CopyOnWriteArrayList<Pair> cowList = new CopyOnWriteArrayList<>(pollPayPassList);
        for (Pair pair : cowList) {
            if (String.valueOf(pid).equals(String.valueOf(pair.getKey()))) {
                cowList.remove(pair);
            }
        }
        return recursiveGetPPA(riskLog, cowList, amountL);
    }

    /**
     * 根据通道ID取子账户
     *
     * @param riskLog
     * @param payPassageId
     * @param amountL
     * @return
     */
    private Object getPPA(String riskLog, int payPassageId, long amountL) {
        // 判断支付通道
        PayPassage payPassage = rpcCommonService.rpcPayPassageService.findById(payPassageId);
        if (payPassage == null || payPassage.getStatus() != MchConstant.PUB_YES) {
            return "支付通道不存在或已关闭[payPassageId=" + payPassageId + "]";
        }
        // 得到可用的支付通道子账户
        List<PayPassageAccount> payPassageAccountList = rpcCommonService.rpcPayPassageAccountService.selectAllByPassageId(payPassageId);
//        List<PayPassageAccount> payPassageAccountList = rpcCommonService.rpcPayPassageAccountService.selectAllByPassageId2(payPassageId);
        if (CollectionUtils.isEmpty(payPassageAccountList)) {
            return "该支付通道没有配置可用子账户[payPassageId=" + payPassageId + "]";
        }
        // 需要根据风控规则得到子账户号
        // 随机打乱子账户顺序
        //Collections.shuffle(payPassageAccountList);

        List<Pair> pollPayPassList = new LinkedList<>();

        for (PayPassageAccount ppa : payPassageAccountList) {
            Pair pair = new Pair(ppa.getId(), ppa.getPollWeight());
            pollPayPassList.add(pair);
        }

        WeightRandom weightRandom = new WeightRandom(pollPayPassList);
        PayPassageAccount availablePayPassageAccount = null;

        int count = 0;
        while (true) {
            count++;
            if (count > 10) break;
            Integer ppaId = (Integer) weightRandom.random();
            PayPassageAccount myPpa = rpcCommonService.rpcPayPassageAccountService.findById(ppaId);
            availablePayPassageAccount = getAvailablePPA(riskLog, amountL, payPassage, myPpa);
            if (availablePayPassageAccount == null) continue;
            break;
        }


        return availablePayPassageAccount;
    }

    /**
     * 获取可用子账户(需判断账户状态,及风控)
     *
     * @param riskLog
     * @param amountL
     * @param ppa
     * @return
     */
    private PayPassageAccount getAvailablePPA(String riskLog, long amountL, PayPassage payPassage, PayPassageAccount ppa) {
        // 判断账号状态为关闭,则返回空
        if (ppa.getStatus() != MchConstant.PUB_YES) {
            return null;
        }
        // 风控继承模式,设置继承的风控信息
        if (ppa.getRiskMode() == 1) {
            ppa.setRiskStatus(payPassage.getRiskStatus());
            ppa.setMaxDayAmount(payPassage.getMaxDayAmount());
            ppa.setMaxEveryAmount(payPassage.getMaxEveryAmount());
            ppa.setMinEveryAmount(payPassage.getMinEveryAmount());
            ppa.setTradeStartTime(payPassage.getTradeStartTime());
            ppa.setTradeEndTime(payPassage.getTradeEndTime());
        }
        // 判断风控状态为关闭,则返回账号对象
        if (ppa.getRiskStatus() == MchConstant.PUB_NO) {   // 风控状态是关闭的
            return ppa;
        }
        // 下面逻辑判断是否触发风控规则,如果触发则返回空
        // 单笔交易金额是否满足
        long maxEveryAmount = ppa.getMaxEveryAmount();
        long minEveryAmount = ppa.getMinEveryAmount();
        if (amountL < minEveryAmount || amountL > maxEveryAmount) {
            _log.info("{}单笔交易金额触发风控.payPassageAccountId={},maxEveryAmount={},minEveryAmount={},amount={}", riskLog, ppa.getId(), maxEveryAmount, minEveryAmount, amountL);
            return null;
        }
        // 交易时间是否满足
        String ymd = DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD);
        String tradeStartTime = ymd + " " + (StringUtils.isBlank(ppa.getTradeStartTime()) ? "00:00:00" : ppa.getTradeStartTime());
        String tradeEndTime = ymd + " " + (StringUtils.isBlank(ppa.getTradeEndTime()) ? "23:59:59" : ppa.getTradeEndTime());

        long startTime = DateUtil.str2date(tradeStartTime).getTime();
        long endTime = DateUtil.str2date(tradeEndTime).getTime();
        long currentTime = System.currentTimeMillis();
        if (currentTime < startTime || currentTime > endTime) {
            _log.info("{}交易时间触发风控.payPassageAccountId={},tradeStartTime={},tradeEndTime={}", riskLog, ppa.getId(), tradeStartTime, tradeEndTime);
            return null;
        }
        // 今日总交易金额是否满足
        long maxDayAmount = ppa.getMaxDayAmount();
        long dayAmount = rpcCommonService.rpcPayOrderService.sumAmount4PayPassageAccount(ppa.getId(),
                DateUtil.str2date(ymd + " 00:00:00"),
                DateUtil.str2date(ymd + " 23:59:59"));
        if (dayAmount > maxDayAmount) {
            _log.info("{}每日交易总额触发风控.payPassageAccountId={},maxDayAmount={},dayAmount={}", riskLog, ppa.getId(), maxDayAmount, dayAmount);
            return null;
        }
        return ppa;
    }

}
