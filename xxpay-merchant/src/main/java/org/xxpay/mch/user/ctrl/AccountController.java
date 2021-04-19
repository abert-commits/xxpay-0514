package org.xxpay.mch.user.ctrl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.entity.MchAccount;
import org.xxpay.core.entity.MchAccountHistory;
import org.xxpay.mch.common.ctrl.BaseController;
import org.xxpay.mch.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 17/12/6
 * @description:
 */
@RestController
@RequestMapping(Constant.MCH_CONTROLLER_ROOT_PATH + "/account")
@PreAuthorize("hasRole('"+ MchConstant.MCH_ROLE_NORMAL+"')")
public class AccountController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    /**
     * 查询账户信息
     * @return
     */
    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get() {
        MchAccount mchAccount = rpcCommonService.rpcMchAccountService.findByMchId(getUser().getId());
        JSONObject object = (JSONObject) JSON.toJSON(mchAccount);
        object.put("availableBalance", mchAccount.getAvailableBalance());                       // 可用余额
        object.put("availableSettAmount", mchAccount.getAvailableSettAmount());                 // 可结算金额
        object.put("availableAgentpayBalance", mchAccount.getAvailableAgentpayBalance());       // 可用代付余额
        return ResponseEntity.ok(XxPayResponse.buildSuccess(object));
    }

    /**
     * 查询资金流水列表
     * @return
     */
    @RequestMapping("/history_list")
    @ResponseBody
    public ResponseEntity<?> historyList(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        MchAccountHistory mchAccountHistory = getObject(param, MchAccountHistory.class);
        int count = rpcCommonService.rpcMchAccountHistoryService.count(getUser().getId(), mchAccountHistory, getQueryObj(param));
        if(count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<MchAccountHistory> mchAccountHistoryList = rpcCommonService.rpcMchAccountHistoryService
                .select(getUser().getId(), (getPageIndex(param) - 1) * getPageSize(param), getPageSize(param), mchAccountHistory, getQueryObj(param));
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(mchAccountHistoryList, count));
    }

    /**
     * 查询资金流水列表
     * @return
     */
    @RequestMapping("/history_get")
    @ResponseBody
    public ResponseEntity<?> historyGet(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Long id = getLongRequired(param, "id");
        MchAccountHistory mchAccountHistory = rpcCommonService.rpcMchAccountHistoryService
                .findById(getUser().getId(), id);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(mchAccountHistory));
    }

}
