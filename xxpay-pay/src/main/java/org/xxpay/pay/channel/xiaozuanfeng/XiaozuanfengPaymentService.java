package org.xxpay.pay.channel.xiaozuanfeng;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.net.URLEncoder;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class XiaozuanfengPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(XiaozuanfengPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_SHUNFENG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doHuaFeiPayReq(payOrder, "weixin_wap"); //pay.weixin.h5 微信H5
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doHuaFeiPayReq(payOrder, "alipay_wap"); //pay.alipay.h5 支付宝H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【顺丰支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            //公共参数
            map.put("partner_id", channelPayConfig.getmD5Key());// pid
            map.put("service", "com.order.Unified.Pay");
            map.put("sign_type","RSA");
            map.put("rand_str", RandomStringUtils.randomAlphanumeric(32));
            map.put("version", "v1");
            map.put("merchant_no", channelPayConfig.getMchId());



            if (!StringUtil.isBlank(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));

            map.put("merchant_order_sn", payOrder.getPayOrderId());
            map.put("paychannel_type", channel); //支付产品ID
            map.put("trade_amount", String.valueOf(payOrder.getAmount())); //支付金额
            map.put("merchant_notify_url", payConfig.getNotifyUrl(channelName)); //支付结果后台回调URL
            map.put("merchant_return_url", payConfig.getReturnUrl(channelName)); //支付结果后台回调URL


            map.put("ord_name", "支付"); //商品主题
            map.put("interface_type", "1"); //商户订单号
            map.put("client_ip","127.0.0.1");
            String sendM = XXPayUtil.mapToString(map);
            String sign = RSASHA256withRSAUtils.sign(sendM.getBytes(), channelPayConfig.getRsaprivateKey());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}",sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/v2", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);


            JSONObject resObj = JSONObject.parseObject(res);
            String errcode = resObj.getString("errcode");
            String msg = resObj.getString("msg");

            if ("0".equals(errcode)) {
                    String payJumpUrl = resObj.getJSONObject("data").getString("out_pay_url");
                    JSONObject payParams = new JSONObject();
                    payParams.put("payJumpUrl", payJumpUrl);
                    if(payOrder.getChannelId().contains("SDK")){
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    }else if(payJumpUrl.contains("form"))
                    {
                        //表单跳转
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    }else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
                    payInfo.put("payParams",payParams);
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_sn"));
                    _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);

            } else {
                payInfo.put("errDes", "下单失败[" + msg + "]");
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
        String logPrefix = "【顺丰支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            //公共参数
            map.put("partner_id", channelPayConfig.getmD5Key());// pid
            map.put("service", "com.order.Trade.Query");
            map.put("sign_type","RSA2");
            map.put("rand_str", RandomStringUtils.randomAlphanumeric(32));
            map.put("version", "v1");
            map.put("merchant_no", channelPayConfig.getMchId());
            map.put("merhant_order_sn", payOrder.getPayOrderId());

            String sendM = XXPayUtil.mapToString(map);
            String sign = RSASHA256withRSAUtils.sign(sendM.getBytes("UTF-8"), channelPayConfig.getRsaprivateKey());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/v2",  sendMsg);
            _log.info("上游返回信息：" + res);

            JSONObject resObj = JSONObject.parseObject(res);
            String errcode = resObj.getString("errcode");
            String msg = resObj.getString("msg");
            if ("0".equals(errcode)) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + errcode + ",订单状态:" + msg+ "");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put("msg", "查询系统：请求上游发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }

    private  String GetStatusMsg(int code)
    {
        switch (code)
        {
            case  0: return  "订单生成";
            case  1: return  "支付中";
            case  2: return  "支付成功";
            case  3: return  "业务处理完成";
            default: return  "交易失败";
        }
    }
}
