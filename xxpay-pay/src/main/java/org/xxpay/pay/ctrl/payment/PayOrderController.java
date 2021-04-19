package org.xxpay.pay.ctrl.payment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.constant.PayEnum;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.*;
import org.xxpay.core.common.vo.OrderCostFeeVO;
import org.xxpay.core.entity.*;
import org.xxpay.pay.channel.PaymentInterface;
import org.xxpay.pay.channel.alipay.AlipayConfig;
import org.xxpay.pay.ctrl.common.BaseController;
import org.xxpay.pay.mq.rocketmq.normal.BaseNotify5CashColl;
import org.xxpay.pay.service.PayOrderService;
import org.xxpay.pay.service.RpcCommonService;
import org.xxpay.pay.util.SpringUtil;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author dingzhiwei jmdhappy@126.com
 * @version V1.0
 * @Description: 支付订单, 包括:统一下单,订单查询,补单等接口
 * @date 2017-07-05
 * @Copyright: www.xxpay.org
 */
@Controller
public class PayOrderController extends BaseController {

    private final MyLog _log = MyLog.getLog(PayOrderController.class);

    @Autowired
    private RpcCommonService rpcCommonService;

    @Qualifier("aaapayPaymentService")
    @Autowired
    private PaymentInterface paymentInterface;

    @Autowired
    private PayOrderService payOrderService;

    /**
     * 统一下单接口:
     * 1)先验证接口参数以及签名信息
     * 2)验证通过创建支付订单/api/pay/create_order
     * 3)根据商户选择渠道,调用支付服务进行下单
     * 4)返回下单数据
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/pay/create_order")
    @ResponseBody
    public String payOrder(HttpServletRequest request, HttpServletResponse response) {
        _log.info("###### 开始接收商户统一下单请求 ######");
        String logPrefix = "【商户统一下单】";
        try {
            JSONObject po = getJsonParam(request);
            _log.info("{}请求参数:{}", logPrefix, po);
            JSONObject payContext = new JSONObject();
            PayOrder payOrder = null;
            // 验证参数有效性
            Object object = validateParams(po, payContext, request);
            if (object instanceof String) {
                _log.info("{}参数校验不通过:{}", logPrefix, object);
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, object.toString(), null, PayEnum.ERR_0014.getCode(), object.toString()));
            }
            if (object instanceof PayOrder) payOrder = (PayOrder) object;
            if (payOrder == null)
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付中心下单失败", null, PayEnum.ERR_0010.getCode(), "生成支付订单失败"));

            String channelId = payOrder.getChannelId();
            String channelName = channelId.substring(0, channelId.indexOf("_"));
            try {
                paymentInterface = (PaymentInterface) SpringUtil.getBean(channelName.toLowerCase() + "PaymentService");
            } catch (BeansException e) {
                _log.error(e, "支付渠道类型[channelId=" + channelId + "]实例化异常");
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "调用支付渠道失败", null, PayEnum.ERR_0010.getCode(), "支付渠道类型[channelId=" + channelId + "]实例化异常"));
            }
            // 如果该通道重新定义了订单号,那么使用新的订单号
            String orderId = paymentInterface.getOrderId(payOrder);
            if (StringUtils.isNotBlank(orderId)) payOrder.setPayOrderId(orderId);
            // 如果该通道重新设置订单金额，那么重写订单金额及分润
            Long newAmount = paymentInterface.getAmount(payOrder);
            if (newAmount != null) { // 通道实现了getAmount方法
                if (newAmount == -1) {   // 表示当前金额不可用，需更换金额重新下单
                    return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付中心下单失败", null, PayEnum.ERR_0010.getCode(), "请更换金额重新下单"));
                } else if (newAmount > 0) {
                    payOrder.setAmount(newAmount);
                    // 重新计算订单:渠道成本费用,代理商费用,商户入账,平台利润
                    OrderCostFeeVO orderCostFeeVO = XXPayUtil.calOrderCostFeeAndIncome(newAmount, payOrder.getChannelRate(), payOrder.getAgentRate(), payOrder.getParentAgentRate(), payOrder.getMchRate());
                    // 重新设置渠道成本及分润
                    payOrder.setChannelCost(orderCostFeeVO.getChannelCostFee());
                    payOrder.setPlatProfit(orderCostFeeVO.getPlatProfit());
                    payOrder.setAgentProfit(orderCostFeeVO.getAgentProfit());
                    payOrder.setParentAgentProfit(orderCostFeeVO.getParentAgentProfit());
                    payOrder.setMchIncome(orderCostFeeVO.getMchIncome());
                }
            }
            int result = rpcCommonService.rpcPayOrderService.createPayOrder(payOrder);
            _log.info("{}创建支付订单,结果:{}", logPrefix, result);
            if (result != 1) {
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付中心下单失败", null, PayEnum.ERR_0010.getCode(), "DB插入支付订单失败"));
            }

            // 执行支付
            JSONObject retObj = paymentInterface.pay(payOrder);

            if (retObj.get(PayConstant.RETURN_PARAM_RETCODE).equals(PayConstant.RETURN_VALUE_SUCCESS)) {
                retObj.put("payOrderId", payOrder.getPayOrderId());
                // 使用StringEscapeUtils.unescapeJava去掉字符串中的转义符号(不采用,会导致json解析报错)
                //return StringEscapeUtils.unescapeJava(XXPayUtil.makeRetData(retObj, payContext.getString("key")));
                return XXPayUtil.makeRetData(retObj, payContext.getString("key"));
            } else {
                //todo  retObj.getString("errDes")+retObj.get(PayConstant.RETURN_PARAM_RETMSG)
               String retMsg= retObj.get(PayConstant.RETURN_PARAM_RETMSG)==null ?"":retObj.get(PayConstant.RETURN_PARAM_RETMSG).toString();
                String errDes= StringUtils.isNotBlank(retObj.getString("errDes"))?retObj.getString("errDes"):"" ;
                _log.info("下单失败的错误信息：" + retMsg);
                PayOrder payOrders = new PayOrder();
                payOrders.setErrMsg(errDes+retMsg);
                payOrders.setPayOrderId(payOrder.getPayOrderId());
                rpcCommonService.rpcPayOrderService.updateCodeAndErrorMessage(payOrders);
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL,
                        "调用支付渠道失败" + (retObj.get(PayConstant.RETURN_PARAM_RETMSG) == null ? "" : ("(" + retObj.get(PayConstant.RETURN_PARAM_RETMSG) + ")")),
                        null, retObj.getString("errCode"), retObj.getString("errDes")));
            }
        } catch (Exception e) {
            _log.error(e, "");
            return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付中心系统异常", null, PayEnum.ERR_0010.getCode(), "请联系技术人员查看"));
        }
    }


    /**
     * 服务端跳转
     *
     * @param request
     * @return
     * @throws ServletException
     * @throws IOException
     */
    @RequestMapping("/api/jump.htm")
    public String toPay(HttpServletRequest request, ModelMap model) throws ServletException, IOException {
        String params = request.getParameter("params");
        if (StringUtils.isNotBlank(params)) {
            String jumpForm = new String(MyBase64.decode(params));
            model.put("jumpForm", jumpForm);
        } else {
            model.put("jumpForm", "跳转出现异常,请联系管理员.");
        }
        return "payment/jump";
    }

    /**
     * 验证创建订单请求参数,参数通过返回JSONObject对象,否则返回错误文本信息
     *
     * @param params
     * @return
     */
    private Object validateParams(JSONObject params, JSONObject payContext, HttpServletRequest request) {
        String riskLog = "[支付风控]";
        // 验证请求参数,参数有问题返回错误提示
        String errorMessage;
        // 支付参数
        String mchId = params.getString("mchId");                // 商户ID
        String appId = params.getString("appId");              // 应用ID
        String productId = params.getString("productId");      // 支付产品ID
        String mchOrderNo = params.getString("mchOrderNo");    // 商户订单号
        String amount = params.getString("amount");            // 支付金额（单位分）
        String currency = params.getString("currency");        // 币种
        String clientIp = params.getString("clientIp");        // 客户端IP
        String device = params.getString("device");            // 设备
        String extra = params.getString("extra");                // 特定渠道发起时额外参数
        String param1 = params.getString("param1");            // 扩展参数1
        String param2 = params.getString("param2");            // 扩展参数2
        String returnUrl = params.getString("returnUrl");        // 支付结果同步请求url
        String notifyUrl = params.getString("notifyUrl");        // 支付结果回调URL
        String sign = params.getString("sign");                // 签名
        String subject = params.getString("subject");            // 商品主题
        String body = params.getString("body");                    // 商品描述信息
        String payPassAccountId = params.getString("payPassAccountId"); // 支付通道子账户ID,非必填

        // 验证请求参数有效性（必选项）
        Long mchIdL;
        if (StringUtils.isBlank(mchId) || !NumberUtils.isDigits(mchId)) {
            errorMessage = "请求参数[mchId]不能为空且为数值类型.";
            return errorMessage;
        }

        mchIdL = Long.parseLong(mchId);
        // 查询商户信息
        MchInfo mchInfo = rpcCommonService.rpcMchInfoService.findByMchId(mchIdL);
        if (mchInfo == null) {
            errorMessage = "商户不存在[mchId=" + mchId + "].";
            return errorMessage;
        }
        if (mchInfo.getStatus() != MchConstant.PUB_YES) {
            errorMessage = "商户状态不可用[mchId=" + mchId + "].";
            return errorMessage;
        }
        // 判断请求IP是否允许
        String clintIp = IPUtility.getRealIpAddress(request);
        boolean isAllow = XXPayUtil.ipAllow(clintIp, mchInfo.getPayWhiteIp(), mchInfo.getPayBlackIp());
        if (!isAllow) {
            errorMessage = "IP[" + clintIp + "]不允许访问";
            return errorMessage;
        }

        Integer productIdI = null;
        if (StringUtils.isBlank(productId) || !NumberUtils.isDigits(productId)) {
            errorMessage = "请求参数[productId]不能为空且为数值类型.";
            return errorMessage;
        }

        productIdI = Integer.parseInt(productId);

        if (StringUtils.isBlank(mchOrderNo)) {
            errorMessage = "请求参数[mchOrderNo]不能为空.";
            return errorMessage;
        }

        if (!NumberUtils.isDigits(amount)) {
            errorMessage = "请求参数[amount]应为数值类型.";
            return errorMessage;
        }
        Long amountL = Long.parseLong(amount);
        if (amountL <= 0) {
            errorMessage = "请求参数[amount]必须大于0.";
            return errorMessage;
        }
        if (StringUtils.isBlank(currency)) {
            errorMessage = "请求参数[currency]不能为空.";
            return errorMessage;
        }
        if (StringUtils.isBlank(notifyUrl)) {
            errorMessage = "请求参数[notifyUrl]不能为空.";
            return errorMessage;
        }
        if (StringUtils.isBlank(subject)) {
            errorMessage = "请求参数[subject]不能为空.";
            return errorMessage;
        }
        if (StringUtils.isBlank(body)) {
            errorMessage = "请求参数[body]不能为空.";
            return errorMessage;
        }
        if (StringUtils.isBlank(clientIp)) {
            clientIp = IPUtility.getRealIpAddress(request);
        }
        String channelUser = "";

        // 签名信息
        if (StringUtils.isEmpty(sign)) {
            errorMessage = "请求参数[sign]不能为空.";
            return errorMessage;
        }

        // 查询应用信息
        if (StringUtils.isNotBlank(appId)) {
            MchApp mchApp = rpcCommonService.rpcMchAppService.findByMchIdAndAppId(mchIdL, appId);
            if (mchApp == null) {
                errorMessage = "应用不存在[appId=" + appId + "]";
                return errorMessage;
            }
            if (mchApp.getStatus() != MchConstant.PUB_YES) {
                errorMessage = "应用状态不可用[appId=" + appId + "]";
                return errorMessage;
            }
        }

        String key = mchInfo.getPrivateKey();
        if (StringUtils.isBlank(key)) {
            errorMessage = "商户私钥为空,请配置商户私钥[mchId=" + mchId + "].";
            return errorMessage;
        }
        payContext.put("key", key);

        // 查询商户对应的支付渠道
        String channelMchId;
        String channelType;
        String channelId;
        BigDecimal channelRate;
        BigDecimal mchRate;
        BigDecimal parentAgentRate = null;
        BigDecimal agentRate = null;
        BigDecimal qrAgentRate = null;//码商代理费率
        Integer passageAccountId;
        PayProduct product = rpcCommonService.rpcPayProductService.findById(productIdI);
        if (product != null && product.getStatus() == 0) {
            errorMessage = "该产品的支付通道[productId=" + productId + "]，已维护";
            return errorMessage;
        }

        MchGroupPayPassage mchGroupPayPassage = null;
        if (mchInfo.getGroupId() != null && mchInfo.getGroupId().intValue() != 0) {
            mchGroupPayPassage = rpcCommonService.rpcMchGroupPayPassageService.findByMchGroupIdAndProductId(mchInfo.getGroupId().longValue(), productIdI);
        }

        MchPayPassage mchPayPassage = rpcCommonService.rpcMchPayPassageService.findByMchIdAndProductId(mchIdL, productIdI);
        if (mchPayPassage == null && mchGroupPayPassage == null) {
            errorMessage = "没有该产品的支付通道[productId=" + productId + ",mchId=" + mchId + "]";
            return errorMessage;
        }

        // 支付通道ID
        Integer payPassageId = null;
        PayPassageAccount payPassageAccount = null;
        if (StringUtils.isNotBlank(payPassAccountId) && NumberUtils.isDigits(payPassAccountId)) {
            payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(Integer.parseInt(payPassAccountId));
            if (payPassageAccount == null || payPassageAccount.getStatus() != MchConstant.PUB_YES) {
                errorMessage = "传入子账户不可用[payPassAccountId=" + payPassAccountId + ",mchId=" + mchId + "]";
                return errorMessage;
            }
            payPassageId = payPassageAccount.getPayPassageId();
        } else {
            // 获取通道子账户（商户）
            Object obj = payOrderService.getPayPassageAccount(mchPayPassage, riskLog, amountL);
            if (obj instanceof String) {
                if (payPassageId == null) {
                    //商户组通道获取
                    Object objGroup = payOrderService.getGroupPayPassageAccount(mchGroupPayPassage, riskLog, amountL);
                    if (objGroup instanceof String) {
                        return objGroup;
                    }

                    if (objGroup instanceof PayPassageAccount) {
                        payPassageAccount = (PayPassageAccount) objGroup;
                        payPassageId = payPassageAccount.getPayPassageId();
                    }
                }

            }
            if (obj instanceof PayPassageAccount) {
                payPassageAccount = (PayPassageAccount) obj;
                payPassageId = payPassageAccount.getPayPassageId();
            }
        }


        // 判断支付通道
        if (payPassageId == null) {
            errorMessage = "无法取得可用的支付通道[productId=" + productId + ",mchId=" + mchId + "]";
            return errorMessage;
        }

        // 判断子账户
        if (payPassageAccount == null) {
            errorMessage = "该支付通道没有可用子账户[payPassageId=" + payPassageId + "]";
            return errorMessage;
        }

        passageAccountId = payPassageAccount.getId();
        channelMchId = payPassageAccount.getPassageMchId();
        channelType = payPassageAccount.getIfTypeCode();
        channelId = payPassageAccount.getIfCode();
        channelRate = payPassageAccount.getPassageRate();
        if (mchPayPassage != null) {
            mchRate = mchPayPassage.getMchRate();
        } else {
            mchRate = mchGroupPayPassage.getMchRate();
        }

        // 处理二级代理商
        Long agentId = mchInfo.getAgentId();
        Long parentAgentId = mchInfo.getParentAgentId();
        if (agentId != null) {
            AgentPassage agentPassage = rpcCommonService.rpcAgentPassageService.findByAgentIdAndProductId(agentId, productIdI);
            if (agentPassage != null && agentPassage.getStatus() == MchConstant.PUB_YES) {
                agentRate = agentPassage.getAgentRate();
            }

            if (agentRate == null) {
                errorMessage = "请设置二级代理商费率";
                return errorMessage;
            }

            //处理一级代理商
            if (parentAgentId != null && parentAgentId != 0) {
                AgentPassage agentPassage2 = rpcCommonService.rpcAgentPassageService.findByAgentIdAndProductId(parentAgentId, productIdI);
                if (agentPassage2 != null && agentPassage2.getStatus() == MchConstant.PUB_YES) {
                    parentAgentRate = agentPassage2.getAgentRate();
                }
                if (parentAgentRate == null) {


                    errorMessage = "请设置一级代理商费率";
                    return errorMessage;
                }
            }
        } else {

            if (payPassageAccount.getAgentId() != null) {

                AgentInfo agentInfo = rpcCommonService.rpcAgentInfoService.findByAgentId(payPassageAccount.getAgentId());
                if (agentInfo != null) {
                    agentId = agentInfo.getAgentId();
                    agentRate = agentInfo.getOffRechargeRate();
                    if (agentInfo.getParentAgentId() != null && agentInfo.getParentAgentId() > 0) {
                        //如果是二级代理，代理ID存总代理的
                        agentId = agentInfo.getParentAgentId();
                    }

                }
            }
        }

        if (channelType == null || channelId == null) {
            errorMessage = "商户没有该产品的支付通道[productId=" + productId + ",mchId=" + mchId + ",channelType=" + channelType + ",channelId=" + channelId + "]";
            return errorMessage;
        }


        // 根据不同渠道,判断extra参数
        if (PayConstant.PAY_CHANNEL_WX_JSAPI.equalsIgnoreCase(channelId)) {
            if (StringUtils.isEmpty(extra)) {
                errorMessage = "request params[extra] error.";
                return errorMessage;
            }
            JSONObject extraObject = JSON.parseObject(extra);
            String openId = extraObject.getString("openId");
            if (StringUtils.isBlank(openId)) {
                errorMessage = "request params[extra.openId] error.";
                return errorMessage;
            }
            channelUser = openId;
        } else if (PayConstant.PAY_CHANNEL_WX_MWEB.equalsIgnoreCase(channelId)) {
            if (StringUtils.isEmpty(extra)) {
                errorMessage = "请求参数[extra]不能为空.";
                return errorMessage;
            }
            JSONObject extraObject = JSON.parseObject(extra);
            String sceneInfo = extraObject.getString("sceneInfo");
            if (StringUtils.isBlank(sceneInfo)) {
                errorMessage = "请求参数[extra.sceneInfo]不能为空.";
                return errorMessage;
            }
            if (StringUtils.isBlank(clientIp)) {
                errorMessage = "请求参数[clientIp]不能为空.";
                return errorMessage;
            }
        } else if (PayConstant.PAY_CHANNEL_ACCOUNTPAY_BALANCE.equalsIgnoreCase(channelId)) {
            if (StringUtils.isEmpty(extra)) {
                errorMessage = "请求参数[extra]不能为空.";
                return errorMessage;
            }
            JSONObject extraObject = JSON.parseObject(extra);
            String userId = extraObject.getString("userId");
            if (StringUtils.isBlank(userId)) {
                errorMessage = "请求参数[extra.userId]不能为空.";
                return errorMessage;
            }
            channelUser = userId;
        }

        // 验证签名数据
        boolean verifyFlag = XXPayUtil.verifyPaySign(params, key);
        if (!verifyFlag) {
            errorMessage = "验证签名失败.";
            return errorMessage;
        }

        // 验证参数通过,返回JSONObject对象
        PayOrder payOrder = new PayOrder();
        payOrder.setPayOrderId(MySeq.getPay());
        payOrder.setMchId(mchIdL);
        payOrder.setMchType(mchInfo.getType());
        payOrder.setAppId(appId);
        payOrder.setMchOrderNo(mchOrderNo);
        payOrder.setAgentId(agentId);
        payOrder.setParentAgentId(parentAgentId);
        payOrder.setProductId(productIdI);                          // 支付产品ID
        if (mchPayPassage != null) {
            payOrder.setProductType(mchPayPassage.getProductType());    // 产品类型
        } else {
            payOrder.setProductType(mchGroupPayPassage.getProductType());    // 产品类型
        }

        payOrder.setPassageId(payPassageId);                        // 支付通道ID
        payOrder.setPassageAccountId(passageAccountId);             // 支付通道账户ID
        payOrder.setChannelType(channelType);
        payOrder.setChannelId(channelId);
        payOrder.setAmount(amountL);
        payOrder.setCurrency(currency);
        payOrder.setClientIp(clientIp);
        payOrder.setDevice(device);
        payOrder.setSubject(subject);
        payOrder.setBody(body);
        payOrder.setExtra(extra);
        payOrder.setChannelMchId(channelMchId);
        payOrder.setChannelUser(channelUser);
        // 设置费率
        payOrder.setChannelRate(channelRate);
        payOrder.setAgentRate(agentRate);
        payOrder.setParentAgentRate(parentAgentRate);
        payOrder.setMchRate(mchRate);
        // 计算订单:渠道成本费用,代理商费用,商户入账,平台利润
        OrderCostFeeVO orderCostFeeVO = XXPayUtil.calOrderCostFeeAndIncome(amountL, channelRate, agentRate, parentAgentRate, mchRate);
        // 设置渠道成本及分润
        payOrder.setChannelCost(orderCostFeeVO.getChannelCostFee());
        payOrder.setPlatProfit(orderCostFeeVO.getPlatProfit());
        payOrder.setAgentProfit(orderCostFeeVO.getAgentProfit());
        payOrder.setParentAgentProfit(orderCostFeeVO.getParentAgentProfit());
        payOrder.setMchIncome(orderCostFeeVO.getMchIncome());
        AlipayConfig alipayConfig = new AlipayConfig(payPassageAccount.getParam());
        payOrder.setParam1(alipayConfig.getAppId());
        payOrder.setParam2(param2);
        payOrder.setNotifyUrl(notifyUrl);
        payOrder.setReturnUrl(returnUrl);
        return payOrder;
    }


    @Autowired
    private BaseNotify5CashColl baseNotify5CashColl;


    /**
     * 订单补发分账:
     * 1)先验证接口参数以及签名信息
     *
     * @param request
     * @return
     */
    @RequestMapping("/api/pay/orderfenzhang")
    @ResponseBody
    public String orderfenzhang(HttpServletRequest request, HttpServletResponse response) {
        _log.info("######订单分账请求 ######");
        String logPrefix = "【订单分账请求】";
        String myKey = "4F6B1A7BE25A3D8FB3E1149213EDF60D";
        try {
            JSONObject po = getJsonParam(request);
            _log.info("{}请求参数:{}", logPrefix, po);

            PayOrder payOrder = null;
            // 验证参数有效性
            SortedMap map = new TreeMap();
            String orderId = request.getParameter("orderId");
            String dateTemp = request.getParameter("dateTemp");//时间戳
            String reqSign1 = request.getParameter("sign");

            map.put("orderId", orderId);
            map.put("dateTemp", dateTemp);
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

            //发起再次分账
            baseNotify5CashColl.doNotify(orderId);

            // 执行支付
            JSONObject retObj = new JSONObject();
            Map resMap = new HashMap();
            resMap.put("retMsg", "发送成功");
            resMap.put("retCode", PayConstant.RETURN_VALUE_SUCCESS);
            resMap.put("dateTemp", DateUtil.getSeqString());
            String resSign = PayDigestUtil.getSign(map, "4F6B1A7BE25A3D8FB3E1149213EDF60D");
            resMap.put("sign", sign.toUpperCase());
            _log.info("{}支付订单:{},结果:{}", logPrefix, orderId, retObj.toJSONString());
            return JSON.toJSONString(resMap);
        } catch (Exception e) {
            _log.error(e, "");
            return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付中心系统异常", null, PayEnum.ERR_0010.getCode(), "请联系技术人员查看"));
        }
    }


    private final String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAOnpyVEOvANpRu8lqwhLLxM+oFCDA6DmiReD6v2rKwkSz/s+k88QdN4qaDgdT66PCQGIomPU7N0mN3tJXiWEo8mUxAUD7jyZkDbysyGot9JEbAtDdWrmIe3UE8MvDAW02ahBfm4ZrT8E6mt4Lzqp8aAQ0FqsFJkR1XbgpJ/ITDjtAgMBAAECgYEA15mK21GPbj19CjRX/p791ukkbtEzaPzUY35N/F3mnsheNx+osXRjo9rGkOJDbYudK3K66vV5FSWCgfpP8pjdNM7ycahKwFg952MdtNpty3zpyFDkcWeMjVBSso5wtyqvgILfr4qgS3aHMintLSwSQtUjt5DhulzSyX3Z5YsAI8ECQQD2an3sAsSz4z6RfGlIr5tJc5Mi300btwpROGNmUVxAwbqpqq0UeSOgJEzmoMburS7X/v82Dn4QIfGQc0FZk3mlAkEA8wLOsgUUfsJfp7zcdxwD135EDnYm9jRib2vk9DBGulKV1MkbPMF8Z/3yn9UySH8b8pm3y7o7Ur5iuFb84xtPqQJBANQufp9rAtWjJ40/A6mDDMQCsP+mKE9lHY0ycOT5yeY46vKN9NtcNEEBAPbWGnYKyftTp450jDh4AfnQRMVNJ8ECQQCZ8eRZGCjEqIQKcfVEK2YvpJiehLDn9YWKSlKPcunLbTfnxcLQeU5DXrfOEzQ4gvWEeWba085y+5L0bn7jrFCJAkACF7+rox6W+wkuTfrSp4JAf0brfWwJhTV9QhKAYOzPjHU3xQHPcLq8h9rUitBbo1k8gfP+CW0lve7OZf0ecJl5";
    private final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDp6clRDrwDaUbvJasISy8TPqBQgwOg5okXg+r9qysJEs/7PpPPEHTeKmg4HU+ujwkBiKJj1OzdJjd7SV4lhKPJlMQFA+48mZA28rMhqLfSRGwLQ3Vq5iHt1BPDLwwFtNmoQX5uGa0/BOpreC86qfGgENBarBSZEdV24KSfyEw47QIDAQAB";

    /**
     * 支付宝资金归集号，客服端监听反馈接口
     *
     * @param
     * @return
     */

    @RequestMapping("/api/pay/alipayMonitor")
    @ResponseBody
    public void aliMonitor(@RequestBody String data) {
        StringBuffer stringBuffer = new StringBuffer();
        JSONObject retObj = new JSONObject();
        try {
            stringBuffer.append("######支付宝资金归集号，客服端监听反馈接口 ######");
            String logPrefix = "【支付宝资金归集号，客服端监听反馈接口】";
            // 验证参数有效性
            SortedMap map = new TreeMap();
            String reqData = JSONObject.parseObject(data).getString("data");
            reqData = reqData.replace("\\n", "").replace(" ", "+");
            byte[] reqDataByte = Base64Utils.decode(reqData);
            byte[] decodedData = RSAUtils.decryptByPrivateKey(reqDataByte, privateKey);
            String reqDataJson = new String(decodedData);
            stringBuffer.append("" + logPrefix + ",请求明文=>" + reqDataJson);

            JSONObject jsonObject = JSONObject.parseObject(reqDataJson);

            String userId = jsonObject.getString("userId");//客服端监听着标识ID
            String status = jsonObject.getString("status");//通知状态 0:非正常收款，1：正常收款
            String dateTemp = jsonObject.getString("dateTemp");//时间戳
            PayCashCollConfig payCashCollConfig = new PayCashCollConfig();
            payCashCollConfig.setTransInUserId(userId);
            payCashCollConfig.setStatus((byte) 1);
            List<PayCashCollConfig> payCashCollConfigs = rpcCommonService.rpcPayCashCollConfigService.selectAll(payCashCollConfig);
//            //判断请求过来的客服端监听着标识ID是否存在
//            if (payCashCollConfigs == null || payCashCollConfigs.size() == 0) {
//                stringBuffer.append("根据客服端监听的标识ID查询未找到资金归集号。标识ID：" + userId);
//                String sendMsg = MessageFormat.format("预警消息=>根据客服端监听的标识ID查询未找到资金归集号。标识ID：{0}。请运营人员核查改该收款方ID是否正在做收款操作或者联系该收款方ID所属着核查,配置是否正确！时间:{1}", userId, DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD_HH_MM_SS));
//                TelegramUtil.SendMsg(sendMsg);
//                return;
//            }
//
//            //如果等于0,标识非正常收款
//            if (status.equals("0")) {
//                String sendMsg = MessageFormat.format("预警消息=>资金归集号为[{0}]的,在最近10秒内没有收款信息，请运营人员根据当前并发量判断是否正常,如果觉得不正常，请先关闭通道账号再进行核实！时间:{1}", payCashCollConfigs.get(0).getRemark());
//                TelegramUtil.SendMsg(sendMsg);
//                return;
//            }

            return;
        } catch (Exception ex) {
//            String sendMsg = MessageFormat.format("预警消息=>支付宝资金归集号，客服端监听反馈接口。发生异常,异常信息：{0}", ex.getStackTrace() + ex.getMessage());
//            TelegramUtil.SendMsg(sendMsg);
//            stringBuffer.append("异常：" + ex.getStackTrace() + ex.getMessage());
            return;
        } finally {
            _log.info(stringBuffer.toString());
        }

    }

    public static void main(String[] args) {
        System.out.println(MySeq.getSeqString());
    }

}
