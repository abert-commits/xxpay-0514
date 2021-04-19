package org.xxpay.manage.order.ctrl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxpay.core.common.annotation.MethodLog;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.BizResponse;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayOrderExport;
import org.xxpay.core.entity.PayPassage;
import org.xxpay.manage.common.config.MainConfig;
import org.xxpay.manage.common.service.RpcCommonService;
import org.xxpay.manage.common.ctrl.BaseController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Controller
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/pay_order")
public class PayOrderController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;


    private String payUrl;

    @Autowired
    private static final MyLog _log = MyLog.getLog(PayOrderController.class);


    /**
     * 查询单条支付记录
     *
     * @return
     */
    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String payOrderId = getStringRequired(param, "payOrderId");
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payOrder));
    }

    /**
     * 支付订单记录列表
     *
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {

        JSONObject param = getJsonParam(request);
        Integer page = getInteger(param, "page");
        Integer limit = getInteger(param, "limit");
        Integer highest = getInteger(param, "highest");
        if (highest == null) {
            highest = 0;
        }
        PayOrder payOrder = getObject(param, PayOrder.class);
        if (payOrder.getMinAmount() != null) {
            payOrder.setMinAmount(payOrder.getMinAmount() * 100);
        }
        if (payOrder.getMaxAmount() != null) {
            payOrder.setMaxAmount(payOrder.getMaxAmount() * 100);
        }
        // 订单起止时间
        Date createTimeStart = null;
        Date createTimeEnd = null;
        //订单支付完成起止时间
        Date paySuccessTimeStart = null;
        Date paySuccessTimeEnd = null;

        String ymd = DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD);
        String createTimeStartStr = getString(param, "createTimeStart");
        if (StringUtils.isNotBlank(createTimeStartStr)) {
            createTimeStart = DateUtil.str2date(createTimeStartStr);
        } else {
            createTimeStart = DateUtil.str2date(ymd + " 00:00:00");
        }

        String createTimeEndStr = getString(param, "createTimeEnd");
        if (StringUtils.isNotBlank(createTimeEndStr)) createTimeEnd = DateUtil.str2date(createTimeEndStr);

        String paySuccessTimeStartStr = getString(param, "paySuccessTimeStart");
        if (StringUtils.isNotBlank(paySuccessTimeStartStr))
            paySuccessTimeStart = DateUtil.str2date(paySuccessTimeStartStr);
        String paySuccessTimeEndStr = getString(param, "paySuccessTimeEnd");
        if (StringUtils.isNotBlank(paySuccessTimeEndStr)) paySuccessTimeEnd = DateUtil.str2date(paySuccessTimeEndStr);

        if (StringUtils.isNotBlank(payOrder.getPayOrderId()) || StringUtils.isNotBlank(payOrder.getMchOrderNo())) {
            createTimeStart = null;
            createTimeEnd = null;
            paySuccessTimeStart = null;
            paySuccessTimeEnd = null;
        }


        int count = rpcCommonService.rpcPayOrderService.count(payOrder, createTimeStart, createTimeEnd, highest, paySuccessTimeStart, paySuccessTimeEnd);
        if (count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<PayOrder> payOrderList = rpcCommonService.rpcPayOrderService.select(
                (getPageIndex(page) - 1) * getPageSize(limit), getPageSize(limit), payOrder, createTimeStart, createTimeEnd, highest, paySuccessTimeStart, paySuccessTimeEnd);
        for (PayOrder item : payOrderList) {
            PayPassage passage = rpcCommonService.rpcPayPassageService.findById(item.getPassageId());
            item.setChannelId(passage == null ? "" : passage.getPassageName());
        }

        return ResponseEntity.ok(XxPayPageRes.buildSuccess(payOrderList, count));
    }

    /**
     * 导出支付订单数据 todo
     *
     * @return
     */
    @RequestMapping("/history_export")
    @ResponseBody
    public ResponseEntity<?> historyExpory(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer page = getInteger(param, "page");
        Integer limit = getInteger(param, "limit");
        Integer highest = getInteger(param, "highest");
        if (highest == null) {
            highest = 0;
        }
        PayOrder payOrder = getObject(param, PayOrder.class);
        if (payOrder.getMinAmount() != null) {
            payOrder.setMinAmount(payOrder.getMinAmount() * 100);
        }
        if (payOrder.getMaxAmount() != null) {
            payOrder.setMaxAmount(payOrder.getMaxAmount() * 100);
        }
        // 订单起止时间
        Date createTimeStart = null;
        Date createTimeEnd = null;
        //订单支付完成起止时间
        Date paySuccessTimeStart = null;
        Date paySuccessTimeEnd = null;

        String ymd = DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD);
        String createTimeStartStr = getString(param, "createTimeStart");
        if (StringUtils.isNotBlank(createTimeStartStr)) {
            createTimeStart = DateUtil.str2date(createTimeStartStr);
        } else {
            createTimeStart = DateUtil.str2date(ymd + " 00:00:00");
        }

        String createTimeEndStr = getString(param, "createTimeEnd");
        if (StringUtils.isNotBlank(createTimeEndStr)) createTimeEnd = DateUtil.str2date(createTimeEndStr);

        String paySuccessTimeStartStr = getString(param, "paySuccessTimeStart");
        if (StringUtils.isNotBlank(paySuccessTimeStartStr))
            paySuccessTimeStart = DateUtil.str2date(paySuccessTimeStartStr);
        String paySuccessTimeEndStr = getString(param, "paySuccessTimeEnd");
        if (StringUtils.isNotBlank(paySuccessTimeEndStr)) paySuccessTimeEnd = DateUtil.str2date(paySuccessTimeEndStr);

        if (StringUtils.isNotBlank(payOrder.getPayOrderId()) || StringUtils.isNotBlank(payOrder.getMchOrderNo())) {
            createTimeStart = null;
            createTimeEnd = null;
            paySuccessTimeStart = null;
            paySuccessTimeEnd = null;
        }

        int count = rpcCommonService.rpcPayOrderService.count(payOrder, createTimeStart, createTimeEnd, highest, paySuccessTimeStart, paySuccessTimeEnd);
        if (count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<PayOrder> payOrderList = rpcCommonService.rpcPayOrderService.select(0, count, payOrder, createTimeStart, createTimeEnd, highest, paySuccessTimeStart, paySuccessTimeEnd);
        for (PayOrder item : payOrderList) {
            PayPassage passage = rpcCommonService.rpcPayPassageService.findById(item.getPassageId());
            item.setPassageName(passage == null ? "" : passage.getPassageName());
            item.setParam1(DateUtil.date2Str(item.getCreateTime()));
            item.setParam2(DateUtil.date2Str(item.getPaySuccTime()));
            item.setAmount(item.getAmount() / 100);
            item.setMchIncome(item.getMchIncome() / 100);
        }

        return ResponseEntity.ok(XxPayPageRes.buildSuccess(payOrderList, count));
    }

    /**
     * 查询订单统计数据
     *
     * @return
     */
    @RequestMapping("/count")
    @ResponseBody
    public ResponseEntity<?> count(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String payOrderId = getString(param, "payOrderId");
        String mchOrderNo = getString(param, "mchOrderNo");
        Long productId = getLong(param, "productId");
        Long mchId = getLong(param, "mchId");
        Long passageId = getLong(param, "passageId");
        Long minAmount = null;
        Long maxAmount = null;
        if (getLong(param, "minAmount") != null) {
            minAmount = Long.parseLong(AmountUtil.convertDollar2Cent(getString(param, "minAmount")));

        }
        if (getLong(param, "maxAmount") != null) {
            maxAmount = Long.parseLong(AmountUtil.convertDollar2Cent(getString(param, "maxAmount")));
        }

        Byte productType = getByte(param, "productType");
        // 订单起止时间
        String createTimeStartStr = getString(param, "createTimeStart");
        String ymd = DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD);

        String createTimeEndStr = getString(param, "createTimeEnd");
        String paySuccessTimeStartStr = getString(param, "paySuccessTimeStart");
        String paySuccessTimeEndStr = getString(param, "paySuccessTimeEnd");

        if (StringUtils.isBlank(createTimeStartStr)) {
            createTimeStartStr = ymd + " 00:00:00";
        }

        Map allMap = rpcCommonService.rpcPayOrderService.count4All(null, mchId, productId, payOrderId, mchOrderNo, productType, createTimeStartStr, createTimeEndStr, paySuccessTimeStartStr, paySuccessTimeEndStr, passageId, minAmount, maxAmount);

        Map failMap = rpcCommonService.rpcPayOrderService.count4Fail(null, mchId, productId, payOrderId, mchOrderNo, productType, createTimeStartStr, createTimeEndStr, paySuccessTimeStartStr, paySuccessTimeEndStr, passageId, minAmount, maxAmount);

        if (StringUtils.isBlank(createTimeStartStr)) {
            paySuccessTimeStartStr = ymd + " 00:00:00";
        } else {
            paySuccessTimeStartStr = createTimeStartStr;
        }

        Map successMap = rpcCommonService.rpcPayOrderService.count4Success(null, mchId, productId, payOrderId, mchOrderNo, productType, createTimeStartStr, createTimeEndStr, paySuccessTimeStartStr, paySuccessTimeEndStr, passageId, minAmount, maxAmount);

        JSONObject obj = new JSONObject();
        obj.put("allTotalCount", allMap.get("totalCount"));                         // 所有订单数
        obj.put("allTotalAmount", allMap.get("totalAmount"));                       // 总金额
        obj.put("successTotalCount", successMap.get("totalCount"));                 // 成功订单数
        obj.put("successTotalAmount", successMap.get("totalAmount"));               // 成功金额
        obj.put("successTotalMchIncome", successMap.get("totalMchIncome"));         // 成功商户收入
        obj.put("successTotalAgentProfit", successMap.get("totalAgentProfit"));     // 成功代理商利润
        obj.put("successTotalPlatProfit", successMap.get("totalPlatProfit"));       // 成功平台利润
        obj.put("failTotalCount", failMap.get("totalCount"));                       // 未完成订单数
        obj.put("failTotalAmount", failMap.get("totalAmount"));                     // 未完成金额
        return ResponseEntity.ok(XxPayResponse.buildSuccess(obj));
    }

    /**
     * 补单
     * 1. 将订单为 支付中 状态的修改为支付成功
     * 2. 给商户下发一次通知
     *
     * @return
     */
    @MethodLog(remark = "补单")
    @RequestMapping("/reissue")
    @ResponseBody
    public ResponseEntity<?> reissue(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);

        // 判断输入的超级密码是否正确
        String password = getStringRequired(param, "password");
        if (!MchConstant.MGR_SUPER_PASSWORD.equals(password)) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_MGR_SUPER_PASSWORD_NOT_MATCH));
        }

        // 是否通知商户
        boolean isNotifyMch = false;
        // 修改订单状态
        String payOrderId = getStringRequired(param, "payOrderId");
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        if (payOrder.getStatus() == PayConstant.PAY_STATUS_PAYING) { // 初始或支付中
            // 修改状态为支付成功,
            int updateCount = rpcCommonService.rpcPayOrderService.updateStatus4Success(payOrderId);
            _log.info("[补单]userId={},payOrderId={},将支付中修改为支付成功,返回结果:{}", getUser().getId(), payOrder.getPayOrderId(), updateCount);
            if (updateCount == 1) isNotifyMch = true;
        }

        // 发送商户通知
        if (isNotifyMch) {
            rpcCommonService.rpcXxPayNotifyService.executePayNotify(payOrderId);
        }

        return ResponseEntity.ok(XxPayResponse.buildSuccess(payOrder));
    }


    /**
     * 查询上游订单信息
     *
     * @return
     */
    @RequestMapping("/searchupperOrder")
    @ResponseBody
    public ResponseEntity<?> searchUpperOrder(HttpServletRequest request) {
        try {
            SortedMap map = new TreeMap();
            String orderId = request.getParameter("orderId");
            String dateTemp = DateUtil.getSeqString();//时间错
            String mchOrderId = request.getParameter("mchOrderId");
            map.put("orderId", orderId);
            map.put("dateTemp", dateTemp);
            map.put("mchOrderId", mchOrderId);
            String sign = PayDigestUtil.getSign(map, "4F6B1A7BE25A3D8FB3E1149213EDF60D");
            map.put("sign", sign.toUpperCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String res = XXPayUtil.doPostQueryCmd(mainConfig.getPayUrl() + "/pay/queryUpperOrder", sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            //String resSign=resObj.getString("sign");这里暂时不验签，接口那边做了验签了
            resObj.remove("sign");
            return ResponseEntity.ok(XxPayResponse.buildSuccess(resObj));
        } catch (Exception ex) {
            return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_COMM_UNKNOWN_ERROR));
        }
    }


    /**
     * 订单分账
     *
     * @param request
     * @return
     */
    @RequestMapping("/orderfenzhang")
    @MethodLog(remark = "补发订单分账")
    @ResponseBody
    public ResponseEntity<?> orderfenzhang(HttpServletRequest request) {
        try {

            SortedMap map = new TreeMap();
            String orderId = request.getParameter("orderId");
            String dateTemp = DateUtil.getSeqString();//时间错
            map.put("orderId", orderId);
            map.put("dateTemp", dateTemp);
            String sign = PayDigestUtil.getSign(map, "4F6B1A7BE25A3D8FB3E1149213EDF60D");
            map.put("sign", sign.toUpperCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String res = XXPayUtil.doPostQueryCmd(mainConfig.getPayUrl() + "/pay/orderfenzhang", sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            resObj.remove("sign");
            return ResponseEntity.ok(XxPayResponse.buildSuccess(resObj));
        } catch (Exception ex) {
            return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_COMM_UNKNOWN_ERROR));
        }
    }


    /**
     * 补发商户通知
     *
     * @param request
     * @return
     */
    @MethodLog(remark = "补发商户通知")
    @RequestMapping("/sendNotify")
    @ResponseBody
    public ResponseEntity<?> sendNotify(HttpServletRequest request) {
        String payOrderId = request.getParameter("payOrderId");

        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
        JSONObject object = new JSONObject();
        if (payOrder.getStatus() != PayConstant.PAY_STATUS_SUCCESS) {
            object.put("msg", "订单状态不满足补发功能");
            return ResponseEntity.ok(XxPayResponse.buildSuccess(object));
        }

        rpcCommonService.rpcXxPayNotifyService.executePayNotify(payOrderId);
        object.put("msg", "补发商户通知成功!");
        return ResponseEntity.ok(XxPayResponse.buildSuccess(object));
    }

}


