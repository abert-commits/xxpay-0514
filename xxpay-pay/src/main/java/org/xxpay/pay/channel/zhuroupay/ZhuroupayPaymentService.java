package org.xxpay.pay.channel.zhuroupay;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ZhuroupayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(ZhuroupayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ZHUROUPAY;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_XJ:
                retObj = doZhuroupayRyPayReq(payOrder, "ALI_HB"); //猪肉支付宝现金红包
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doZhuroupayRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【猪肉支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mch_no", channelPayConfig.getMchId());//商户号
            map.put("app_id", channelPayConfig.getRsaPublicKey()); //应用id
            map.put("nonce_str", String.valueOf(System.currentTimeMillis()));
            if (StringUtils.isNotBlank(channelPayConfig.getRsapassWord())) {
                pay_code = channelPayConfig.getRsapassWord();
            }
            map.put("trade_type", pay_code); //通道类型
            map.put("total_fee", String.valueOf(payOrder.getAmount()));// 金额
            map.put("notify_url", payConfig.getNotifyUrl(channelName));// 回调地址
            map.put("out_trade_no", payOrder.getPayOrderId());// 支付订单号
            map.put("body","韩信支付");//订单描述
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            map.put("sign", sign);// md5签名
            _log.info(logPrefix + "******************sign:{}", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay-api/unified/pay",sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.getString("return_code").equals("0000")) {
                String payJumpUrl = resObj.getString("pay_info");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);
                    if (payJumpUrl.contains("<form>")) {
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
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【猪肉支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mch_no", channelPayConfig.getMchId());//商户号
            map.put("out_trade_no", payOrder.getPayOrderId());//订单号
            map.put("app_id",channelPayConfig.getRsaPublicKey()); //应用id
            map.put("nonce_str",String.valueOf(System.currentTimeMillis()));

            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay-api/unified/query",sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("return_code");
            if (code.equals("0000")) {
                String ressign = resObj.getString("sign");
                //验签
                SortedMap map1 = new TreeMap();
                map1.put("return_code", resObj.getString("return_code"));//商户编号
                map1.put("return_msg", resObj.getString("return_msg"));//平台订单号
                map1.put("mch_no", resObj.getString("mch_no"));//订单金额
                map1.put("app_id", resObj.getString("app_id"));//支付成功时间
                map1.put("nonce_str", resObj.getString("nonce_str"));//交易流水号
                map1.put("trade_type", resObj.getString("trade_type"));//交易状态

                map1.put("total_fee", resObj.getString("total_fee"));//交易状态

                map1.put("trans_no", resObj.getString("trans_no"));//交易状态
                map1.put("out_trade_no", resObj.getString("out_trade_no"));//交易状态
                map1.put("trans_time", resObj.getString("trans_time"));//交易状态
                map1.put("trade_status", resObj.getString("trade_status"));//交易状态
                System.out.println("猪肉------》》》》》》《《《《《《++++++" + map1.toString());
                String signMd5 = PayDigestUtil.getSign(map1, channelPayConfig.getmD5Key());
                //核对验签
                if (!signMd5.equals(ressign)) {
                    retObj.put("status", "1");
                    retObj.put("msg", "响应Code:" + code + "上游查询返回验签失败！");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                }
            }
            if (resObj.getString("trade_status").equals("SUCCESS")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }
            String status=resObj.getString("status");
            retObj.put("msg", "响应Code:" + code + ",订单状态:" + GetStatusMsg(status) + "");
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
                return "订单生成";
            case "1":
                return "支付中";
            case "2":
                return "支付成功";
            case "3":
                return "业务处理完成";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }


}
