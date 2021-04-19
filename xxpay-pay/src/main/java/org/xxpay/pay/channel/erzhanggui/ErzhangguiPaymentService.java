package org.xxpay.pay.channel.erzhanggui;

import com.alibaba.fastjson.JSONObject;
import org.jsoup.helper.StringUtil;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class ErzhangguiPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(ErzhangguiPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_ERZHANGGUI;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {

            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doHuaFeiPayReq(payOrder, "0"); //pay.alipay.h5 支付宝H5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doHuaFeiPayReq(PayOrder payOrder, String channel) {
        String logPrefix = "【二掌柜支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();

            if (!StringUtil.isBlank(channelPayConfig.getmD5Key())) {
                channel = channelPayConfig.getmD5Key();
            }
            map.put("uid", channelPayConfig.getMchId());// 商家号
            map.put("amount", AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount()))); //支付金额
            map.put("pay_type", channel); //支付产品ID
            map.put("order_id", payOrder.getPayOrderId()); //商户订单号
            map.put("notify_url", payConfig.getNotifyUrl(getChannelName())); //支付结果后台回调URL
            map.put("version", "2"); //版本
            String string = XXPayUtil.mapToString(map);
            RsaErzhangui rsaErzhangui = new RsaErzhangui();
            String sign = rsaErzhangui.sign(string, channelPayConfig.getRsaprivateKey());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign ); //签名
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            JSONObject payParams = new JSONObject();
            StringBuffer jumpForm = new StringBuffer();
            jumpForm.append("<form id=\"payForm\" name=\"payForm\" action=\""+channelPayConfig.getReqUrl() +"/pay\" method=\"post\">");
            jumpForm.append("<input type=\"hidden\" name=\"uid\" id=\"uid\" value=\""+map.get("uid")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"amount\" id=\"amount\" value=\""+map.get("amount")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"pay_type\" id=\"pay_type\" value=\""+map.get("pay_type")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"order_id\" id=\"order_id\" value=\""+map.get("order_id")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"notify_url\" id=\"notify_url\" value=\""+map.get("notify_url")+"\">");
            jumpForm.append("<input type=\"hidden\" name=\"version\" id=\"version\" value=\""+map.get("version")+"\">");
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
        String logPrefix = "【二掌柜支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("uid", channelPayConfig.getMchId());// 商家号
            map.put("order_id", payOrder.getPayOrderId()); //商户订单号
            map.put("version", "2"); //版本号

            String string = XXPayUtil.mapToString(map);
            RsaErzhangui rsaErzhangui = new RsaErzhangui();
            String sign = rsaErzhangui.sign(string, channelPayConfig.getRsaprivateKey());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", URLEncoder.encode(sign,"UTF-8")); //签名

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);

            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/pay/query", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("respcode");
            String retMsg = resObj.getString("respmsg");
            if (retCode.equals("00")) {
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
            case -4:
                return "修复失败";
            case -3:
                return "通知失败";
            case -2:
                return "支付失败";
            case -1:
                return "未支付";
            case 0:
                return "排队中";
            case 1:
                return "支付中";
            case 2:
                return "已支付";
            case 3:
                return "通知中";
            case 4:
                return "通知成功";
            case 5:
                return "待修复";
            default:
                return "交易失败";
        }
    }
}
