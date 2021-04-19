package org.xxpay.pay.channel.CC;

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
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class CcPaymentService  extends BasePayment {

    private static final MyLog _log = MyLog.getLog(CcPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_CC;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doHuaFeiPayReq(payOrder, "28"); // 京东网关
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【CC支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            if (!StringUtil.isBlank(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }

            map.put("appID", channelPayConfig.getMchId());// 商家号
            map.put("channel", channel); //支付产品ID
            map.put("partnerNo", payOrder.getPayOrderId()); //商户订单号
            map.put("amount", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount()))); //支付金额 分
            map.put("notifyUrl", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("returnUrl", "http://www.baidu.com");
            String sign = PayDigestUtil.getSignNotKey(map, "&"+channelPayConfig.getmD5Key());

            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            //   /pc_build       /create_order
            String res = XXPayUtil.http(channelPayConfig.getReqUrl() + "/Gateway/Do/Rech", map);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");

            if ("1".equals(resultCode)) {
                String signs = resObj.getString("sign");
                resObj.remove("sign");

                String payJumpUrl = resObj.getString("url");
                payJumpUrl = URLDecoder.decode(payJumpUrl, "UTF-8");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);
                payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_no"));
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);

            } else {
                payInfo.put("errDes", "下单失败[" + retMsg + "]");
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
        String logPrefix = "【CC支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("appID", channelPayConfig.getMchId());// 商家号
            map.put("method", "order.query"); //应用ID
            map.put("partnerNo", payOrder.getPayOrderId()); //商户订单号
            String sign = PayDigestUtil.getSignNotKey(map, "&"+channelPayConfig.getmD5Key());
//            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);



            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.http(channelPayConfig.getReqUrl() + "/Gateway/Do/Index", map);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retstate = resObj.getString("state");
            String retMsg = resObj.getString("msg");
            if (retstate.equals("3")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(resObj.getInteger("state")) + "");
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
                return "订单生成";
            case 1:
                return "支付中";
            case 2:
                return "支付成功";
            case 3:
                return "业务处理完成";
            default:
                return "交易失败";
        }
    }
}


