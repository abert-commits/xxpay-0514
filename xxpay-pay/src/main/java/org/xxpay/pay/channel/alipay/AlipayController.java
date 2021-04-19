package org.xxpay.pay.channel.alipay;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.ctrl.common.BaseController;
import org.xxpay.pay.service.RpcCommonService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URLEncoder;

@Controller
public class AlipayController extends BaseController {

    private static final MyLog _log = MyLog.getLog(AlipayController.class);

    @Autowired
    private RpcCommonService rpcCommonService;

    @RequestMapping("/api/alipay/pay_{type}.htm")
    public String toPay(HttpServletRequest request, ModelMap model, @PathVariable("type") String type) throws ServletException, IOException {
        JSONObject po = getJsonParam(request);
        // https://qr.alipay.com/fkx04112akzpulwo9pvye74?t=1541681073413
        String amount = getString(po, "amount");
        String payOrderId = getString(po, "payOrderId");
        String mchOrderNo = getString(po, "mchOrderNo");
        String expireTime = String.valueOf(5*60);
        String codeUrl = getString(po, "codeUrl");
        String codeImgUrl = getString(po, "codeImgUrl");
        String toAlipayUrl = "https://mobilecodec.alipay.com/client_download.htm?qrcode=" + codeUrl.substring(codeUrl.lastIndexOf("/") + 1);
        String startAppUrl = "alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + URLEncoder.encode(codeUrl);
        // https://mobilecodec.alipay.com/client_download.htm?qrcode=fkx04319ftj9wcl46eh8m5a&t=1540812174754
        //String toAlipayUrl = "https://mobilecodec.alipay.com/client_download.htm?qrcode=" + codeUrl.substring(codeUrl.lastIndexOf("/") + 1);
        // alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=https%3a%2f%2fmobilecodec.alipay.com%2fclient_download.htm%3fqrcode%3dfkx04112akzpulwo9pvye74%3ft%3d1541681073413
        //String startAppUrl = "alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=" + URLEncoder.encode(toAlipayUrl);
        model.put("amount", AmountUtil.convertCent2Dollar(amount));
        model.put("amountStr", "￥"+ AmountUtil.convertCent2Dollar(amount));
        model.put("mchOrderNo", mchOrderNo);
        model.put("payOrderId", payOrderId);
        model.put("expireTime", expireTime);
        model.put("codeUrl", codeUrl);
        model.put("codeImgUrl", codeImgUrl);
        model.put("toAlipayUrl", toAlipayUrl);
        model.put("startAppUrl", startAppUrl);

        return "payment/alipay/pay_" + type;
    }


    /**
     * 订单查询
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/api/alipay/query")
    @ResponseBody
    public String toQuery(HttpServletRequest request) throws ServletException, IOException {
        JSONObject po = getJsonParam(request);
        String payOrderId = getString(po, "payOrderId");
        _log.info("[查询订单]参数payOrderId={}", payOrderId);

        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        JSONObject retObj = new JSONObject();
        String status = "-1";
        String url = "";
        if(payOrder != null) {
            status = payOrder.getStatus() + "";
            url = payOrder.getReturnUrl();
        }
        retObj.put("status", status);
        retObj.put("url", url);
        _log.info("[查询订单]结果payOrderId={},retObj={}", payOrderId, retObj);
        return retObj.toJSONString();
    }
}
