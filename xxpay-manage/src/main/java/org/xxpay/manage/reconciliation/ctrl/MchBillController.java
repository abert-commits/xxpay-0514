package org.xxpay.manage.reconciliation.ctrl;

import com.alibaba.fastjson.JSONObject;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.entity.MchBill;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;

import java.io.File;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 18/02/07
 * @description:
 */
@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/bill/mch")
public class MchBillController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        MchBill mchBill = getObject(param, MchBill.class);
        if(mchBill == null) mchBill = new MchBill();
        int count = rpcCommonService.rpcMchBillService.count(mchBill);
        if(count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<MchBill> mchBillList = rpcCommonService.rpcMchBillService.select((getPageIndex(param) - 1) * getPageSize(param), getPageSize(param), mchBill);
        for(MchBill bill : mchBillList) {
            // 完成下载,设置账单下载URL
            if(MchConstant.MCH_BILL_STATUS_COMPLETE == bill.getStatus()) {
                bill.setBillPath(mainConfig.getDownMchBillUrl() + File.separator + DateUtil.date2Str(bill.getBillDate(), DateUtil.FORMAT_YYYY_MM_DD) + File.separator + bill.getMchId() + ".csv");
            }
        }
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(mchBillList, count));
    }

    /**
     *
     * @return
     */
    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Long id = getLongRequired(param, "id");
        MchBill mchBill = rpcCommonService.rpcMchBillService.findById(id);
        if(MchConstant.MCH_BILL_STATUS_COMPLETE == mchBill.getStatus()) {
            mchBill.setBillPath(mainConfig.getDownMchBillUrl() + File.separator + DateUtil.date2Str(mchBill.getBillDate(), DateUtil.FORMAT_YYYY_MM_DD) + File.separator + mchBill.getMchId() + ".csv");
        }
        return ResponseEntity.ok(XxPayResponse.buildSuccess(mchBill));
    }

}