package org.xxpay.agent.user.ctrl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.agent.common.ctrl.BaseController;
import org.xxpay.agent.common.service.RpcCommonService;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.entity.AgentAccount;
import org.xxpay.core.entity.AgentAccountHistory;
import org.xxpay.core.entity.MchAccount;
import org.xxpay.core.entity.MchAccountHistory;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 17/12/6
 * @description:
 */
@RestController
@RequestMapping(Constant.AGENT_CONTROLLER_ROOT_PATH + "/account")
@PreAuthorize("hasRole('"+ MchConstant.AGENT_ROLE_NORMAL+"')")
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
        AgentAccount agentAccount = rpcCommonService.rpcAgentAccountService.findByAgentId(getUser().getId());
        JSONObject object = (JSONObject) JSON.toJSON(agentAccount);
        object.put("availableBalance", agentAccount.getAvailableBalance());       // 可用余额
        object.put("availableSettAmount", agentAccount.getAvailableSettAmount()); // 可结算金额
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
        AgentAccountHistory agentAccountHistory = getObject(param, AgentAccountHistory.class);
        if(agentAccountHistory == null) agentAccountHistory = new AgentAccountHistory();
        agentAccountHistory.setAgentId(getUser().getId());
        int count = rpcCommonService.rpcAgentAccountHistoryService.count(agentAccountHistory);
        if(count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<AgentAccountHistory> agentAccountHistoryList = rpcCommonService.rpcAgentAccountHistoryService
                .select((getPageIndex(param) -1) * getPageSize(param), getPageSize(param), agentAccountHistory);
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(agentAccountHistoryList, count));
    }

    /**
     * 查询资金流水
     * @return
     */
    @RequestMapping("/history_get")
    @ResponseBody
    public ResponseEntity<?> historyGet(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Long id = getLongRequired(param, "id");
        AgentAccountHistory agentAccountHistory = rpcCommonService.rpcAgentAccountHistoryService
                .findByAgentIdAndId(getUser().getId(), id);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(agentAccountHistory));
    }

}
