package org.xxpay.manage.order.ctrl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.annotation.MethodLog;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.PayDigestUtil;
import org.xxpay.core.common.util.XXPayUtil;
import org.xxpay.core.entity.RefundOrder;
import org.xxpay.manage.common.service.RpcCommonService;
import org.xxpay.manage.common.ctrl.BaseController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author: dingzhiwei
 * @date: 17/12/6
 * @description:
 */
@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/refund_order")
public class RefundOrderController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    /**
     * 查询单条退款记录
     *
     * @return
     */
    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String refundOrderId = getStringRequired(param, "refundOrderId");
        RefundOrder refundOrder = rpcCommonService.rpcRefundOrderService.findByRefundOrderId(refundOrderId);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(refundOrder));
    }

    /**
     * 退款订单记录列表
     *
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        RefundOrder refundOrder = getObject(param, RefundOrder.class);
        // 订单起止时间
        Date createTimeStart = null;
        Date createTimeEnd = null;
        String createTimeStartStr = getString(param, "createTimeStart");
        if (StringUtils.isNotBlank(createTimeStartStr)) createTimeStart = DateUtil.str2date(createTimeStartStr);
        String createTimeEndStr = getString(param, "createTimeEnd");
        if (StringUtils.isNotBlank(createTimeEndStr)) createTimeEnd = DateUtil.str2date(createTimeEndStr);

        int count = rpcCommonService.rpcRefundOrderService.count(refundOrder, createTimeStart, createTimeEnd);
        if (count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<RefundOrder> refundOrderList = rpcCommonService.rpcRefundOrderService.select((getPageIndex(param) - 1) * getPageSize(param),
                getPageSize(param), refundOrder, createTimeStart, createTimeEnd);
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(refundOrderList, count));
    }

    /**
     * 统一退款接口:
     * 1)先验证接口参数以及签名信息
     * 2)验证通过创建支付订单
     * 3)根据商户选择渠道,调用支付服务进行下单
     * 4)返回下单数据
     *
     * @param request
     * @return
     */
    @MethodLog(remark = "订单退款")
    @RequestMapping(value = "/alipayrefundorder")
    @ResponseBody
    public ResponseEntity<?> aliPayRefundOrder(HttpServletRequest request) {
        try {
            SortedMap map = new TreeMap();
            String mchId = request.getParameter("mchId");
            String dateTemp = DateUtil.getSeqString();//时间错
            String payOrderId = request.getParameter("payOrderId");
            map.put("mchId", mchId);
            map.put("dateTemp", dateTemp);
            map.put("payOrderId", payOrderId);
            String sign = PayDigestUtil.getSign(map, "4F6B1A7BE25A3D8FB3E1149213EDF60D");
            map.put("sign", sign.toUpperCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String res = XXPayUtil.doPostQueryCmd(mainConfig.getPayUrl() + "/refund/alipayrefundorder", sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);
            return ResponseEntity.ok(XxPayResponse.buildSuccess(resObj));
        } catch (Exception ex) {
            return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_COMM_UNKNOWN_ERROR));
        }
    }
}
