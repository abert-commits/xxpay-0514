package org.xxpay.transit.contorller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayCashCollConfig;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.transit.service.AlipayConfig;
import org.xxpay.transit.service.AlipayPayNotifyService;
import org.xxpay.transit.service.AlipayPaymentService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

@Controller
public class testController {

    @Autowired
    public AlipayPaymentService alipayPaymentService;

    @Autowired
    public AlipayPayNotifyService alipayPayNotifyService;

    @RequestMapping("/index")
    public ModelAndView index(@RequestParam("mchOrderId") String mchOrderId, @RequestParam("amount") String amount) {
        ModelAndView mav = new ModelAndView("index");

        String payUrl = CacheUtil.get("AliPay" + mchOrderId);//本地缓存支付宝支付链接，缓存时间5分钟

        if (StringUtils.isNotBlank(payUrl)) {
            mav.addObject("msg", "");
            mav.addObject("payUrl", payUrl);
        } else {
            mav.addObject("msg", "订单已经过期");
            mav.addObject("payUrl", "");
        }

        mav.addObject("mchOrderId", mchOrderId);
        mav.addObject("amount", amount);

        mav.setViewName("index");
        return mav;
    }


    @RequestMapping("/alipay")
    public ModelAndView alipay(@RequestParam("orderId") String orderId) {
        ModelAndView mav = new ModelAndView("index");
        String payUrl = CacheUtil.get(orderId);//本地缓存支付宝支付链接，缓存时间5分钟
        mav.addObject("payUrl", payUrl);
        mav.setViewName("alipay");
        return mav;
    }

    private static final MyLog _log = MyLog.getLog(testController.class);

    /**
     * 支付
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/pay")
    @ResponseBody
    public String pay(HttpServletRequest request, HttpServletResponse response) {
        try {
            String payOrder = request.getParameter("payOrder");
            String alipayConfig = request.getParameter("alipayConfig");
            JSONObject resObj = alipayPaymentService.pay(payOrder, alipayConfig);
            return resObj.toJSONString();
        } catch (Exception ex) {
            JSONObject retObj = new JSONObject();
            retObj.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败[" + ex.getMessage() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj.toJSONString();
        }
    }


    /**
     * 单笔转账
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/alipaytrans")
    @ResponseBody
    public String alipaytrans(HttpServletRequest request, HttpServletResponse response) {
        try {
            String payOrderCashCollRecord = request.getParameter("payOrderCashColl");
            String alipayConfig = request.getParameter("alipayConfig");

            JSONObject resObj = alipayPaymentService.alipaytrans(payOrderCashCollRecord, alipayConfig);
            return resObj.toJSONString();
        } catch (Exception ex) {
            ex.printStackTrace();
            JSONObject retObj = new JSONObject();
            retObj.put(PayConstant.RETURN_PARAM_RETMSG, "下单失败[" + ex.getMessage() + "]");
            retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return retObj.toJSONString();
        }
    }


    @RequestMapping("/notifyUrl")
    @ResponseBody
    public String notifyUrl(HttpServletRequest request) {
        return alipayPayNotifyService.doNotify(request);
    }

    /**
     * 订单查询
     *
     * @param request
     * @return
     */

    @RequestMapping("/query")
    @ResponseBody
    public String query(HttpServletRequest request) {
        String payOrder = request.getParameter("payOrder");
        String alipayConfig = request.getParameter("alipayConfig");
        JSONObject resObj = alipayPaymentService.query(payOrder, alipayConfig);
        return resObj.toJSONString();
    }


    /**
     * 分账关系绑定
     *
     * @param request
     * @return
     */
    @RequestMapping("/relationbind")
    @ResponseBody
    public String relationbind(HttpServletRequest request) {

        String payCashCollConfig = request.getParameter("payCashCollConfig");
        String alipayConfig = request.getParameter("alipayConfig");
        JSONObject resObj = alipayPaymentService.relationbind(payCashCollConfig, alipayConfig);
        return resObj.toJSONString();
    }


    /**
     * 执行分账
     *
     * @param request
     * @return
     */
    @RequestMapping("/merchantSplit")
    @ResponseBody
    public String merchantSplit(HttpServletRequest request) {

        String royalty_parameters = request.getParameter("royalty_parameters");
        String alipayConfig = request.getParameter("alipayConfig");
        String out_request_no = request.getParameter("out_request_no");
        String trade_no = request.getParameter("trade_no");
        String passageAccountId = request.getParameter("passageAccountId");
        JSONObject resObj = alipayPaymentService.merchantSplit(royalty_parameters, alipayConfig, out_request_no, trade_no, passageAccountId);
        return resObj.toJSONString();
    }


    @RequestMapping("/querymerchantSplit")
    @ResponseBody
    public String querymerchantSplit(HttpServletRequest request) {
        String payOrder = request.getParameter("payOrder");
        String alipayConfig = request.getParameter("alipayConfig");
        JSONObject resObj = alipayPaymentService.querymerchantSplit(payOrder, alipayConfig);
        return resObj.toJSONString();
    }

    /**
     * 现金红包分发
     *
     * @param
     * @return
     */
    @RequestMapping("/transunitransfer")
    @ResponseBody
    public String transunitransfer(HttpServletRequest request) {

        String payOrder = request.getParameter("payOrder");
        String alipayConfig = request.getParameter("alipayConfig");
        String payCashCollConfig = request.getParameter("payCashCollConfig");
        JSONObject resObj = alipayPaymentService.transunitransfer(payOrder, alipayConfig, payCashCollConfig);
        return resObj.toJSONString();
    }

    @RequestMapping("/accountquery")
    @ResponseBody
    public String accountquery(HttpServletRequest request) {

        //支付资金接口查询
        AlipayConfig alipayConfig = new AlipayConfig();
        alipayConfig.setPid("2088131761439843");
        alipayConfig.setAppId("2018070360540297");
        alipayConfig.setPrivateKey("MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC7rupMu0/YijeODjpuvxYEIRzufUcdSkbsv9BXweTPXfvhnnLKt/VAGCxaMZfcn0Y4LDLNHZ4zroEtEXWpvnx6RtnJtmfjwOdF2f9hV3Ju3lyZ2l4azR7YWAwVlDN8H6U/KilKLgeLwyDASkx384VmpDzAukDg+nr9oDDSYdw8DaJtAxGq+kgLyKCns99H/0DqYX5JZlf1Fwv9CD6WATBDezmbKEnY60O32UpVqAl33iseJyYCuCAXIQ6foddQu22ah7clb8K3Ubj4j4f9mCZeocG2CEBJgdK9nvPB15wH2gvQaJVt+gZoGu2Cj53p06NuBovn3RAzcHTK3f7Wei7XAgMBAAECggEAfTc5wm/F2aZ9Yh1EBtceDSs6bjLO20/O+e0PRz1pNqBxiBl6ZJ1O1rBKBvLHar4ozxlN22NDRR/LtxHK+rDHw5y5eMZlkaIFqqjCpWL+SdG43jy+RAtN5PD5PvxdTRaf3QB4A7Gp/yj0FNZ9JhomilBQ6BRbbL4wiD3z1KjNkpIxbgAs+NHITTq5jjTQ/QQa78ABfHqs1vJx3ryYbVdNcPrIQMngoohelaco8AdeXrn0D+2WaIIDuu2oegGkv4AFMklod/OTzl7rdp0JnNDnlyw09ORyNAS2+18dU9aRqNxysRkPfjI8dj0PyryqI86gs7Ksab8fKTbfE4OMfi0hQQKBgQDkRXfYOOaUmhBC7BQ3/MMRnC29VaXlcDqSYf+dxFPgsKseR2B7frrFCaRhmvtwIQ7tNwiph/PS7zT8KEPkQRIdFBjp0nTixZ1ZImfnKyQVQO43vApbtJvXNWF0sAjgP0R9PxO3adYJIFFe9TWmhNJ7LJDNGLh0SfVH9z915BHfHwKBgQDSe0h2BsCIAa+1PyJo9qkQHIMLnHFCAecgTLQ6jr7J/HsiExs968iNTlEFWkz2a3y31ntC2uDlNs8VBV8It7dWvcIW7JDPBhZN9VjI4dlIvLjIwhS1e2aV6fVEIOPxZPfaCkWeG7zJczLK89tC05BpgznWV+qX4cOiPRv+bt+RSQKBgAKC5SOi1J6T51PlJv/Krxa1gidQFugkSPCtVVOWAlo5d1h47o3NQh7C0WUlgFimdHSVo2nCDiOm8A1KIB0Vvi5Ft1RzNUf9ZpLBdyUq61W0hipkzjReE0zV0IGpIh/dclJybDQbXPhyu5Jw3is70Nj8D4fCGt1kpucoyDDy1mN/AoGAXq64nOdSqpvfl6/L4d27lZTN2mZIiIHkn6IBnSnjZ2ddWKmxonwzXmVxiu/hGSSnGOex0RQ3AMVUCEe/RWOnZKy956QJTCeX1v/cLZlzLgY/NYgg2ralIiD5hOkqwMdDu2DNil894H0ixytFYd1b5Cr01Jf93s7fO1c0BZOInIkCgYAUSNuCsf1LIp3SGF++eGqsdjzvFA/Uk7pcjAlxxLT+kjYVVJ/MjMRuAzVwsI6KCCNUAWvRpc+R/CL4sjFPVQyXami3ewOzBfPtquhArTIlJhQ8+4A7rZbreANBh0QcnEI0b1pjHNk8VPmSehcUbRwPsDxuZoj9eFnsDNrA8VNqJA==");
        JSONObject object = alipayPaymentService.accountquery(alipayConfig);
        return object.toJSONString();
    }


    @RequestMapping("/api/alipay/pay_{type}.htm")
    public String toPay(HttpServletRequest request, ModelMap model, @PathVariable("type") String type) throws ServletException, IOException {
        JSONObject po = getJsonParam(request);
        // https://qr.alipay.com/fkx04112akzpulwo9pvye74?t=1541681073413
        String amount = getString(po, "amount");
        String payOrderId = getString(po, "payOrderId");
        String mchOrderNo = getString(po, "mchOrderNo");
        String expireTime = String.valueOf(5 * 60);
        String codeUrl = getString(po, "codeUrl");
        String codeImgUrl = getString(po, "codeImgUrl");
        String toAlipayUrl = "https://mobilecodec.alipay.com/client_download.htm?qrcode=" + codeUrl.substring(codeUrl.lastIndexOf("/") + 1);
        String startAppUrl = "alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + URLEncoder.encode(codeUrl);
        // https://mobilecodec.alipay.com/client_download.htm?qrcode=fkx04319ftj9wcl46eh8m5a&t=1540812174754
        //String toAlipayUrl = "https://mobilecodec.alipay.com/client_download.htm?qrcode=" + codeUrl.substring(codeUrl.lastIndexOf("/") + 1);
        // alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=https%3a%2f%2fmobilecodec.alipay.com%2fclient_download.htm%3fqrcode%3dfkx04112akzpulwo9pvye74%3ft%3d1541681073413
        //String startAppUrl = "alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + URLEncoder.encode(toAlipayUrl);
        model.put("amount", AmountUtil.convertCent2Dollar(amount));
        model.put("amountStr", "￥" + AmountUtil.convertCent2Dollar(amount));
        model.put("mchOrderNo", mchOrderNo);
        model.put("payOrderId", payOrderId);
        model.put("expireTime", expireTime);
        model.put("codeUrl", codeUrl);
        model.put("codeImgUrl", codeImgUrl);
        model.put("toAlipayUrl", toAlipayUrl);
        model.put("startAppUrl", startAppUrl);

        return "pay_" + type;
    }

    private String getString(JSONObject param, String key) {
        if (param == null) return null;
        return param.getString(key);
    }

    private JSONObject getJsonParam(HttpServletRequest request) {
        String params = request.getParameter("params");
        if (StringUtils.isNotBlank(params)) {
            return JSON.parseObject(params);
        }
        // 参数Map
        Map properties = request.getParameterMap();
        // 返回值Map
        JSONObject returnObject = new JSONObject();
        Iterator entries = properties.entrySet().iterator();
        Map.Entry entry;
        String name;
        String value = "";
        while (entries.hasNext()) {
            entry = (Map.Entry) entries.next();
            name = (String) entry.getKey();
            Object valueObj = entry.getValue();
            if (null == valueObj) {
                value = "";
            } else if (valueObj instanceof String[]) {
                String[] values = (String[]) valueObj;
                for (int i = 0; i < values.length; i++) {
                    value = values[i] + ",";
                }
                value = value.substring(0, value.length() - 1);
            } else {
                value = valueObj.toString();
            }
            returnObject.put(name, value);
        }
        return returnObject;
    }

}
