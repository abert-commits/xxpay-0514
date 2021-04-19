package org.xxpay.pay.channel.jinxiu;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class JinxiuPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(JinxiuPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_HANXIN;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doJinXiuPayReq(payOrder, ""); //pay.weixin.h5 微信H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doJinXiuPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【锦绣闲鱼统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));

            SortedMap map = new TreeMap();
            map.put("pay_memberid", channelPayConfig.getMchId()); // 商户号
            map.put("pay_orderid", payOrder.getPayOrderId()); // 订单号
            map.put("pay_applydate", DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS)); // 提交时间
            map.put("pay_bankcode", channelPayConfig.getChannelId()); // 银行编码
            map.put("pay_notifyurl", payConfig.getNotifyUrl(channelName)); // 服务端通知
            map.put("pay_callbackurl", payConfig.getReturnUrl(channelName)); // 页面跳转通知
            map.put("pay_amount", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount())));// 金额

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toUpperCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("pay_md5sign", sign); // md5签名
            map.put("pay_productname", "锦绣闲鱼"); //商品名称

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
//            JSONObject jsonObject = new JSONObject(map);
//            String sendMsg = jsonObject.toJSONString();
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay_Index.html", sendMsg, "utf-8", "application/x-www-form-urlencoded");
//            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/pay", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("code");
            String retMsg = resObj.getString("msg");
            if ("200".equals(retCode)) {
                String payJumpUrl = resObj.getString("data");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);
                if (payOrder.getChannelId().contains("SDK")) {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else if (payJumpUrl.contains("form")) {
                    //表单跳转
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), "");
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
        String logPrefix = "【锦绣闲鱼订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("pay_memberid", channelPayConfig.getMchId());//商户号
            map.put("pay_orderid", payOrder.getPayOrderId());//订单号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("pay_md5sign", sign);// md5签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
//            JSONObject jsonObject = new JSONObject(map);
//            String sendMsg = jsonObject.toJSONString();
//            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay_Trade_query.html", sendMsg, "utf-8", "application/x-www-form-urlencoded");
//            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/query/pay", sendMsg);
            _log.info("上游返回信息：" + res);

            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("returncode");
            if (code.equals("00")) {
                retObj.put("status", "2");
                retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(resObj.getString("trade_state")) + "");
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + code + ",订单状态:" + "查询失败" + "");
            }
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    //    0：待接单，1：已接单，2：已确认，3：已取消, 4：异常订单
    private String GetStatusMsg(String code) {
        switch (code) {
            case "NOTPAY":
                return "未支付";
            case "SUCCESS":
                return "已支付";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }
}
