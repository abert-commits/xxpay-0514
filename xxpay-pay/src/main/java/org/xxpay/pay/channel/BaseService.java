package org.xxpay.pay.channel;

import com.alibaba.fastjson.JSONObject;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.util.FileUtils;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.WeightRandom;
import org.xxpay.core.entity.*;
import org.xxpay.pay.service.RpcCommonService;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 17/12/24
 * @description:
 */
public class BaseService {

    @Autowired
    private RpcCommonService rpcCommonService;

    private static final MyLog _log = MyLog.getLog(BaseService.class);

    protected JSONObject buildRetObj() {
        JSONObject retObj = new JSONObject();
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        return retObj;
    }

    protected JSONObject buildFailRetObj() {
        JSONObject retObj = new JSONObject();
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
        return retObj;
    }

    protected JSONObject buildRetObj(String retValue, String retMsg) {
        JSONObject retObj = new JSONObject();
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, retValue);
        retObj.put(PayConstant.RETURN_PARAM_RETMSG, retMsg);
        return retObj;
    }


    /**
     * 获取三方支付配置信息
     * 如果是平台账户,则使用平台对应的配置,否则使用商户自己配置的渠道
     *
     * @param refundOrder
     * @return
     */
    public String getRefundParam(RefundOrder refundOrder) {
        String payOrderId = refundOrder.getPayOrderId();
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) return "";
        String payParam = "";
        PayPassageAccount payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(payOrder.getPassageAccountId());
        if (payPassageAccount != null && payPassageAccount.getStatus() == MchConstant.PUB_YES) {
            payParam = payPassageAccount.getParam();
        }
        if (StringUtils.isBlank(payParam)) {
            throw new ServiceException(RetEnum.RET_MGR_PAY_PASSAGE_ACCOUNT_NOT_EXIST);
        }
        return payParam;
    }


    /**
     * 获取三方支付配置信息
     * 如果是平台账户,则使用平台对应的配置,否则使用商户自己配置的渠道
     *
     * @param refundOrder
     * @return
     */
    public String getRefundParamAll(RefundOrder refundOrder) {
        String payOrderId = refundOrder.getPayOrderId();
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) return "";
        String payParam = "";
        PayPassageAccount payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(payOrder.getPassageAccountId());
        if (payPassageAccount != null) {
            payParam = payPassageAccount.getParam();
        }
        if (StringUtils.isBlank(payParam)) {
            throw new ServiceException(RetEnum.RET_MGR_PAY_PASSAGE_ACCOUNT_NOT_EXIST);
        }
        return payParam;
    }


    /**
     * 获取三方支付配置信息
     * 如果是平台账户,则使用平台对应的配置,否则使用商户自己配置的渠道
     * 注明：这里查询不管子账户是否关闭，防止回调回来的订单执行失败
     *
     * @param payOrder
     * @return
     */
    public String getPayParamAll(PayOrder payOrder) {
        String payParam = "";
        PayPassageAccount payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(payOrder.getPassageAccountId());
        if (payPassageAccount != null) {
            payParam = payPassageAccount.getParam();
        }
        if (StringUtils.isBlank(payParam)) {
            throw new ServiceException(RetEnum.RET_MGR_PAY_PASSAGE_ACCOUNT_NOT_EXIST);
        }
        return payParam;
    }

    /**
     * 获取证书文件路径
     *
     * @param channelName
     * @param fileName
     * @return
     */
    public String getCertFilePath(String channelName, String certRootPath, String fileName) {
        return certRootPath + File.separator + channelName + File.separator + fileName;
    }


    public static String getExceptionInfo(Exception e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        return baos.toString();
    }

    /**
     * 获取appCertPublicKey证书路径
     *
     * @param payOrderId
     * @param appId
     * @return
     */
    protected String GetAppCertPublicKey(String payOrderId, String appId) {
        try {

            String prentPath = this.getClass().getResource("/").getPath();
            String filePath = prentPath + "certfile/" + appId + "_appcertpublickey.crt";
            filePath = URLDecoder.decode(filePath, "UTF-8");
            File file = new File(filePath);
            //如果文件存在，直接返回
            if (file.exists()) {
                return filePath;
            }

            AlipayConfig condtion = new AlipayConfig();
            condtion.setAPPID(appId);
            condtion.setStatus(1);
            //不存在的话，查询数据库
            AlipayConfig model = rpcCommonService.rpcAliPayConfigService.findAliPayConfig(condtion);
            if (model != null && StringUtils.isNotBlank(model.getAppCertPublickeyFileName())) {
                byte[] appCertPublickeyBytes = rpcCommonService.rpcAliPayConfigService.selectById(model.getId()).getAppCertPublickey();
                FileUtils.byteToFile(appCertPublickeyBytes, filePath);
            }

            _log.info("GetAppCertPublicKey地址：" + filePath);
            return filePath;

        } catch (Exception ex) {
            _log.info("获取AppCertPublicKey发生异常,订单号:" + payOrderId + ",异常信息：" + ex.getMessage());
            return "error:获取appCertPublicKey发生异常";
        }
    }


    /**
     * 获取alipayCertPublicKey证书路径
     *
     * @param payOrderId
     * @param appId
     * @return
     */
    protected String GetAlipayCertPublicKey(String payOrderId, String appId) {
        try {

            String prentPath = this.getClass().getResource("/").getPath();
            String filePath = prentPath + "certfile/" + appId + "_alipaycertpublickey.crt";
            filePath = URLDecoder.decode(filePath, "UTF-8");
            File file = new File(filePath);
            //如果文件存在，直接返回
            if (file.exists()) {
                return filePath;
            }

            AlipayConfig condtion = new AlipayConfig();
            condtion.setAPPID(appId);
            condtion.setStatus(1);
            //不存在的话，查询数据库
            AlipayConfig model = rpcCommonService.rpcAliPayConfigService.findAliPayConfig(condtion);
            if (model != null && StringUtils.isNotBlank(model.getAliPayCertPublickeyFileName())) {
                byte[] aliPayCerPubliceKeyBytes = rpcCommonService.rpcAliPayConfigService.selectById(model.getId()).getAliPayCertPublickey();
                FileUtils.byteToFile(aliPayCerPubliceKeyBytes, filePath);
            }

            _log.info("GetAlipayCertPublicKey地址：" + filePath);
            return filePath;
        } catch (Exception ex) {
            _log.info("获取appCertPublicKey发生异常,订单号:" + payOrderId + ",异常信息：" + ex.getMessage());
            return "error:获取appCertPublicKey发生异常";
        }
    }


    /**
     * 获取AlipayRootCert证书路径
     *
     * @param payOrderId
     * @param appId
     * @return
     */
    protected String GetAlipayRootCert(String payOrderId, String appId) {
        try {
            String prentPath = this.getClass().getResource("/").getPath();
            String myPath = prentPath + "certfile/alipayrootcert.crt";
            myPath = URLDecoder.decode(myPath, "UTF-8");
            _log.info("GetAlipayRootCert地址：" + myPath);
            return myPath;
        } catch (Exception ex) {
            _log.info("获取appCertPublicKey发生异常,订单号:" + payOrderId + ",异常信息：" + ex.getMessage());
            return "error:获取appCertPublicKey发生异常";
        }
    }

    public PayCashCollConfig QueryPayCashCollConfig(String passageAccountId, byte pubStatus) {
        try {

            PayPassageAccount account = rpcCommonService.rpcPayPassageAccountService.findById(Integer.parseInt(passageAccountId));
            PayCashCollConfig selectCondition = new PayCashCollConfig();
            selectCondition.setType(2);//配置类型，红包
            if (pubStatus != -1) {
                selectCondition.setStatus(pubStatus); //仅查询开启状态
            }

            if (account.getCashCollMode() == 1) { //继承
                selectCondition.setBelongPayAccountId(0); //查询系统全局配置
            } else {
                selectCondition.setBelongPayAccountId(account.getId()); //查询子账户的特有配置账号
            }

            List<PayCashCollConfig> configList = rpcCommonService.rpcPayCashCollConfigService.selectAll(selectCondition);
            if (configList == null || configList.size() <= 0) {
                return null;
            }

            List<Pair> pollPayPassList = new LinkedList<>();
            for (PayCashCollConfig item : configList) {
                int pid = item.getId();
                int weight = item.getTransInPercentage().intValue();
                Pair pair = new Pair(pid, weight);
                pollPayPassList.add(pair);
            }

            WeightRandom weightRandom = new WeightRandom(pollPayPassList);
            int id = (Integer) weightRandom.random();
            PayCashCollConfig model = rpcCommonService.rpcPayCashCollConfigService.findById(id);
            return model;
        } catch (Exception ex) {
            _log.info("获取现金红包收款账号异常，异常信息：" + ex.getMessage());
            return null;
        }
    }

    /**
     * 现金红包拆包
     * <p>
     * 以下金额计算，都是以分为单位
     *
     * @param payOrder
     * @return
     */
    protected Long CashUnpacking(PayOrder payOrder) {
        SysConfig sysConfig = rpcCommonService.sysConfigService.findCode("redenvelope");

        Long unitAmount = sysConfig == null ? new Long(400) : new Long(sysConfig.getValue());

        Map<String, Object> map = new HashMap<>();
        map.put("payOrderId", payOrder.getPayOrderId());
        map.put("status", "1");
        Map successMap = rpcCommonService.rpcPayOrderCashCollRecordService.transSuccess(map);
        Long amount = new Long(0);
        if (successMap != null) {
            amount = new Long(successMap.get("transSuccessAmount").toString());  //红包领取成功金额
        }

        //订单金额-已领取金额=剩余未分红包金额
        Long remainingAmount = payOrder.getAmount().longValue() - amount.longValue();//红包剩余未领取金额

        //剩余未分红包金额-基础单位400
        Long currentAmount = remainingAmount - unitAmount;
        if (currentAmount >= 0) {
            //如果减去基础数值400 还大于0,标识未领取的红包金额大于阈值，如果大于阈值.就按阈值的金额进行红包分发
            currentAmount = unitAmount;
        } else {
            //如果小于0 则表示剩余未领取的红包金额少于阈值，得到的是负数，取绝对值
            currentAmount = Math.abs(currentAmount);
        }

        return currentAmount;
    }


    protected Boolean ChekCashUnpcaking(PayOrder payOrder) {
        Map<String, Object> map = new HashMap<>();
        map.put("payOrderId", payOrder.getPayOrderId());
        map.put("status", "1");
        Map successMap = rpcCommonService.rpcPayOrderCashCollRecordService.transSuccess(map);
        Long amount = new Long(0);
        if (successMap != null) {
            amount = new Long(successMap.get("transSuccessAmount").toString());  //红包领取成功金额
        }

        return payOrder.getAmount().longValue() == amount.longValue();
    }
}
