package org.xxpay.pay.channel.zhongyi;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class ZhongyiPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(ZhongyiPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YILIANBAO;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doJuheRyPayReq(payOrder, "913");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doJuheRyPayReq(payOrder, "911");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doJuheRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【中益统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();

            map.put("MemberId", channelPayConfig.getMchId());//商户号
            map.put("OrderId", payOrder.getPayOrderId());//订单号
            map.put("Date", DateUtil.date2Str(new Date()));
            if (StringUtils.isNotBlank(channelPayConfig.getRsapassWord())) {
                pay_code = channelPayConfig.getRsapassWord();
            }
            map.put("PayType", pay_code); //银行编码
            map.put("NotifyUrl", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("CallbackUrl", payConfig.getReturnUrl(channelName)); //页面跳转返回地址
            map.put("Amount", String.valueOf(amount));// 金额

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());

            map.put("Sign", sign);// md5签名
            //map.put("GoodsName", "会员充值");//商品名称
            map.put("ReturnType", "1");//返回类型

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay_Order_create.html"
                    , sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);

            if (!res.contains("success")) {
                String retMsg = resObj.getString("msg");
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }
            JSONObject payParams = new JSONObject();
            if (payOrder.getChannelId().contains("SDK")) {
                String payJumpUrl = resObj.getJSONObject("data").getString("url");
                payParams.put("payJumpUrl", payJumpUrl);
                payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
            } else {
                String payJumpUrl = resObj.getJSONObject("data").getString("url");
                payParams.put("payJumpUrl", payJumpUrl);
                if (payJumpUrl.contains("form")) {
                    //表单跳转
                    payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                } else {
                    payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                }
            }
            payInfo.put("payParams", payParams);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), null);
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
        String logPrefix = "【中益订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("MemberId", channelPayConfig.getMchId());//商户号
            map.put("OrderId", payOrder.getPayOrderId());//订单号

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("Sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay_Trade_query.html", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);

            if (resObj.getString("TradeState").equals("CODE_SUCCESS")) {
                retObj.put("status", "2");
                retObj.put("msg", "订单状态:已支付");

            } else {
                retObj.put("status", "1");
                retObj.put("msg", "订单状态:未支付");
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

}
