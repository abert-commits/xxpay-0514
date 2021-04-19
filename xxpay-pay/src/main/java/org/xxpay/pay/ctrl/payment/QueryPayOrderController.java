package org.xxpay.pay.ctrl.payment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.constant.PayEnum;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.*;
import org.xxpay.core.common.vo.OrderCostFeeVO;
import org.xxpay.core.entity.MchApp;
import org.xxpay.core.entity.MchInfo;
import org.xxpay.core.entity.PayCashCollConfig;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.pay.channel.CashCollInterface;
import org.xxpay.pay.channel.PaymentInterface;
import org.xxpay.pay.ctrl.common.BaseController;
import org.xxpay.pay.service.PayOrderService;
import org.xxpay.pay.service.RpcCommonService;
import org.xxpay.pay.util.SpringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author dingzhiwei jmdhappy@126.com
 * @version V1.0
 * @Description: 支付订单查询
 * @date 2017-08-31
 * @Copyright: www.xxpay.org
 */
@RestController
public class QueryPayOrderController extends BaseController {

    private final MyLog _log = MyLog.getLog(QueryPayOrderController.class);

    @Autowired
    private PayOrderService payOrderService;

    @Autowired
    private RpcCommonService rpcCommonService;

    private PaymentInterface paymentInterface;

    private CashCollInterface cashCollInterface;


    /**
     * 查询支付订单接口:
     * 1)先验证接口参数以及签名信息
     * 2)根据参数查询订单
     * 3)返回订单数据
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/pay/query_order")
    public String queryPayOrder(HttpServletRequest request) {
        _log.info("###### 开始接收商户查询支付订单请求 ######");
        String logPrefix = "【商户支付订单查询】";
        try {
            JSONObject po = getJsonParam(request);
            _log.info("{}请求参数:{}", logPrefix, po);
            JSONObject payContext = new JSONObject();
            // 验证参数有效性
            String errorMessage = validateParams(po, payContext);
            if (!"success".equalsIgnoreCase(errorMessage)) {
                _log.warn(errorMessage);
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, errorMessage, null, null));
            }
            _log.debug("请求参数及签名校验通过");
            Long mchId = po.getLong("mchId");                    // 商户ID
            String mchOrderNo = po.getString("mchOrderNo");    // 商户订单号
            String payOrderId = po.getString("payOrderId");    // 支付订单号
            Boolean executeNotify = po.getBooleanValue("executeNotify");   // 是否执行回调
            PayOrder payOrder = payOrderService.query(mchId, payOrderId, mchOrderNo, executeNotify);
            _log.info("{}查询支付订单,结果:{}", logPrefix, payOrder);
            if (payOrder == null) {
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付订单不存在", null, null));
            }
            Map<String, Object> map = buildRetMap(payOrder);
            _log.info("###### 商户查询订单处理完成 ######");
            return XXPayUtil.makeRetData(map, payContext.getString("key"));
        } catch (Exception e) {
            _log.error(e, "");
            return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付中心系统异常", null, null));
        }
    }

    /**
     * 构建返回Map
     *
     * @param payOrder
     * @return
     */
    Map buildRetMap(PayOrder payOrder) {
        Map<String, Object> map = new HashedMap();
        map.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        map.put("mchId", StrUtil.toString(payOrder.getMchId()));
        map.put("appId", StrUtil.toString(payOrder.getAppId()));
        map.put("productId", StrUtil.toString(payOrder.getProductId()));
        map.put("payOrderId", StrUtil.toString(payOrder.getPayOrderId()));
        map.put("mchOrderNo", StrUtil.toString(payOrder.getMchOrderNo()));
        map.put("amount", StrUtil.toString(payOrder.getAmount()));
        map.put("currency", StrUtil.toString(payOrder.getCurrency()));
        map.put("status", StrUtil.toString(payOrder.getStatus()));
        map.put("channelUser", StrUtil.toString(payOrder.getChannelUser()));
        map.put("channelOrderNo", StrUtil.toString(payOrder.getChannelOrderNo()));
        map.put("channelAttach", "".equals(StrUtil.toString(payOrder.getChannelAttach())) ? "" : JSONObject.parse(payOrder.getChannelAttach()));
        map.put("paySuccTime", "".equals(StrUtil.toString(payOrder.getPaySuccTime())) ? "" : payOrder.getPaySuccTime().getTime());
        return map;
    }

    /**
     * 验证创建订单请求参数,参数通过返回JSONObject对象,否则返回错误文本信息
     *
     * @param params
     * @return
     */
    private String validateParams(JSONObject params, JSONObject payContext) {
        // 验证请求参数,参数有问题返回错误提示
        String errorMessage;
        // 支付参数
        String mchId = params.getString("mchId");                // 商户ID
        String appId = params.getString("appId");               // 应用ID
        String mchOrderNo = params.getString("mchOrderNo");    // 商户订单号
        String payOrderId = params.getString("payOrderId");    // 支付订单号

        String sign = params.getString("sign");                // 签名

        // 验证请求参数有效性（必选项）
        Long mchIdL;
        if (StringUtils.isBlank(mchId) || !NumberUtils.isDigits(mchId)) {
            errorMessage = "request params[mchId] error.";
            return errorMessage;
        }
        mchIdL = Long.parseLong(mchId);

        if (StringUtils.isBlank(mchOrderNo) && StringUtils.isBlank(payOrderId)) {
            errorMessage = "request params[mchOrderNo or payOrderId] error.";
            return errorMessage;
        }

        // 签名信息
        if (StringUtils.isEmpty(sign)) {
            errorMessage = "request params[sign] error.";
            return errorMessage;
        }

        // 查询商户信息
        MchInfo mchInfo = rpcCommonService.rpcMchInfoService.findByMchId(mchIdL);
        if (mchInfo == null) {
            errorMessage = "Can't found mchInfo[mchId=" + mchId + "] record in db.";
            return errorMessage;
        }
        if (mchInfo.getStatus() != MchConstant.PUB_YES) {
            errorMessage = "mchInfo not available [mchId=" + mchId + "] record in db.";
            return errorMessage;
        }
        // 查询应用信息
        if (StringUtils.isNotBlank(appId)) {
            MchApp mchApp = rpcCommonService.rpcMchAppService.findByMchIdAndAppId(mchIdL, appId);
            if (mchApp == null) {
                errorMessage = "Can't found app[appId=" + appId + "] record in db.";
                return errorMessage;
            }
            if (mchApp.getStatus() != MchConstant.PUB_YES) {
                errorMessage = "app not available [appId=" + appId + "] record in db.";
                return errorMessage;
            }
        }

        String key = mchInfo.getPrivateKey();
        if (StringUtils.isBlank(key)) {
            errorMessage = "key is null[mchId=" + mchId + "] record in db.";
            return errorMessage;
        }
        payContext.put("key", key);

        // 验证签名数据
        boolean verifyFlag = XXPayUtil.verifyPaySign(params, key);
        if (!verifyFlag) {
            errorMessage = "Verify XX pay sign failed.";
            return errorMessage;
        }

        return "success";
    }

    /**
     * 查询上游订单信息:
     * 1)先验证接口参数以及签名信息
     *
     * @param request
     * @return
     */
    @RequestMapping("/api/pay/queryUpperOrder")
    @ResponseBody
    public String queryUpperOrder(HttpServletRequest request, HttpServletResponse response) {
        _log.info("######上游订单请求 ######");
        String logPrefix = "【上游订单请求】";
        String myKey = "4F6B1A7BE25A3D8FB3E1149213EDF60D";
        try {
            JSONObject po = getJsonParam(request);
            _log.info("{}请求参数:{}", logPrefix, po);
            JSONObject payContext = new JSONObject();
            PayOrder payOrder = null;
            // 验证参数有效性
            Object object = null;

            SortedMap map = new TreeMap();
            String orderId = request.getParameter("orderId");
            String dateTemp = request.getParameter("dateTemp");//时间戳
            String mchOrderId = request.getParameter("mchOrderId");
            String reqSign1 = request.getParameter("sign");

            map.put("orderId", orderId);
            map.put("dateTemp", dateTemp);
            map.put("mchOrderId", mchOrderId);
            String sign = PayDigestUtil.getSign(map, myKey).toUpperCase();//内部私用Key
            //签名验证
            if (!sign.equals(reqSign1)) {
                _log.info("{}参数校验不通过:{}", logPrefix, "请求失败，验证签名不通过!");
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "请求失败，验证签名不通过!", null, PayEnum.ERR_0121.getCode(), ""));
            }

            //订单信息验证
            payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(orderId);
            if (payOrder == null) {
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "本地订单信息不存在", null, PayEnum.ERR_0112.getCode(), "本地订单信息不存在"));
            }

            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            try {
                paymentInterface = (PaymentInterface) SpringUtil.getBean(channelName.toLowerCase() + "PaymentService");
            } catch (BeansException e) {
                _log.error(e, "支付渠道类型[channelId=" + channelId + "]实例化异常");
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "调用支付渠道失败", null, PayEnum.ERR_0010.getCode(), "支付渠道类型[channelId=" + channelId + "]实例化异常"));
            }

            // 执行支付
            JSONObject retObj = paymentInterface.query(payOrder);
            Map resMap = new HashMap();
            resMap.put("retMsg", retObj.getString("msg"));
            resMap.put("retCode", retObj.getString("status"));
            resMap.put("dateTemp", DateUtil.getSeqString());
            String resSign = PayDigestUtil.getSign(map, "4F6B1A7BE25A3D8FB3E1149213EDF60D");
            resMap.put("sign", sign.toUpperCase());
            _log.info("{}查询上游支付订单,渠道:{} 结果:{}", logPrefix, channelId, retObj.toJSONString());

            return JSON.toJSONString(resMap);
        } catch (Exception e) {
            _log.error(e, "");
            return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付中心系统异常", null, PayEnum.ERR_0010.getCode(), "请联系技术人员查看"));
        }
    }


    /**
     * 商家分账关系绑定
     *
     * @return
     */
    @RequestMapping("/api/pay/relationBind")
    @ResponseBody
    private String relationBind(HttpServletRequest request) {

        try {
            JSONObject param = getJsonParam(request);
            _log.info("商家分账关系绑定，接收参数：" + param.toJSONString());
            PayCashCollConfig payProduct = getObject(param, PayCashCollConfig.class);
            cashCollInterface = (CashCollInterface) SpringUtil.getBean("alipayCashCollService");
            _log.info("商家分账关系绑定=>111111111111111");
            JSONObject jsonObject = cashCollInterface.relationbind(payProduct);
            _log.info("商家分账关系绑定=>22222222222222222222");
            return jsonObject.toJSONString();

        } catch (Exception ex) {
            _log.error(ex, ex.getMessage());
            return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "绑定商家分账关系发生异常", null, PayEnum.ERR_0010.getCode(), "请联系技术人员查看"));
        }
    }
}
