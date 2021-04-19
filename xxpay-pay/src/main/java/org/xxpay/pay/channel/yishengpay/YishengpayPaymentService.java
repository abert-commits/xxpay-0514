package org.xxpay.pay.channel.yishengpay;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class YishengpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(YishengpayPaymentService.class);

    public static StringBuffer accessToken = new StringBuffer();

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_YISHENG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAYQR:
                retObj = doYishengRyPayReq(payOrder, "1"); //pay.alipay.scan支付宝扫码
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doYishengRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【亿盛支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("mch_id", channelPayConfig.getMchId());//商户号
            if (!StringUtil.isBlank(channelPayConfig.getRsaPublicKey())) {
                pay_code = channelPayConfig.getRsaPublicKey();
            }
            map.put("ptype", pay_code);//支付方式
            map.put("order_sn", payOrder.getPayOrderId());// 商户订单号
            map.put("money", String.valueOf(amount));// 金额
            map.put("goods_desc", "亿盛支付");
            map.put("client_ip", "127.0.0.1");// 客户端ip
            map.put("format", "page"); //接口返回格式
            map.put("notify_url", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("time", String.valueOf(System.currentTimeMillis())); //请求时间戳
            System.out.println(map.toString());
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            _log.info(logPrefix + "******************sendMsg:{}", map.toString());

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            JSONObject payParams = new JSONObject();
            StringBuffer jumpForm = new StringBuffer();
            jumpForm.append("<form id=\"payForm\" name=\"payForm\" action=\""+channelPayConfig.getReqUrl() +"/order/place\" method=\"post\">");
            jumpForm.append("<input type=\"hidden\" name=\"client_ip\" id=\"client_ip\" value=\""+map.get("client_ip")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"format\" id=\"format\" value=\""+map.get("format")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"goods_desc\" id=\"goods_desc\" value=\""+map.get("goods_desc")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"mch_id\" id=\"mch_id\" value=\""+map.get("mch_id")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"money\" id=\"money\" value=\""+map.get("money")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"notify_url\" id=\"notify_url\" value=\""+map.get("notify_url")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"order_sn\" id=\"order_sn\" value=\""+map.get("order_sn")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"ptype\" id=\"ptype\" value=\""+map.get("ptype")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"time\" id=\"time\" value=\""+map.get("time")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"sign\" id=\"sign\" value=\""+sign+"\">");
            jumpForm.append("<input type=\"submit\" value=\"立即支付\" style=\"display:none\" >");
            jumpForm.append("</form>");
            jumpForm.append("<script>document.forms[0].submit();</script>");

            payParams.put("payJumpUrl", jumpForm.toString());
            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
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
        String logPrefix = "【亿盛支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            Map map = new TreeMap();
            map.put("out_order_sn", payOrder.getPayOrderId());// 订单号
            map.put("mch_id", channelPayConfig.getMchId()); // 商户号
            map.put("time",String.valueOf(System.currentTimeMillis()));//时间戳
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key()).toLowerCase();
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/order/query", sendMsg);
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
                System.out.println("亿盛------》》》》》》《《《《《《++++++" + map1.toString());
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
