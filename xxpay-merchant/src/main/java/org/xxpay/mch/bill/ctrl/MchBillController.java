package org.xxpay.mch.bill.ctrl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
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
import org.xxpay.core.entity.PayDataStatistics;
import org.xxpay.mch.common.ctrl.BaseController;
import org.xxpay.mch.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 18/02/07
 * @description:
 */
@RestController
@RequestMapping(Constant.MCH_CONTROLLER_ROOT_PATH + "/bill")
@PreAuthorize("hasRole('"+ MchConstant.MCH_ROLE_NORMAL+"')")
public class MchBillController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    @Value("${config.downMchBillUrl}")
    private String downBillUrl;

    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        MchBill mchBill = getObject(param, MchBill.class);
        if(mchBill == null) mchBill = new MchBill();
        mchBill.setMchId(getUser().getId());
        int count = rpcCommonService.rpcMchBillService.count(mchBill);
        if(count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<MchBill> mchBillList = rpcCommonService.rpcMchBillService.select((getPageIndex(param) - 1) * getPageSize(param), getPageSize(param), mchBill);
        for(MchBill bill : mchBillList) {
            // 完成下载,设置账单下载URL
            if(MchConstant.MCH_BILL_STATUS_COMPLETE == bill.getStatus()) {
                bill.setBillPath(downBillUrl + File.separator + DateUtil.date2Str(bill.getBillDate(), DateUtil.FORMAT_YYYY_MM_DD) + File.separator + bill.getMchId() + ".csv");
            }
        }
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(mchBillList, count));
    }

    /**
     * 商户充值数据
     *
     * @return
     */
    @RequestMapping("/data/merchanttopup")
    @ResponseBody
    public ResponseEntity<?> merchanttopup(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String createTimeStart = getString(param, "createTimeStart");
        String createTimeEnd = getString(param, "createTimeEnd");
        Integer page = getInteger(param, "page");
        Integer limit = getInteger(param, "limit");
        Long id = getUser().getId();
        Integer mchId = id.intValue();
        Long passageId = getLong(param,"productId");
        PayDataStatistics payDataStatistics = new PayDataStatistics();
        String ymd = DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD);
        if (StringUtils.isNotBlank(createTimeStart)) {
            payDataStatistics.setCreateTimeStart(createTimeStart);
        }else {
            payDataStatistics.setCreateTimeStart(ymd + " 00:00:00");
        }

        if (StringUtils.isNotBlank(createTimeEnd)) {
            payDataStatistics.setCreateTimeEnd(createTimeEnd);
        }
        if (mchId != null && mchId != -99) {
            payDataStatistics.setMchId(mchId);
        }
        if (passageId != null && passageId != -99) {
            payDataStatistics.setPassageId(passageId);
        }
        int count = rpcCommonService.rpcMchInfoService.merchantTopup(payDataStatistics);
        if (count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        // 支付数据统计
        List<PayDataStatistics> payProductList = rpcCommonService.rpcPayOrderService
                .merchanttopupdata((getPageIndex(page) - 1) * getPageSize(limit), getPageSize(limit), payDataStatistics);
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(payProductList, count));
    }

    /**
     * 查询应用信息
     * @return
     */
    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Long id = getLongRequired(param, "id");
        MchBill mchBill = rpcCommonService.rpcMchBillService.findByMchIdAndId(getUser().getId(), id);
        if(MchConstant.MCH_BILL_STATUS_COMPLETE == mchBill.getStatus()) {
            mchBill.setBillPath(downBillUrl + File.separator + DateUtil.date2Str(mchBill.getBillDate(), DateUtil.FORMAT_YYYY_MM_DD) + File.separator + mchBill.getMchId() + ".csv");
        }
        return ResponseEntity.ok(XxPayResponse.buildSuccess(mchBill));
    }

}
