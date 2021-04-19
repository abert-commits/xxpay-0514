package org.xxpay.pay.channel.fg;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
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
public class FgpayPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(FgpayPaymentService.class);

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_FG;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY_SDK:
                retObj = doFGPayReq(payOrder, 650); //ALIH5 支付宝 sdk 支付
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }

    public JSONObject doFGPayReq(PayOrder payOrder, int channel) {
        String logPrefix = "【FG支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));

            //开始创建订单，订单生成参数请根据相关参数自行调整。
            Map<String, Object> post = new HashMap<String, Object>();
            post.put("paytype", channel);//类型请自行调整
            post.put("out_trade_no", payOrder.getPayOrderId());//商户订单号
            post.put("notify_url", payConfig.getNotifyUrl(getChannelName()));//这个是订单回调地址，成功付款后定时通知队列会调这个地址。
            post.put("return_url", payConfig.getReturnUrl());//这个是订单回调地址，成功付款后实时跳回这个地址。
            post.put("goodsname", "韩信支付");//商品名称
            BigDecimal b = new BigDecimal(payOrder.getAmount().doubleValue() / 100);
            String f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
            post.put("total_fee", f1);//定单金额，不要带小数，必须是整数
            post.put("remark", "韩信");//平台的名称，做区分用的。
            post.put("requestip", "127.0.0.1");//玩家的IP。
            //开始创建订单，订单生成参数请根据相关参数自行调整。
            Map<String, Object> post1 = new HashMap<String, Object>();
            post1.put("mchid", Integer.parseInt(channelPayConfig.getMchId()));//商户ID
            post1.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));// 时间戳(10位秒)
            post1.put("nonce", RandomStringUtils.randomAlphanumeric(31));//随机码


            Map<String, Object> post2 = new HashMap<String, Object>();
            post2.putAll(post);
            post2.putAll(post1);

            Map<String, Object> sortMap = new TreeMap<String, Object>(
                    new MapKeyComparator());
            sortMap.putAll(post2);

            //加入签名
            String sign = PayDigestUtil.getSign(sortMap, channelPayConfig.getmD5Key()).toLowerCase();
            post1.put("sign", sign);//商户密钥,请自行修改
            _log.info(logPrefix + "******************sign:{}", sign);
            post1.put("data", post); //合并真正提交的参数JSON
            JSONObject json = new JSONObject(post1);
            _log.info(logPrefix + "******************sendMsg:{}", json.toJSONString());
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/pay/gopay", json.toJSONString());
            _log.info(logPrefix + "******************上游返回数据:{}", res);
            JSONObject resObj = JSONObject.parseObject(res);
            int resultCode = resObj.getInteger("error");
            String retMsg = resObj.getString("msg");
            if (resultCode == 0) {
                //  验签
                if (verifyPayParams(resObj)) {
                    String payJumpUrl = resObj.getJSONObject("data").getString("payurl");
                    JSONObject payParams = new JSONObject();
                    payParams.put("payJumpUrl", payJumpUrl);
                    if (payOrder.getChannelId().contains("SDK")) {
                        //SDK跳转
                        payParams.put("payMethod", PayConstant.PAY_METHOD_SDK_JUMP);
                    } else if (payOrder.getChannelId().contains("QR")) {
                        //二维码
                        payParams.put("payMethod", PayConstant.PAY_METHOD_CODE_IMG);
                    } else {
                        //表单跳转
                        if(payJumpUrl.contains("form"))
                        {
                            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
                        }else {
                            payParams.put("payMethod", PayConstant.PAY_METHOD_URL_JUMP);
                        }
                    }

                    payInfo.put("payParams", payParams);
                    int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), resObj.getJSONObject("data").getString("trade_no"));
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
     * 验证蚂蚁返回参数
     *
     * @return
     */
    public boolean verifyPayParams(JSONObject res) throws Exception {
        Map<String, Object> post = new HashMap<String, Object>();
        post.put("paytype", res.getInteger("error"));//0代表成功,其他代表失败
        // post.put("msg", res.getString("msg"));//描述
        post.put("serverTime", res.getInteger("serverTime"));//服务器时间戳

        String total_fee = res.getJSONObject("data").getString("total_fee");               // 订单金额

        Map<String, Object> post1 = new HashMap<String, Object>();
        post1.put("trade_no", res.getJSONObject("data").getString("trade_no"));//支付平台订单号
        post1.put("total_fee", total_fee);//支付金额
        post1.put("paytype", res.getJSONObject("data").getInteger("paytype"));//支付类型
        post1.put("payurl", res.getJSONObject("data").getString("payurl"));//支付地址url
        post1.put("out_trade_no", res.getJSONObject("data").getString("out_trade_no"));//商户订单号
        post1.put("mark", res.getJSONObject("data").getString("mark"));//订单的标识数字
        post1.put("createdate", res.getJSONObject("data").getString("createdate"));//订单创建时间

        Map<String, Object> post2 = new HashMap<String, Object>();
        post2.putAll(post);
        post2.putAll(post1);

        String sign = res.getString("sign");  //签名

        // 查询payOrder记录
        String payOrderId = post1.get("out_trade_no").toString();
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder == null) {
            _log.error("Can't found payOrder form db. payOrderId={}, ", payOrderId);
            return false;
        }
        // 验证签名
        ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
        Map<String, Object> sortMap = new TreeMap<String, Object>(
                new MapKeyComparator());
        sortMap.putAll(post2);

      /*  String signMd5 = PayDigestUtil.getSign(sortMap, channelPayConfig.getmD5Key());
        if (!signMd5.equals(sign)) {
            _log.error("验证签名失败. payOrderId={}, ", payOrderId);
            return false;
    }*/
        // 核对金额
        long outPayAmt = new BigDecimal(total_fee).multiply(new BigDecimal(100)).longValue();
        long dbPayAmt = payOrder.getAmount().longValue();
        if (dbPayAmt != outPayAmt) {
            _log.error("金额不一致. outPayAmt={},payOrderId={}", outPayAmt, payOrderId);
            return false;
        }
        return true;
    }


    /**
     * 查询订单
     *
     * @param payOrder
     * @return
     */
    @Override
    public JSONObject query(PayOrder payOrder) {
        String logPrefix = "【FG支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("mchid", channelPayConfig.getMchId());//商户编号
            map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));//时间戳(10位秒)
            map.put("nonce", RandomStringUtils.randomAlphanumeric(31));//随机码
            map.put("trade_no", payOrder.getChannelOrderNo());//随机码
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toLowerCase());

            map.remove("trade_no");
            map.put("data", new JSONObject().put("trade_no", payOrder.getChannelOrderNo()));//业务参数对象

            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.postJson(channelPayConfig.getReqUrl() + "/api/pay/orderinfo", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String error = resObj.getString("error");
            String state = resObj.getString("state");

            if (error.equals("0") && state.equals("3")) {
                BigDecimal amount = resObj.getBigDecimal("amount");
                String ressign = resObj.getString("sign");
                //验签
                SortedMap map1 = new TreeMap();
                map1.put("error", resObj.getString("error"));//商户编号
                map1.put("msg", resObj.getString("msg"));
                map1.put("serverTime", resObj.getString("serverTime"));
                map1.put("trade_no", resObj.getJSONObject("data").getString("trade_no"));
                map1.put("total_fee", resObj.getJSONObject("data").getString("total_fee"));
                map1.put("tradingfee", resObj.getJSONObject("data").getString("tradingfee"));
                map1.put("paysucessdate", resObj.getJSONObject("data").getString("paysucessdate"));
                map1.put("out_trade_no", resObj.getJSONObject("data").getString("out_trade_no"));
                map1.put("state", resObj.getJSONObject("data").getString("state"));
                String signMd5 = PayDigestUtil.getSign(map1, channelPayConfig.getmD5Key());
                //核对验签
                if (!signMd5.equals(ressign)) {
                    retObj.put("status", "1");
                } else {
                    // 核对金额
                    long outPayAmt = amount.multiply(new BigDecimal(100)).longValue();//乘以100
                    long dbPayAmt = payOrder.getAmount().longValue();
                    if (dbPayAmt == payOrder.getAmount()) {
                        //支付成功
                        retObj.put("status", "2");
                    } else {
                        retObj.put("status", "1");
                    }
                }
            } else {

                retObj.put("status", "1");
            }
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("errDes", "操作失败!");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }
}

class MapKeyComparator implements Comparator<String> {
    public int compare(String str1, String str2) {
        return str1.compareTo(str2);
    }
}