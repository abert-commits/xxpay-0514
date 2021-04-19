package org.xxpay.pay.channel.pinduoduo;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.Nginx;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PinduoduoOrders;
import org.xxpay.core.entity.PinduoduoUser;
import org.xxpay.pay.channel.BasePayment;
import org.xxpay.pay.channel.ChannelPayConfig;
import org.xxpay.pay.mq.BaseNotify4MchPay;
import org.xxpay.pay.mq.Mq4PinduoduoNotify;
import org.xxpay.pay.service.RpcCommonService;

import javax.jms.Queue;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

@Service
public class PinduoduoPaymentService extends BasePayment {

    private static final MyLog _log = MyLog.getLog(PinduoduoPaymentService.class);
    @Autowired
    private RpcCommonService rpcCommonService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    public Mq4PinduoduoNotify mq4PinduoduoNotify;

    @Autowired
    private Queue payPddNotifyQueue;

    private Nginx getRedisNginx(){
        List<Nginx> nginxList = (List<Nginx>) redisTemplate.opsForValue().get(PDDUtils.NginxList);
        WeightUtil<Nginx> wt = new WeightUtil<Nginx>();
        if(nginxList==null||nginxList.size()<1){
            Nginx nginx=new Nginx();
            nginx.setStatus("1");
            nginxList=rpcCommonService.rpcINginxService.select(0,50000,nginx);
            redisTemplate.opsForValue().set(PDDUtils.NginxList,nginxList);
            return wt.choose(nginxList);
        }
        return wt.choose(nginxList);
    }

    @Override
    public String getChannelName() {
        return PayConstant.CHANNEL_NAME_PINDUODUO;
    }

    @Override
    public JSONObject pay(PayOrder payOrder) {
        JSONObject retObj;
        String channelId = payOrder.getChannelId();
        String channelName = channelId.substring(channelId.indexOf("_") + 1);
        switch (channelName) {
            //支付宝H5
            case PayConstant.newTradeTypeConstant.TRADE_TYPE_ALIPAY:
                retObj = doAliH5PayReq(payOrder);
                break;
            default:
                retObj = buildRetObj(PayConstant.RETURN_VALUE_FAIL, "不支持的渠道[channelId=" + channelId + "]");
                break;
        }
        return retObj;
    }


    public JSONObject doAliH5PayReq(PayOrder payOrder) {
        String logPrefix = "【pinduoduo付支付统一下单】";
        JSONObject payInfo = new JSONObject();
        try {
                //根据订单金额 获取拼多多订单
            PinduoduoOrders orders=new PinduoduoOrders();
            orders.setTotal(payOrder.getAmount().intValue());
            orders.setIsPay(false);
            orders.setPayType(Byte.valueOf("9"));
            orders.setStatus(0);
            List<PinduoduoOrders> ordersList=rpcCommonService.rpcIPinduoduoOrderService.select(0,1,orders,null,null);
            if(ordersList==null|| ordersList.size()<1){
                _log.info(logPrefix+": "+payOrder.getPayOrderId()+" 失败信息：[没有"+ AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount())) +" 金额的商品了 请联系运营人员添加]");
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[没有"+ AmountUtil.convertCent2DollarShort(String.valueOf(payOrder.getAmount())) +" 金额的商品了 请联系运营人员添加]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }

            //获取下单人员信息
            PinduoduoOrders orders1=ordersList.get(0);
            PinduoduoUser user=new PinduoduoUser();
            user.setId(orders1.getUserId());
            user.setIs_limit(false);
            user.setIs_expired(false);
            user.setStatus(true);
            List<PinduoduoUser> userList =rpcCommonService.rpcIPinduoduoUserService.selectUser(user);
            if(userList==null|| userList.size()<1){
                _log.info(logPrefix+": "+payOrder.getPayOrderId()+" 失败信息：[没有"+ orders1.getUserId() +" 的下单账号了 请联系运营人员添加]");
                payInfo.put("errDes", "下单失败,\n" + "失败信息：[没有"+ orders1.getUserId()+" 的下单账号了 请联系运营人员添加]");
                payInfo.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return payInfo;
            }
            user=userList.get(0);
            //拼接下单信息 {"pay_app_id":9,"version":3,"attribute_fields":{"paid_times":0,"forbid_contractcode":"1","forbid_pappay":"1"},
            // "return_url":"https://mobile.yangkeduo.com/transac_wappay_callback.html?order_sn=200423-419802809030363","order_sn":"200423-419802809030363","term":null}
            JSONObject jsonObject=new JSONObject();
            jsonObject.put("pay_app_id",orders1.getPayType());
            jsonObject.put("version",3);
            jsonObject.put("return_url","https://mobile.yangkeduo.com/transac_wappay_callback.html?order_sn="+orders1.getOrderSn());
            jsonObject.put("order_sn",orders1.getOrderSn());
            jsonObject.put("term",null);

            JSONObject jsonObject1=new JSONObject();
            jsonObject1.put("paid_times",0);
            jsonObject1.put("forbid_contractcode","1");
            jsonObject1.put("forbid_pappay","1");
            jsonObject.put("attribute_fields",jsonObject1);

            //根据拼多多订单 获取 支付信息
            Nginx nginx=getRedisNginx();
            JSONObject json=PDDUtils.repayUrl(user.getUid(),user.getAccess_token(),user.getUin(),jsonObject.toJSONString(),nginx.getNginxIP(),nginx.getNginxPort());
               //组装H5
            _log.info("上游返回信息：" + json);
             String ailPayUrl=json.getString("gateway_url");
            JSONObject query=json.getJSONObject("query");
            StringBuffer payForm = new StringBuffer();
            payForm.append("<form  type=\"hidden\" name=\"punchout_form\" method=\"get\" action=\""+ailPayUrl+"\" >");
            payForm.append("<input type=\"hidden\" name=\"service\" value=\"" + query.getString("service") + "\" >");
            payForm.append("<input type=\"hidden\" name=\"partner\" value=\"" + query.getString("partner") + "\" >");
            payForm.append("<input type=\"hidden\" name=\"seller_id\" value=\"" + query.getString("seller_id") + "\" >");
            payForm.append("<input type=\"hidden\" name=\"payment_type\" value=\"" + query.getString("payment_type") + "\" >");
            payForm.append("<input type=\"hidden\" name=\"notify_url\" value=\"" + query.getString("notify_url") + "\" >");
            payForm.append("<input type=\"hidden\" name=\"out_trade_no\" value=\"" + query.getString("out_trade_no") + "\" >");
            payForm.append("<input type=\"hidden\" name=\"subject\" value=\"" + query.getString("subject") + "\">");
            payForm.append("<input type=\"hidden\" name=\"total_fee\" value=\"" + query.getString("total_fee") + "\" >");
            payForm.append("<input type=\"hidden\" name=\"return_url\" value=\"" + query.getString("return_url") + "\" >");
            payForm.append("<input type=\"hidden\" name=\"sign\" value=\"" + query.getString("sign") + "\">");
            payForm.append("<input type=\"hidden\" name=\"sign_type\" value=\"" + query.getString("sign_type") + "\">");
            payForm.append("<input type=\"hidden\" name=\"goods_type\" value=\"" + query.getString("goods_type") + "\" >");
            payForm.append("<input type=\"hidden\" name=\"_input_charset\"value=\"" + query.getString("_input_charset") + "\"  >");
            payForm.append("<input type=\"submit\" value=\"立即支付\" style=\"display:none\" >");
            payForm.append("</form>");
            payForm.append("<script>document.forms[0].submit();</script>");

            JSONObject payParams = new JSONObject();
            payParams.put("payMethod", PayConstant.PAY_METHOD_FORM_JUMP);
            payParams.put("payJumpUrl", payForm);
            payInfo.put("payParams", payParams);
            //修改拼多多订单状态
            orders1.setIsPay(true);
            orders1.setIp(nginx.getNginxIP());
            orders1.setApiOrderSn(payOrder.getPayOrderId());
            orders1.setMtime(new Date());
            int resultOrders=rpcCommonService.rpcIPinduoduoOrderService.update(orders1);
            int result = rpcCommonService.rpcPayOrderService.updateStatus4Ing(payOrder.getPayOrderId(), "");
            if (result > 0) {
                JSONObject jsonObject2=new JSONObject();
                jsonObject2.put("order_sn",orders1.getOrderSn());
                jsonObject2.put("payOrderId",payOrder.getPayOrderId());
                jsonObject2.put("PDDAccessToken",user.getAccess_token());
                jsonObject2.put("nginxIP",nginx.getNginxIP());
                jsonObject2.put("nginxPort",nginx.getNginxPort());
                jsonObject2.put("count",0);
                // 拼拼多定时查看状态
                mq4PinduoduoNotify.send(payPddNotifyQueue,"", 20 * 1000);
            }
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
        String logPrefix = "【pinduoduo支付订单查询】";
        JSONObject payInfo = new JSONObject();
        JSONObject retObj = new JSONObject();
        try {
            ChannelPayConfig channelPayConfig = new ChannelPayConfig(getPayParam(payOrder));
            SortedMap map = new TreeMap();
            map.put("pay_memberid", channelPayConfig.getMchId());//商户编号
            map.put("orderid", payOrder.getPayOrderId());//上游訂單號
            map.put("randStr", RandomStringUtils.randomAlphanumeric(32));//上游訂單號
            String sign = PayDigestUtil.getSign(map, channelPayConfig.getmD5Key());
            _log.info(logPrefix + "******************sign:{}", sign);
            map.put("sign", sign.toLowerCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            _log.info(logPrefix + "******************sendMsg:{}", sendMsg);
            String res = XXPayUtil.doPostQueryCmd(channelPayConfig.getReqUrl() + "/Pay_Index_verify.html", sendMsg);
            _log.info("上游返回信息：" + res);
            JSONObject resObj = JSONObject.parseObject(res);
            String retCode = resObj.getString("status");
//            {"status":"error","msg":"不存在的交易订单号.","data":[]}
            if (resObj.containsKey("status") && resObj.getString("status").equals("error")) {
                retObj.put("status", "1");
                retObj.put("msg", resObj.getString("msg"));
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
                return retObj;
            }
            if (retCode.equals("success")) {
                // 订单成功
                BigDecimal amount = resObj.getJSONObject("data").getBigDecimal("amount");
                // 核对金额
                long outPayAmt = amount.multiply(new BigDecimal(100)).longValue();//乘以100
                long dbPayAmt = payOrder.getAmount().longValue();
                if (dbPayAmt == payOrder.getAmount()) {
                    //支付成功
                    retObj.put("status", "2");
                    retObj.put("msg", "支付成功");
                } else {
                    retObj.put("status", "1");
                    retObj.put("msg", "上游订单金额与本地订单金额不符合");
                }

            } else {
                retObj.put("status", "1");
                retObj.put("msg", "支付中");
            }

            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
            return retObj;
        } catch (Exception e) {
            retObj.put("msg", "查询上游订单发生异常！");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj;
        }
    }
}
