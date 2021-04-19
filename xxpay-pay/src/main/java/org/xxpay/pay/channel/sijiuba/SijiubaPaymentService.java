package org.xxpay.pay.channel.sijiuba;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.net.URLDecoder;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class SijiubaPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(SijiubaPaymentService.class);

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
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAYZXQR:
                retObj = doHuaFeiPayReq(payOrder, "2");//pay.weixin.scan微信扫码
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【498支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));


            if (!StringUtil.isBlank(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }
            //登录
           /* JSONObject login=new JSONObject(16,true);
            login.put("a","login");
            login.put("ou",channelPayConfig.getRsaPublicKey());
            login.put("op",channelPayConfig.getRsaprivateKey());
            login.put("tn",)*/

            JSONObject payCode=new JSONObject(16,true);
            payCode.put("a","payCode");
            payCode.put("i",channelPayConfig.getRsaprivateKey());
            payCode.put("j", AmountUtil.convertCent2Dollar(String.valueOf(payOrder.getAmount())));
            payCode.put("q",channel);
            payCode.put("tn","0002");
            payCode.put("t","3");
            payCode.put("z","1");
            payCode.put("oo",payOrder.getPayOrderId());
            payCode.put("notify_url",payConfig.getNotifyUrl(getChannelName()));
           /* SortedMap map = new TreeMap();
            map.put("mchId", channelPayConfig.getMchId());// 商家号
            map.put("appId", channelPayConfig.getRsapassWord()); //应用ID
            map.put("productId", channel); //支付产品ID
            map.put("mchOrderNo", payOrder.getPayOrderId()); //商户订单号
            map.put("currency", "cny"); //币种
            map.put("amount", String.valueOf(payOrder.getAmount())); //支付金额
            map.put("notifyUrl", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("subject", "支付"); //商品主题
            map.put("body", "韩信支付"); //商品描述信息
            map.put("extra", ""); //附加参数*/
            _log.info(logPrefix + "******************json:{}", payCode.toJSONString());

            String  sign = Base64Utils.encode(RSASHA256withRSAUtils.encryptByPublicKey(payCode.toJSONString().getBytes(), channelPayConfig.getRsaPublicKey()));
            _log.info(logPrefix + "******************sign:{}", sign);


            String res = XXPayUtil.doGetQueryCmd(channelPayConfig.getReqUrl() + "?s="+sign+channelPayConfig.getMchId());
            _log.info(logPrefix + "******************上游返回数据:{}", res);

            JSONObject resObj = JSONObject.parseObject(res);
            String resultCode = resObj.getString("f");
            String retMsg = resObj.getString("m");

            if ("1".equals(resultCode)) {
                String signs = resObj.getString("sign");
                resObj.remove("sign");
                // md5 加密
                String md5 = PayDigestUtil.getSign(resObj, channelPayConfig.getmD5Key());
                if (md5.equals(signs)) {
                    String payJumpUrl = resObj.getJSONObject("payParams").getString("payUrl");
                    JSONObject payParams = new JSONObject();
                    if (channelPayConfig.getRsaprivateKey()!=null && channelPayConfig.getRsaprivateKey().equals("1")) {
                        payParams.put("payJumpUrl", URLDecoder.decode(payJumpUrl, "UTF-8"));
                    }else{
                        payParams.put("payJumpUrl", payJumpUrl);
                    }

                    if (payOrder.getChannelId().contains("SDK")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    } else if (payJumpUrl.contains("form")) {
                        //表单跳转
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
                    payInfo.put("payParams", payParams);
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getString("order_no"));
                    _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                } else {
                    _log.error("验签失败，payOrderId={},res={}", payOrder.getPayOrderId(), res);
                    payInfo.put("errDes", "下单失败[验签失败]");
                    payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                    return payInfo;
                }
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
        String logPrefix = "【498支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mchId", channelPayConfig.getMchId());// 商家号
            map.put("appId", channelPayConfig.getRsapassWord()); //应用ID
            map.put("mchOrderNo", payOrder.getPayOrderId()); //商户订单号
            map.put("executeNotify", "false"); //是否执行回调

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/api/pay/query_order", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("retCode");
            String retMsg = resObj.getString("retMsg");
            if (retCode.equals("SUCCESS")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(resObj.getInteger("status")) + "");
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
