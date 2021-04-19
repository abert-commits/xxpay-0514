package org.xxpay.mch.user.ctrl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.JsonUtil;
import org.xxpay.core.entity.*;
import org.xxpay.mch.common.ctrl.BaseController;
import org.xxpay.mch.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 18/4/8
 * @description: 用户账户接口
 */
@RestController
@RequestMapping(Constant.MCH_CONTROLLER_ROOT_PATH + "/uaccount")
@PreAuthorize("hasRole('"+ MchConstant.MCH_ROLE_NORMAL+"')")
public class UserAccountController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    /**
     * 查询用户账户列表
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> historyGet(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Long mchId = getUser().getId();
        String userId = getString(param, "userId");
        Short state = param.getShort("state");
        UserAccount userAccount = new UserAccount();
        userAccount.setMchId(mchId);
        if(StringUtils.isNotBlank(userId)) userAccount.setUserId(userId);
        if(state != null) userAccount.setState(state);
        int count = rpcCommonService.rpcUserAccountService.countUserAccount(userAccount);
        if(count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<UserAccount> userAccountList = rpcCommonService.rpcUserAccountService
                .selectUserAccount((getPageIndex(param) -1) * getPageSize(param), getPageSize(param), userAccount);
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(userAccountList, count));
    }

    /**
     * 查询用户账户信息
     * @return
     */
    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String userId = getStringRequired(param, "userId");
        UserAccount userAccount = rpcCommonService.rpcUserAccountService.getUserAccount(getUser().getId(), userId);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(userAccount));
    }

    /**
     * 查询账户资金变更明细列表
     * @return
     */
    @RequestMapping("/detail_list")
    @ResponseBody
    public ResponseEntity<?> detailList(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String userId = getStringRequired(param, "userId");
        Integer changeDay = param.getInteger("changeDay");
        Short changeType = param.getShort("changeType");
        Short accountType = param.getShort("accountType");
        int count = rpcCommonService.rpcUserAccountService.getUserAccountDetailTotalCount(getUser().getId(), userId, changeDay, changeType, accountType);
        if(count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<UserAccountChangeDetail> userAccountChangeDetailList = rpcCommonService.rpcUserAccountService
                .getUserAccountDetailList(getUser().getId(), userId, changeDay, changeType, accountType, (getPageIndex(param) -1) * getPageSize(param), getPageSize(param));
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(userAccountChangeDetailList, count));
    }

}
