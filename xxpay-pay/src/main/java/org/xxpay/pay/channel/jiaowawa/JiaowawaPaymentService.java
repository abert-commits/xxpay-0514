package org.xxpay.pay.channel.jiaowawa;

import com.alibaba.fastjson.JSONObject;
import com.sun.javafx.collections.MappingChange;
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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class JiaowawaPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(JiaowawaPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_DAFA;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doHuaFeiPayReq(payOrder, "1038"); //pay.alipay.h5 支付宝 SDK
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doHuaFeiPayReq(payOrder, "1035"); //pay.alipay.h5 支付宝 扫码
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【叫娃娃支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            if (!StringUtil.isBlank(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }
            map.put("userid", channelPayConfig.getMchId());// 商户ID
            map.put("orderid", payOrder.getPayOrderId());// 商户订单号
            map.put("money", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount())));// 订单金额
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("url", payConfig.getNotifyUrl(channelName));// 商户接收后台返回储值结果的地址
            map.put("bankid", channel);// 银行编号

            String sign = "userid=" + channelPayConfig.getMchId() + "&orderid=" +
                    payOrder.getPayOrderId() + "&bankid=" + channel + "&money=" + map.get("money") + "&keyvalue=" + channelPayConfig.getmD5Key();

            sign = PayDigestUtil.md5(sign, "utf-8");
            map.put("sign", sign.toLowerCase());// 签名数据
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doGetQueryCmd(channelPayConfig.getReqUrl() + "/pay.aspx?" + sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            if (res.contains("service") || res.contains("bizcontext") || res.contains("window.location")) {
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", res);
                if (payOrder.getChannelId().contains("SDK")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else if (res.contains("form") || res.contains("window.location")) {
                    //表单跳转
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }


                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "下单失败[" + res + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }
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
        String logPrefix = "【叫娃娃支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("userid", channelPayConfig.getMchId());// 商家号
            map.put("orderid", payOrder.getPayOrderId()); //商户订单号

            String sign = "userid=" + channelPayConfig.getMchId() + "&orderid=" + payOrder.getPayOrderId() + "&keyvalue=" + channelPayConfig.getmD5Key();
            sign = PayDigestUtil.md5(sign, "utf-8").toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doGetQueryCmd(channelPayConfig.getReqUrl() + "/payquery.aspx?" + sendMsg);
            _log.info("上游返回信息：" + res);
            Map resObj = XXPayUtil.convertParamsString2Map(res);
            String retCode = (String) resObj.get("returncode");
            if (retCode.equals("1")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg((String) resObj.get("returncode")) + "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private String GetStatusMsg(String code) {
        switch (code) {
            case "0":
                return "未支付";
            case "1":
                return "支付成功";
            case "2":
                return "商户不存在";
            case "3":
                return "订单不存在";
            case "4":
                return "签名错误";
            case "5":
                return "系统维护";
            default:
                return "交易失败";
        }
    }
}
