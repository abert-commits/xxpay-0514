package org.xxpay.mch.order.ctrl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayPassage;
import org.xxpay.mch.common.ctrl.BaseController;
import org.xxpay.mch.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(Constant.MCH_CONTROLLER_ROOT_PATH + "/pay_order")
@PreAuthorize("hasRole('"+ MchConstant.MCH_ROLE_NORMAL+"')")
public class PayOrderController extends BaseController {


    @Autowired
    private RpcCommonService rpcCommonService;

    /**
     * 查询单条支付记录
     * @return
     */
    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String payOrderId = getStringRequired(param, "payOrderId");
        PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByMchIdAndPayOrderId(getUser().getId(), payOrderId);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payOrder));
    }

    /**
     * 支付订单记录列表
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer page = getInteger(param, "page");
        Integer limit = getInteger(param, "limit");
        PayOrder payOrder = getObject(param, PayOrder.class);
        // 订单起止时间
        Date createTimeStart = null;
        Date createTimeEnd = null;
        String createTimeStartStr = getString(param, "createTimeStart");
        if(StringUtils.isNotBlank(createTimeStartStr)) createTimeStart = DateUtil.str2date(createTimeStartStr);
        String createTimeEndStr = getString(param, "createTimeEnd");
        if(StringUtils.isNotBlank(createTimeEndStr)) createTimeEnd = DateUtil.str2date(createTimeEndStr);

        int count = rpcCommonService.rpcPayOrderService.count(getUser().getId(), payOrder, createTimeStart, createTimeEnd,0,null,null);
        if(count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<PayOrder> payOrderList = rpcCommonService.rpcPayOrderService.select(getUser().getId(),
                (getPageIndex(page) -1) * getPageSize(limit), getPageSize(limit), payOrder, createTimeStart, createTimeEnd,0,null,null);
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(payOrderList, count));
    }

    /**
     * 查询订单统计数据
     * @return
     */
    @RequestMapping("/count")
    @ResponseBody
    public ResponseEntity<?> count(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String payOrderId = getString(param, "payOrderId");
        String mchOrderNo = getString(param, "mchOrderNo");
        Long productId = getLong(param, "productId");
        Byte productType = getByte(param, "productType");
        Long passageId = getLong(param, "passageId");
        Long minAmount=null;
        Long maxAmount=null;
        if (getLong(param,"minAmount") != null){
            minAmount = Long.parseLong(AmountUtil.convertDollar2Cent(getString(param,"minAmount")));

        }
        if (getLong(param,"minAmount") != null){
            maxAmount=Long.parseLong(AmountUtil.convertDollar2Cent(getString(param,"maxAmount")));
        }
        Long mchId = getUser().getId();

        // 订单起止时间
        String createTimeStartStr = getString(param, "createTimeStart");
        String createTimeEndStr = getString(param, "createTimeEnd");
        String paySuccessTimeStartStr = getString(param, "paySuccessTimeStart");
        String paySuccessTimeEndStr = getString(param, "paySuccessTimeEnd");


        Map allMap = rpcCommonService.rpcPayOrderService.count4All(null, mchId, productId, payOrderId, mchOrderNo, productType,createTimeStartStr, createTimeEndStr,paySuccessTimeStartStr, paySuccessTimeEndStr,passageId,minAmount,maxAmount);
        Map successMap = rpcCommonService.rpcPayOrderService.mchCount4Success(null, mchId, productId, payOrderId, mchOrderNo, productType, createTimeStartStr, createTimeEndStr,paySuccessTimeStartStr, paySuccessTimeEndStr,passageId,minAmount,maxAmount);
        Map failMap = rpcCommonService.rpcPayOrderService.count4Fail(null, mchId, productId, payOrderId, mchOrderNo, productType, createTimeStartStr ,createTimeEndStr,paySuccessTimeStartStr, paySuccessTimeEndStr,passageId,minAmount,maxAmount);

        JSONObject obj = new JSONObject();
        obj.put("allTotalCount", allMap.get("totalCount"));                         // 所有订单数
        obj.put("allTotalAmount", allMap.get("totalAmount"));                       // 总金额
        obj.put("successTotalCount", successMap.get("totalCount"));                 // 成功订单数
        obj.put("successTotalAmount", successMap.get("totalAmount"));               // 成功金额
        obj.put("successTotalMchIncome", successMap.get("totalMchIncome"));         // 成功商户收入
        obj.put("failTotalCount", failMap.get("totalCount"));                       // 未完成订单数
        obj.put("failTotalAmount", failMap.get("totalAmount"));                     // 未完成金额
        System.out.println("打印：："+obj.toString());
        return ResponseEntity.ok(XxPayResponse.buildSuccess(obj));
    }


    /**
     * 导出支付订单数据 todo
     *
     * @return
     */
    @RequestMapping("/history_export")
    @ResponseBody
    public ResponseEntity<?> historyExpory(HttpServletRequest request) {
        System.out.println("进入了导出函数");
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
        String createTimeStartStr = getString(param, "createTimeStart");
        //if(StringUtils.isNotBlank(createTimeStartStr)) createTimeStart = DateUtil.str2date(createTimeStartStr);
        //String currentTime=new SimpleDateFormat("YYYY-MM-dd").format(new Date());
        if(StringUtils.isEmpty(createTimeStartStr)){
            String time= new SimpleDateFormat("YYYY-MM-dd 00:00:00").format(new Date());
            createTimeStart = DateUtil.str2date(time);
        }
            String createTimeEndStr = getString(param, "createTimeEnd");
       // if(StringUtils.isNotBlank(createTimeEndStr)) createTimeEnd = DateUtil.str2date(createTimeEndStr);
        if(StringUtils.isEmpty(createTimeEndStr)){
             String time= new SimpleDateFormat("YYYY-MM-dd 23:59:59").format(new Date());
            createTimeEnd = DateUtil.str2date(time);
        }

        int count = rpcCommonService.rpcPayOrderService.count(getUser().getId(), payOrder, createTimeStart, createTimeEnd,0,null,null);
        if (count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        //List<PayOrder> payOrderList = rpcCommonService.rpcPayOrderService.select(getUser().getId(), (getPageIndex(page) -1) * getPageSize(limit), getPageSize(limit), payOrder, createTimeStart, createTimeEnd,0,null,null);
        List<PayOrder> payOrderList = rpcCommonService.rpcPayOrderService.select(getUser().getId(), 0, count, payOrder, createTimeStart, createTimeEnd,0,null,null);
        for (PayOrder item : payOrderList) {
            PayPassage passage = rpcCommonService.rpcPayPassageService.findById(item.getPassageId());
            item.setPassageName(passage == null ? "" : passage.getPassageName());
            item.setParam1(DateUtil.date2Str(item.getCreateTime()));
            item.setParam2(DateUtil.date2Str(item.getPaySuccTime()));
            item.setAmount(item.getAmount() / 100);
            item.setMchIncome(item.getMchIncome() / 100);
            Byte status = item.getStatus();
            String status1 = xff(status);
            item.setErrMsg(status1);
        }

        return ResponseEntity.ok(XxPayPageRes.buildSuccess(payOrderList, count));
    }

    private String xff(Byte status) {
        String status1="";
        if(status == 0) {
            return "订单生成";
        }else if(status == 1) {
            return "支付中";
        }else if(status == 2) {
            return "支付成功";
        }else if(status == -1) {
            return "支付失败";
        }else if(status == 3) {
            return "处理完成";
        }else if(status == 4) {
            return "已退款";
        }
        return status1;
    }


}