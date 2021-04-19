package org.xxpay.pay.channel.mayi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.TransOrder;
import org.xxpay.pay.channel.BaseTrans;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.channel.ChannelTransConfig;
import org.xxpay.pay.mq.Mq4TransQuery;

import java.net.URLEncoder;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class MayipayTransService extends BaseTrans {

    private static final MyLog _log = MyLog.getLog(MayipayTransService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_MAYIPAY;
    }

    @Autowired
    private Mq4TransQuery mq4TransQuery;

    @Override
    public JSONObject trans(TransOrder transOrder) {

        String logPrefix = "【蚂蚁代付】";
        ChannelTransConfig channelTransConfig = new ChannelTransConfig(getTransParam(transOrder));
        JSONObject retObj = buildRetObj();
        String transOrderId = transOrder.getTransOrderId();

        SortedMap<String, String> postParamMap = new TreeMap<String, String>();
        postParamMap.put("memberid", transOrder.getMchId().toString());
        postParamMap.put("orderid", transOrderId);
        postParamMap.put("payAmount", AmountUtil.convertCent2Dollar(transOrder.getAmount().toString())); //单位元
        postParamMap.put("cardHolder", transOrder.getAccountName());
        postParamMap.put("notifyUrl", payConfig.getNotifyTransUrl(getChannelName()));
        postParamMap.put("bankName", transOrder.getBankName());
        postParamMap.put("bankProvince", transOrder.getProvince());
        postParamMap.put("bankCity", transOrder.getCity());
        postParamMap.put("bankCardNo", transOrder.getAccountNo());
        postParamMap.put("bankCode", transOrder.getBankCode());
        postParamMap.put("bankBranchName", transOrder.getBankName());
        postParamMap.put("send_type", "D0");
        String sign = null;
        String res = "";
        try {
            String md5sign = PayDigestUtil.getSign(postParamMap, channelTransConfig.getmD5Key()).toUpperCase();
            _log.info(logPrefix + "******************sign:{}", md5sign);
            sign = URLEncoder.encode(MyRSAUtils.sign(channelTransConfig.getRsaprivateKey(), md5sign, "SHA1withRSA"), "UTF-8");
            sign = sign.replaceAll("\r|\n", "");
            String postString = XXPayUtil.mapToString(postParamMap) + "&sign=" + sign;
            System.out.println("代付提交数据：" + postString.toString());
            res = XXPayUtil.doPostQueryCmd(channelTransConfig.getReqUrl()+"Pay/Ahpay/dfPay", postString);
            if (StringUtils.isBlank(res)) {
                _log.info("{} >>> 请求蚂蚁没有响应,将转账转为失败", logPrefix);
                retObj.put("status", 3);    // 失败
            } else {
                JSONObject resultObj = JSON.parseObject(res);
                String retCode = resultObj.getString("retCode");
                String status = resultObj.getString("status");
                String retMsg = resultObj.getString("retMsg");
                retObj.put("channelErrCode", retCode);
                retObj.put("channelErrMsg", retMsg);
                // 0未处理 1处理中，2成功，3失败或拒绝
                if("2".equals(status)) {
                    // 交易成功
                    _log.info("{} >>> 转账成功", logPrefix);
                    retObj.put("transOrderId", transOrderId);
                    retObj.put("status", 2);            // 成功
                    retObj.put("channelOrderNo", resultObj.getString("merchantId"));
                }else if("3".equals(status)) {
                    // 交易失败
                    _log.info("{} >>> 转账失败", logPrefix);
                    retObj.put("status", 3);    // 失败
                }else if("1".equals(status)) {
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
        } catch (Exception e) {
            _log.error(e, "转账异常");
            retObj = buildFailRetObj();
            return retObj;
        }
    }

    @Override
    public JSONObject query(TransOrder transOrder) {
        String logPrefix = "【蚂蚁代付查询】";

        return super.query(transOrder);
    }

    @Override
    public JSONObject balance(String payParam) {
        return null;
    }
}
