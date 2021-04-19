package org.xxpay.manage.reconciliation.ctrl;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.BizResponse;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.entity.CheckMistake;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 18/1/21
 * @description:
 */
@Controller
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/bill/check_mistake")
public class CheckMistakeController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    /**
     * 查询单条
     * @return
     */
    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Long id = getLongRequired(param, "id");
        CheckMistake checkMistake = rpcCommonService.rpcCheckService.findByMistakeId(id);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(checkMistake));
    }

    /**
     * 记录列表
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer page = getInteger(param, "page");
        Integer limit = getInteger(param, "limit");
        CheckMistake checkMistake = getObject(param, CheckMistake.class);
        int count = rpcCommonService.rpcCheckService.countCheckMistake(checkMistake);
        if(count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<CheckMistake> checkMistakeList = rpcCommonService.rpcCheckService.selectCheckMistake(
                (getPageIndex(page) -1) * getPageSize(limit), getPageSize(limit), checkMistake);
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(checkMistakeList, count));
    }

    /**
     * 处理差错
     * @return
     */
    @RequestMapping("/handle")
    @ResponseBody
    public ResponseEntity<?> handle(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Long id = getLongRequired(param, "id");
        String handleType = getStringRequired(param, "handleType");
        String handleRemark = getString(param, "handleRemark");
        int count = rpcCommonService.rpcCheckService.handleCheckMistake(id, handleType, handleRemark);
        if(count != 1) ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
        return ResponseEntity.ok(BizResponse.buildSuccess());
    }

}
