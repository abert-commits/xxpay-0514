package org.xxpay.manage.config.ctrl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.annotation.MethodLog;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.constant.PayEnum;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.BizResponse;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayCashCollConfig;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;
import org.xxpay.manage.utils.SpringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/config/pay_cash_coll")
public class PayCashCollConfigController extends BaseController {

    private final MyLog _log = MyLog.getLog(PayCashCollConfigController.class);

    @Autowired
    private RpcCommonService rpcCommonService;

    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) throws ParseException {
        JSONObject param = getJsonParam(request);
        PayCashCollConfig payProduct = getObject(param, PayCashCollConfig.class);
        int count = rpcCommonService.rpcPayCashCollConfigService.count(payProduct);
        if (count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<PayCashCollConfig> payProductList = rpcCommonService.rpcPayCashCollConfigService.select((getPageIndex(param) - 1) * getPageSize(param), getPageSize(param), payProduct);


        String ymd = DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD);
        Date beginDate = DateUtil.str2date(ymd + " 00:00:00");
        Date endDate = DateUtil.str2date(ymd + " 23:59:59");
        Map<String, Object> map = new HashMap<>();
        map.put("payOrderId", null);
        map.put("channelOrderNo", null);
        map.put("createTimeStart", beginDate);
        map.put("createTimeEnd", endDate);

        if (payProductList != null && payProductList.size() > 0) {
            for (PayCashCollConfig collConfig : payProductList) {
                map.put("transInUserId", collConfig.getTransInUserId());
                map.put("status", "1");
                map.put("passageAccountId", null);
                Map transSuccessMap = rpcCommonService.rpcPayOrderCashCollRecordService.transSuccess(map);

                if (transSuccessMap != null) {
                    String amount = AmountUtil.convertCent2Dollar(transSuccessMap.get("transSuccessAmount").toString());
                    collConfig.setTransInUserId(collConfig.getTransInUserId() + "=>" + amount);
                } else {
                    collConfig.setTransInUserId(collConfig.getTransInUserId() + "=>0");
                }
            }
        }
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(payProductList, count));
    }

    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer id = getIntegerRequired(param, "id");
        PayCashCollConfig payProduct = rpcCommonService.rpcPayCashCollConfigService.findById(id);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payProduct));
    }

    @RequestMapping("/update")
    @ResponseBody
    @MethodLog(remark = "修改资金归集账号")
    public ResponseEntity<?> update(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayCashCollConfig payProduct = getObject(param, PayCashCollConfig.class);
        if (payProduct.getType() == null) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_SERVICE_LedgerType_NOT_SELECT));
        }

        JSONObject bindObj = null;
        if (payProduct.getType() == 0) {
            bindObj = relationBind(param);
        }

        if (bindObj != null && !bindObj.getString("retCode").equals(PayConstant.RETURN_VALUE_SUCCESS)) {
            BizResponse bizResponse = new BizResponse(1, bindObj.getString("errDes"));
            return ResponseEntity.ok(bizResponse);
        }

        int count = rpcCommonService.rpcPayCashCollConfigService.update(payProduct);
        if (count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    @RequestMapping("/add")
    @ResponseBody
    @MethodLog(remark = "新增资金归集账号")
    public ResponseEntity<?> add(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayCashCollConfig payProduct = getObject(param, PayCashCollConfig.class);

        if (payProduct.getType() == null) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_SERVICE_LedgerType_NOT_SELECT));
        }

        JSONObject bindObj = null;
        if (payProduct.getType() == 0) {
            bindObj = relationBind(param);
        }

        if (bindObj != null && !bindObj.getString("retCode").equals(PayConstant.RETURN_VALUE_SUCCESS)) {
            BizResponse bizResponse = new BizResponse(1, bindObj.getString("errDes"));
            return ResponseEntity.ok(bizResponse);
        }

        int count = rpcCommonService.rpcPayCashCollConfigService.add(payProduct);
        if (count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }


    /**
     * 查询上游订单信息
     *
     * @return
     */
    private JSONObject relationBind(JSONObject payProduct) {
        JSONObject resObj = null;
        try {

            String sendMsg = XXPayUtil.mapToStringByObj(payProduct).replace(">", "");
            String res = XXPayUtil.doPostQueryCmd(mainConfig.getPayUrl() + "/pay/relationBind", sendMsg);
            resObj = JSONObject.parseObject(res);
            return resObj;
        } catch (Exception ex) {
            resObj.put("errDes", "绑定商家分账关系发生异常");
            resObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return resObj;
        }
    }


}
