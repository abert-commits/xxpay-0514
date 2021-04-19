package org.xxpay.pay.channel.lgpay;

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
public class LgpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(LgpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_LG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doJuheRyPayReq(payOrder, "1"); //支付宝拼多多
                break;
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_WXPAY:
                retObj = doJuheRyPayReq(payOrder, "3"); //微信h5
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doJuheRyPayReq(PayOrder payOrder, String pay_code) {
        String logPrefix = "【LG支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            String channelName = payOrder.getChannelId().substring(0, payOrder.getChannelId().indexOf("_"));
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String amount = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            map.put("merchantsNo", channelPayConfig.getMchId());//商户号
            map.put("orderNo", payOrder.getPayOrderId());//订单号
            map.put("referTime", String.valueOf(System.currentTimeMillis())); //请求13位时间戳
            if (StringUtils.isNotBlank(channelPayConfig.getRsapassWord())) {
                pay_code = channelPayConfig.getRsapassWord();
            }
            map.put("payCode", pay_code); //银行编码
            map.put("serverUrl", payConfig.getNotifyUrl(channelName)); //异步通知地址
            map.put("returnUrl", payConfig.getReturnUrl(channelName)); //页面跳转返回地址
            map.put("amount", String.valueOf(amount));// 金额

            String sginString = XXPayUtil.mapToString(map).replace(">", "");
            String sign = RSASHA256withRSAUtils.sign(sginString.getBytes("UTF-8"), channelPayConfig.getRsaprivateKey());

            map.put("sign", sign);// md5签名
            map.put("goodsName", "手机支付");//商品名称
            map.put("returnType", "html");//返回类型
            _log.info(logPrefix + "******************sign:{}", sign);
            _log.info(logPrefix + "******************sendMsg:{}", map.toString());

            JSONObject jsonObject = new JSONObject(map);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/paididx/index", sendMsg);
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            _log.info("上游返回信息：" + res);
            if (res.contains("msg")) {
                JSONObject resObj = JSONObject.parseObject(res);
                String retMsg = resObj.getString("msg");
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[" + retMsg + "]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }
            JSONObject payParams = new JSONObject();
            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            payParams.put("payJumpUrl", res);
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
        String logPrefix = "【LG支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("merchantsNo", channelPayConfig.getMchId());//商户号
            map.put("orderNo", payOrder.getPayOrderId());//订单号

            String sginString = XXPayUtil.mapToString(map).replace(">", "");
            String sign = RSASHA256withRSAUtils.sign(sginString.getBytes("UTF-8"), channelPayConfig.getRsaprivateKey());
            _log.info(logPrefix + "******************sign:{}", sign);

            map.put("sign", sign);
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            JSONObject jsonObj = new JSONObject(map);
            String res = (String) XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/paidtrd/query", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String status1 = resObj.getString("status");
            if (status1.equals("10000")) {
                String ressign = resObj.getString("sign");
                //验签
                SortedMap map1 = new TreeMap();
                map1.put("merchantsNo", resObj.getString("merchantsNo"));//商户编号
                map1.put("transactionNo", resObj.getString("transactionNo"));//平台订单号
                map1.put("status", resObj.getString("status"));//平台订单号
                map1.put("payStatus", resObj.getString("payStatus"));//平台订单号
                map1.put("orderNo", resObj.getString("orderNo"));//平台订单号
                map1.put("amount", resObj.getString("amount"));//订单金额

                String sginString1 = XXPayUtil.mapToString(map1).replace(">", "");

                boolean verify = RSASHA256withRSAUtils.verify(sginString1.getBytes(), channelPayConfig.getRsaPublicKey(), ressign);
                //核对验签
                if (!verify) {
                    retObj.put("status", "1");
                    retObj.put("msg", "响应Code:" + status1 + "上游查询返回验签失败！");
                    retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                    return retObj;
                }
            }
            if (resObj.getString("payStatus").equals("Y")) {
                retObj.put("status", "2");
            } else {
                retObj.put("status", "1");
            }

            String status = resObj.getString("status");
            retObj.put("msg", "响应Code:" + status + ",订单状态:" + GetStatusMsg(resObj.getString("payStatus")) + "");
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
            case "Y":
                return "已支付";
            default:
                return "未支付";
        }
    }


}
