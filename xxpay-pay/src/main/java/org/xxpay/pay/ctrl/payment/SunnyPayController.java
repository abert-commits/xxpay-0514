package org.xxpay.pay.ctrl.payment;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.constant.PayEnum;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.common.vo.OrderCostFeeVO;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.PaymentInterface;
import org.xxpay.pay.channel.sandpay.util.SandHttpUtil;
import org.xxpay.pay.ctrl.common.BaseController;
import org.xxpay.pay.service.PayOrderService;
import org.xxpay.pay.service.RpcCommonService;
import org.xxpay.pay.util.SpringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 2020.03.12
 * 晴天支付 Controller控制器
 */

@RestController
public class SunnyPayController extends BaseController {
    private final MyLog _log = MyLog.getLog(SunnyPayController.class);

    @Autowired
    private RpcCommonService rpcCommonService;

    private PaymentInterface paymentInterface;

    @Autowired
    private PayOrderService payOrderService;

    /**
     * 晴天支付接口
     * @Author Kate
     */
    @RequestMapping(value = "/api/pay/sunny_order")
    public String sunnyPayMent(HttpServletRequest request, HttpServletResponse response) {
         String url = "http://api.szfqbal.cn/indexpay.php?customerid=11809&sdorderno=ST00003&total_fee=297.00\n" +
                 "&remark=ceshi&paytype=alipay &notifyurl=http://sh.diyiym.com/php/notify_url.php&\n" +
                 "returnurl=http://pay.diyiym.com/return_url.php&sign=a89298c261d8f7712a48c087a45a348a";
            String result = SandHttpUtil.doPost(url);
        System.out.println(result);
        JSONObject payContext = new JSONObject();
        PayOrder payOrder = null;
        try {
            // 执行支付
            JSONObject retObj = paymentInterface.pay(payOrder);
            if(retObj.get(PayConstant.RETURN_PARAM_RETCODE).equals(PayConstant.RETURN_VALUE_SUCCESS)) {
                retObj.put("payOrderId", payOrder.getPayOrderId());
                // 使用StringEscapeUtils.unescapeJava去掉字符串中的转义符号(不采用,会导致json解析报错)
                //return StringEscapeUtils.unescapeJava(XXPayUtil.makeRetData(retObj, payContext.getString("key")));
                return XXPayUtil.makeRetData(retObj, payContext.getString("key"));
            }else {
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL,
                        "调用支付渠道失败" + (retObj.get(PayConstant.RETURN_PARAM_RETMSG) == null ? "" : ("(" + retObj.get(PayConstant.RETURN_PARAM_RETMSG) + ")")),
                        null, retObj.getString("errCode"), retObj.getString("errDes")));
            }
        }catch (Exception e) {
            _log.error(e, "");
            return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付中心系统异常", null, PayEnum.ERR_0010.getCode(), "请联系技术人员查看"));
        }
    }

    public static void main(String[] args) {
        String url = "http://api.szfqbal.cn/indexpay.php?customerid=11809&sdorderno=ST00003&total_fee=297.00\n" +
                "&remark=ceshi&paytype=alipay &notifyurl=http://sh.diyiym.com/php/notify_url.php&\n" +
                "returnurl=http://pay.diyiym.com/return_url.php&sign=a89298c261d8f7712a48c087a45a348a";
        Object result = SandHttpUtil.doPost(url);
        System.out.println("++>><<"+result);
    }

}
