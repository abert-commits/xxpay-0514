package org.xxpay.pay.channel.qinglpay;

import com.alibaba.fastjson.JSONObject;
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
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class QinglongpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(QinglongpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_QINGLONG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doJuheRyPayReq(payOrder, ""); //支付宝拼多多
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doJuheRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【青龙支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("merId", channelPayConfig.getMchId());//商户号
            map.put("terId","202008032146002");//终端号
            map.put("businessOrdid", payOrder.getPayOrderId());//订单号
            map.put("orderName", "韩信支付"); //请求时间
            map.put("tradeMoney", String.valueOf(payOrder.getAmount()));// 金额
            map.put("payType", channelPayConfig.getChannelId()); //银行编码
            map.put("appSence","1001");
            map.put("asynURL", payConfig.getNotifyUrl(channelName)); //异步通知地址
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();

            map.put("sign", sign);// md5签名
//            map.put("selfParam","韩信四方");
            _log.info(logPrefix + "******************sign:{}", sign);

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/gateway/unifiedOrder",sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            if (resObj.getString("code").equals("0")) {
                String payJumpUrl= resObj.getString("data");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payJumpUrl);
                if (payOrder.getChannelId().contains("SDK")) {
                    if (payJumpUrl.contains("form")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    }
                } else {
                    if (payJumpUrl.contains("form")) {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                    } else {
                        payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                    }
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
        String logPrefix = "【青龙支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("merId", channelPayConfig.getMchId());//商户号
            map.put("businessOrdid", payOrder.getPayOrderId());//订单号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            map.put("sign",sign);
            _log.info(logPrefix + "******************sign:{}", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/gateway/query",sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("respCode");
            if (code.equals("1000")) {
                String ressign = resObj.getString("sign");
                //验签
                resObj.remove("sign");
                String signMd5 = PayDigestUtil.getSign(XXPayUtil.JSONObjectToSortedMap(resObj), channelPayConfig.getmD5Key()).toLowerCase();
                //核对验签
                if (!signMd5.equals(ressign)) {
                    retObj.put("status", "1");
                    retObj.put("msg", "响应Code:" + code + "上游查询返回验签失败！");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                }
            }
            if (resObj.getString("status").equals("1")) {
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
