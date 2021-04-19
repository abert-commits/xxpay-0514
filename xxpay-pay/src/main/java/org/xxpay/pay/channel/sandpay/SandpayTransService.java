package org.xxpay.pay.channel.sandpay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.MySeq;
import org.xxpay.core.entity.TransOrder;
import org.xxpay.pay.channel.BaseTrans;
import org.xxpay.pay.channel.sandpay.util.SandHttpUtil;
import org.xxpay.pay.mq.Mq4TransQuery;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author: dingzhiwei
 * @date: 18/04/20
 * @description: 杉德代付接口
 */
@Service
public class SandpayTransService extends BaseTrans {

    private static final MyLog _log = MyLog.getLog(SandpayTransService.class);

    @Autowired
    private Mq4TransQuery mq4TransQuery;

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_SANDPAY;
    }

    @Override
    public JSONObject trans(TransOrder transOrder) {
        String logPrefix = "【杉德实时代付】";
        String transCode = "RTPM";	//实时代付
        JSONObject retObj = buildRetObj();
        try {
            SandpayConfig sandpayConfig = new SandpayConfig(getTransParam(transOrder));
            String url = sandpayConfig.getReqUrl() + "/agentpay";
            String result = null;
            String transOrderId = transOrder.getTransOrderId();
            retObj.put("transOrderId", transOrderId);
            try {
                String reqData = buildAgentPayRequest(transOrder, sandpayConfig);
                _log.info("{}请求数据:{}", logPrefix, reqData);
                result = this.post(sandpayConfig, url, transCode, reqData);
                _log.info("{}响应结果:{}", logPrefix, result);

            } catch (Exception e) {
                _log.error(e, "转账失败");
            }
            if(StringUtils.isBlank(result)) {
                _log.info("{} >>> 请求杉德没有响应,将转账转为失败", logPrefix);
                retObj.put("status", 3);    // 失败
            }else {
                JSONObject resultObj = JSON.parseObject(result);
                String respCode = resultObj.getString("respCode");
                String resultFlag = resultObj.getString("resultFlag");
                String respDesc = resultObj.getString("respDesc");
                retObj.put("channelErrCode", respCode);
                retObj.put("channelErrMsg", respDesc);
                // 0-成功 1-失败 2-处理中
                if("0".equals(resultFlag)) {
                    // 交易成功
                    _log.info("{} >>> 转账成功", logPrefix);
                    retObj.put("transOrderId", transOrderId);
                    retObj.put("status", 2);            // 成功
                    retObj.put("channelOrderNo", resultObj.getString("sandSerial"));
                }else if("1".equals(resultFlag)) {
                    // 交易失败
                    _log.info("{} >>> 转账失败", logPrefix);
                    retObj.put("status", 3);    // 失败
                }else if("2".equals(resultFlag)) {
                    // 交易处理中
                    _log.info("{} >>> 转账处理中", logPrefix);
                    retObj.put("status", 1);    // 处理中
                    JSONObject msgObj = new JSONObject();
                    msgObj.put("count", 1);
                    msgObj.put("transOrderId", transOrderId);
                    msgObj.put("channelName", getChannelName());
                    mq4TransQuery.send(msgObj.toJSONString(), 10 * 1000);  // 10秒后查询
                }else {
                    // 交易处理中,若是杉德返回其他状态,也应认为是处理中
                    // 不确定时不能轻易认为是成功或失败
                    _log.info("{} >>> 转账处理中", logPrefix);
                    retObj.put("status", 1);    // 处理中
                }
            }
            return retObj;
        }catch (Exception e) {
            _log.error(e, "转账异常");
            retObj = buildFailRetObj();
            return retObj;
        }
    }

    public JSONObject query(TransOrder transOrder) {
        String logPrefix = "【杉德代付查询】";
        String transCode = "ODQU";	//订单查询
        JSONObject retObj = buildRetObj();
        try{
            SandpayConfig sandpayConfig = new SandpayConfig(getTransParam(transOrder));
            String url = sandpayConfig.getReqUrl();
            url += "/queryOrder";
            String result;
            String transOrderId = transOrder.getTransOrderId();
            String reqData = buildQueryOrderRequest(transOrder, sandpayConfig);
            _log.info("{}请求数据:{}", logPrefix, reqData);
            result = this.post(sandpayConfig, url, transCode, reqData);
            _log.info("{}返回结果:{}", logPrefix, result);
            if(StringUtils.isBlank(result)) {
                _log.info("{} >>> 请求杉德没有响应,将转账转为失败", logPrefix);
                retObj.put("status", 3);    // 失败
            }else {
                JSONObject resultObj = JSON.parseObject(result);
                String resultFlag = resultObj.getString("resultFlag");
                retObj.put("channelErrCode", resultObj.getString("origRespCode"));
                retObj.put("channelErrMsg", resultObj.getString("origRespDesc"));
                retObj.put("channelObj", resultObj);
                // 0-成功 1-失败 2-处理中
                if("0".equals(resultFlag)) {
                    // 交易成功
                    _log.info("{} >>> 转账成功", logPrefix);
                    retObj.put("transOrderId", transOrderId);
                    retObj.put("status", 2);            // 成功
                    retObj.put("channelOrderNo", resultObj.getString("sandSerial"));
                }else if("1".equals(resultFlag)) {
                    // 交易失败
                    _log.info("{} >>> 转账失败", logPrefix);
                    retObj.put("status", 3);    // 失败
                    retObj.put("channelOrderNo", resultObj.getString("sandSerial"));
                }else if("2".equals(resultFlag)) {
                    // 交易处理中
                    _log.info("{} >>> 转账处理中", logPrefix);
                    retObj.put("status", 1);    // 处理中
                }else {
                    // 交易处理中,若是杉德返回其他状态,也应认为是处理中
                    // 不确定时不能轻易认为是成功或失败
                    _log.info("{} >>> 转账处理中", logPrefix);
                    retObj.put("status", 1);    // 处理中
                }
            }
            return retObj;
        }catch (Exception e) {
            _log.error(e, "杉德代付查询异常");
            retObj = buildFailRetObj();
            return retObj;
        }
    }

    @Override
    public JSONObject balance(String payParam) {
        String logPrefix = "【杉德余额查询】";
        String transCode = "MBQU";	//余额查询
        JSONObject retObj = buildRetObj();
        try {
            SandpayConfig sandpayConfig = new SandpayConfig(payParam);
            String url = sandpayConfig.getReqUrl();
            url += "/queryBalance";
            String result;
            String reqData = buildAgentBalanceRequest();
            _log.info("{}请求数据:{}", logPrefix, reqData);
            result = this.post(sandpayConfig, url, transCode, reqData);
            _log.info("{}返回结果:{}", logPrefix, result);
            if(StringUtils.isBlank(result)) {
                _log.info("{} >>> 请求杉德返回结果为空", logPrefix);
            }else {
                JSONObject resultObj = JSON.parseObject(result);
                String respCode = resultObj.getString("respCode");
                String respDesc = resultObj.getString("respDesc");
                if("0000".equals(respCode)) {
                    String balance = resultObj.getString("balance");
                    balance = balance.replace("+", "").replace("-", "");
                    retObj.put("cashBalance", new BigDecimal(balance).divide(new BigDecimal(100)));           // 余额
                }else {
                    return buildFailRetObj();
                }
            }
            return retObj;
        }catch (Exception e) {
            _log.error(e, "查询余额异常");
            return buildFailRetObj();
        }
    }

    /**
     * 构建杉德代付请求数据
     * @param transOrder
     * @param sandpayConfig
     * @return
     */
    String buildAgentPayRequest(TransOrder transOrder, SandpayConfig sandpayConfig) {
        JSONObject jsonObject = new JSONObject();
        // 版本号
        jsonObject.put("version", "01");
        // 产品ID
        jsonObject.put("productId", "00000004");
        // 交易时间
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        jsonObject.put("tranTime", df.format(new Date()));
        // 订单号
        jsonObject.put("orderCode", transOrder.getTransOrderId());
        // 订单超时时间,不填默认24小时
        // jsonObject.put("timeOut", "20161024120000");
        // 金额,精确到分
        jsonObject.put("tranAmt", String.format("%12d", transOrder.getAmount()).replace(" ", "0"));
        // 币种,默认156
        jsonObject.put("currencyCode", "156");
        // 账户属性:0-对私(默认), 1-对公
        jsonObject.put("accAttr", transOrder.getAccountAttr());
        // 账号类型:3-公司账户,4-银行卡
        jsonObject.put("accType", "4");
        // 收款人账户号
        jsonObject.put("accNo", transOrder.getAccountNo());
        // 收款人账户名
        jsonObject.put("accName", transOrder.getAccountName());

//		jsonObject.put("provNo", "sh");
//		jsonObject.put("cityNo", "sh");
//		jsonObject.put("bankName", "cbc");
//		jsonObject.put("bankType", "1");
        // 摘要,银行要去必填
        jsonObject.put("remark", transOrder.getMchId());
        //jsonObject.put("reqReserved", "请求方保留测试");
        System.out.println("json:"+ jsonObject.toJSONString());
        return jsonObject.toJSONString();
    }

    /**
     * 构建杉德订单查询请求数据
     * @param transOrder
     * @param sandpayConfig
     * @return
     */
    String buildQueryOrderRequest(TransOrder transOrder, SandpayConfig sandpayConfig) {
        JSONObject jsonObject = new JSONObject();
        // 版本号
        jsonObject.put("version", "01");
        // 产品ID
        jsonObject.put("productId", "00000004");
        // 交易时间
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        jsonObject.put("tranTime", df.format(new Date()));
        // 订单号
        jsonObject.put("orderCode", transOrder.getTransOrderId());

        System.out.println("json:"+ jsonObject.toJSONString());
        return jsonObject.toJSONString();
    }

    /**
     * 构建杉德余额查询请求数据
     * @return
     */
    String buildAgentBalanceRequest() {
        JSONObject jsonObject = new JSONObject();
        // 版本号
        jsonObject.put("version", "01");
        // 产品ID
        jsonObject.put("productId", "00000004");
        // 交易时间
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        jsonObject.put("tranTime", df.format(new Date()));
        jsonObject.put("orderCode", MySeq.getSeqString());
        jsonObject.put("channelType", "");
        jsonObject.put("extend", "");
        return jsonObject.toJSONString();
    }

    /**
     * 发送杉德http请求
     * @param sandpayConfig
     * @param url
     * @param transCode
     * @param reqData
     * @return
     * @throws Exception
     */
    public String post(SandpayConfig sandpayConfig, String url, String transCode, String reqData) throws Exception {
        String publicKeyPath = payConfig.getCertRootPath() + File.separator + getChannelName() + File.separator + sandpayConfig.getPublicKeyFile();
        String privateKeyPath = payConfig.getCertRootPath() + File.separator + getChannelName() + File.separator +  sandpayConfig.getPrivateKeyFile();
        String keyPassword = sandpayConfig.getPassword();
        SandHttpUtil httpUtil = new SandHttpUtil(publicKeyPath, privateKeyPath, keyPassword);
        return httpUtil.post(url, sandpayConfig.getMchId(), transCode, reqData);
    }

    public static void main(String[] args) {
        String balance = "+000000001000";

        System.out.println(new BigDecimal(balance).divide(new BigDecimal(100)));
        System.out.println(String.format("%12d", 600).replace(" ", "0"));


    }

}
