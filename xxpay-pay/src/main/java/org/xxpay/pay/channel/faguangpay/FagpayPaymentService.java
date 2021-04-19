package org.xxpay.pay.channel.faguangpay;

import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.math.BigDecimal;
import java.util.*;

@Service
public class FagpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(FagpayPaymentService.class);

    public static StringBuffer accessToken = new StringBuffer();

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_FAGUANG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doFagRyPayReq(payOrder, "alipay"); //支付宝H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doFagRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【发光支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map map = new HashMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("version","1.0");// 版本号
            map.put("customerid", channelPayConfig.getMchId());//商户号
            map.put("sdorderno", payOrder.getPayOrderId());// 商户订单号
            map.put("total_fee", String.valueOf(amount));// 金额
            map.put("paytype",pay_code);//支付类型
            map.put("notifyurl", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("returnurl",payConfig.getReturnUrl(channelName));//同步跳转地址

            System.out.println("下单验签串生成+++"+"version="+map.get("version")+"&customerid="+map.get("customerid")+"&total_fee="+map.get("total_fee")+"&sdorderno="+map.get("sdorderno")+"&notifyurl="+map.get("notifyurl")+"&returnurl="+map.get("returnurl")+"&"+channelPayConfig.getmD5Key());
            String sign = PayDigestUtil.md5("version="+map.get("version")+"&customerid="+map.get("customerid")+"&total_fee="+map.get("total_fee")+"&sdorderno="+map.get("sdorderno")+"&notifyurl="+map.get("notifyurl")+"&returnurl="+map.get("returnurl")+"&"+channelPayConfig.getmD5Key(), "utf-8").toLowerCase();
            _log.info(logPrefix + "下单验签串sign:{}", sign);
            System.out.println("发光支付验签串:<<><><><><><"+"1.0"+channelPayConfig.getMchId()+String.valueOf(amount)+payOrder.getPayOrderId()+payConfig.getNotifyUrl(channelName)+
                    payConfig.getReturnUrl(channelName)+channelPayConfig.getmD5Key());
            map.put("sign", sign);
            map.put("remark","11");
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            JSONObject jsonObj = new JSONObject(map);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/apisubmit", sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            if (resObj.getString("code").equals("10000")) {
                String payDinghongUrl = resObj.getString("h5_link");
                JSONObject payParams = new JSONObject();
                payParams.put("payJumpUrl", payDinghongUrl);
                //表单跳转
                if (payDinghongUrl.contains("form")) {
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
        String logPrefix = "【发光支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("out_order_no", payOrder.getPayOrderId());// 订单号
            map.put("merchant", channelPayConfig.getMchId()); // 商户号
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            JSONObject jsonObj = new JSONObject(map);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/pay/query", jsonObj.toJSONString());
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String code = resObj.getString("respCode");

            if (code.equals("0000")) {
                String ressign = resObj.getString("sign");
                //验签
                SortedMap map1 = new TreeMap();
                map1.put("respCode", resObj.getString("respCode"));//商户编号
                map1.put("msg", resObj.getString("msg"));//平台订单号
                map1.put("out_order_no", resObj.getString("out_order_no"));//订单状态
                map1.put("sys_order_no", resObj.getString("sys_order_no"));//订单实际金额
                map1.put("status", resObj.getString("status"));//状态码
                map1.put("money", resObj.getString("money"));//说明
                map1.put("realPrice", resObj.getString("realPrice"));//说明
                System.out.println("发光------》》》》》》《《《《《《++++++" + map1.toString());
                String signMd5 = PayDigestUtil.getSign(map1, channelPayConfig.getmD5Key()).toLowerCase();
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
            case "1":
                return "支付成功";
            case "2":
                return "支付异常";
            case "3":
                return "未支付";
            default:
                return "用户还未完成支付或者支付失败";
        }
    }
}
