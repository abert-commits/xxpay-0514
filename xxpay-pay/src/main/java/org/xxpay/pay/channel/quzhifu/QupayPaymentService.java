package org.xxpay.pay.channel.quzhifu;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class QupayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(QupayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_QUZHIFU;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doPayReq(payOrder, "zfbwap");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_UnionPay_WAP:
                retObj = doPayReq(payOrder, "bank");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doPayReq(payOrder, "zfbbank");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAYZXQR:
                retObj = doPayReq(payOrder, "wxbank");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doPayReq(payOrder, "wxwap");
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doPayReq(payOrder, "zfbwap");
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【趣支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map map = new HashMap();
            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            map.put("fxid", channelPayConfig.getMchId());//商户编号
            map.put("fxddh", payOrder.getPayOrderId());//商户订单号
            map.put("fxdesc","HXGoodsName");//
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("fxfee", String.valueOf(amount));// 单位 元
            map.put("fxnotifyurl", payConfig.getNotifyUrl(channelName));
            map.put("fxbackurl", payConfig.getReturnUrl(channelName));
            if (StringUtils.isNotBlank(channelPayConfig.getRsapassWord())) {
                channel = channelPayConfig.getRsapassWord();
            }
            map.put("fxpay", channelPayConfig.getChannelId());//支付渠道
//            map.put("fxnotifystyle", "1");
            map.put("fxip","47.113.193.69");
            map.put("fxuserid","127.0.0.1");

            String signStr= MessageFormat.format("{0}{1}{2}{3}{4}",channelPayConfig.getMchId(),payOrder.getPayOrderId(),map.get("fxfee"),map.get("fxnotifyurl"),channelPayConfig.getmD5Key());

            String sign = PayDigestUtil.md5(signStr, "UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("fxsign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("status");
            if (retCode.equals("1")) {
                String payJumpUrl = resObj.getString("payurl");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);
                if (payOrder.getChannelId().contains("SDK")) {
                    String payJumpUrls  = URLDecoder.decode(resObj.getString("sdk_url"),"UTF-8");
                    payParams.put("payJumpUrl", payJumpUrls);
                    //SDK跳转
                    payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                } else if (payOrder.getChannelId().contains("QR")) {
                    //二维码
                    payParams.put("payMethod", PayConstant.PAY_METHOD_CODE_IMG);
                } else {
                    //表单跳转
                    if(payJumpUrl.contains("<html"))
                    {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    }else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
                }

                payInfo.put("payParams", payParams);
                int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), "");
                _log.info("[{}]更新订单状态为支付中:payOrderId={},prepayId={},result={}", getChannelName(), payOrder.getPayOrderId(), "", result);
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            } else {
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + resObj.getString("msg") + "]");
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
        String logPrefix = "【趣支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("fxid", channelPayConfig.getMchId());//商户编号
            map.put("fxtype", "1");//
            map.put("fxorder", payOrder.getPayOrderId());//

            String signStr=MessageFormat.format("{0}{1}{2}{3}",map.get("fxid"),map.get("fxorder"),map.get("fxtype"),channelPayConfig.getmD5Key());
            String sign = PayDigestUtil.md5(signStr,"UTF-8");
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay/queryorder/dfcx", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("result");
            String retMsg = resObj.getString("error");
            String retStatus = resObj.getString("status");
            if (retCode.equals("SUCCESS")) {
                retObj.put("status", "2");
                retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + GetStatusMsg(retStatus) + "");
            } else {
                retObj.put("status", "1");
                retObj.put("msg", "响应Code:" + retCode + ",订单状态:" + retMsg + "");
            }
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }


    //    0：待接单，1：已接单，2：已确认，3：已取消, 4：异常订单
    private String GetStatusMsg(String code) {
        switch (code) {
            case "0":
                return "未支付";
            case "1":
                return "支付成功";
            case "2":
                return "提现成功";
            case "3":
                return "提现等待";
            case "4":
                return "提现驳回";
            default:
                return "异常订单";
        }
    }
}
