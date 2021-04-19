package org.xxpay.pay.channel.longteng;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class LongtengPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(LongtengPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_LONGTENG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doHuaFeiPayReq(payOrder, "1006"); //pay.alipay.h5 支付宝H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【龙腾支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap<String, String> map = new TreeMap<String, String>();

            if (!StringUtil.isBlank(channelPayConfig.getRsaPublicKey())) {
                channel = channelPayConfig.getRsaPublicKey();
            }

            map.put("parter", channelPayConfig.getMchId());// 商家号
            map.put("type", channel); //支付产品ID
            map.put("value", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount()))); //支付金额
            map.put("orderid", payOrder.getPayOrderId()); //商户订单号
            map.put("callbackurl", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("hrefbackurl", payConfig.getReturnUrl(getChannelName())); //商品主题

            String sign = PayDigestUtil.md5(String.format("parter=%s&type=%s&value=%s&orderid=%s&callbackurl=%s",
                    channelPayConfig.getMchId(), channel, map.get("value"), payOrder.getPayOrderId(), map.get("callbackurl"), map.get("hrefbackurl")) + channelPayConfig.getmD5Key(), "utf-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
          /*  StringBuffer payForm = new StringBuffer();

            //String res = XXPayUtil.doGetQueryCmd(channelPayConfig.getReqUrl() + "/gateway/?" + sendMsg);
            payForm.append("<form style=\"display: none\" action=\"" + channelPayConfig.getReqUrl() + "/gateway/?" + sendMsg + "\" method=\"get\">");

            payForm.append("<input type=\"submit\" value=\"立即支付\" style=\"display:none\" >");
            payForm.append("</form>");
            payForm.append("<script>document.forms[0].submit();</script>");*/
            _log.info(logPrefix + "******************生成数据:{}", channelPayConfig.getReqUrl() + "/gateway/?" + sendMsg);
            JSONObject payParams = new JSONObject();
            payParams.put("payJumpUrl",  channelPayConfig.getReqUrl() + "/gateway/?" + sendMsg);
            payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(),null);
            _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);

        } catch (Exception e) {
            _log.error(e, "");
            payInfo.put("errDes", "操作失败!");
            payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return payInfo;
        }
        return payInfo;
    }

    /**
     * 查询订单
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【龙腾支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("parter", channelPayConfig.getMchId());// 商家号
            map.put("orderid", payOrder.getPayOrderId()); //商户订单号

            String sign = PayDigestUtil.md5(String.format("parter=%s&orderid=%s", channelPayConfig.getMchId(), payOrder.getPayOrderId())+channelPayConfig.getmD5Key(), "utf-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doGet(channelPayConfig.getReqUrl() + "/gateway/search.ashx?" + sendMsg);
            _log.info("上游返回信息：" + res);
            Map<String,String > resObj = XXPayUtil.convertParamsString2Map(res);
            String retCode = resObj.get("opstate");
            if (retCode.equals("0")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(Integer.parseInt(retCode))+ "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(int code) {
        switch (code) {
            case 0:
                return "订单支付成功";
            case 3:
                return "验证签名失败";
            case 7:
                return "请求参数有误";
            case 8:
                return "商户有误";
            case 14:
                return "订单号不存在";
            default:
                return "系统错误";
        }
    }
}
