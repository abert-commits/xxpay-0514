package org.xxpay.manage.sys.ctrl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxpay.core.common.annotation.MethodLog;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.domain.BizResponse;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/sys/config")
public class SysConfigController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    /**
     * 查询配置信息
     * @return
     */
    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String type = getStringRequired(param, "type");
        JSONObject obj = rpcCommonService.rpcSysConfigService.getSysConfigObj(type);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(obj));
    }

    /**
     * 修改配置信息
     * @return
     */
    @RequestMapping("/update")
    @ResponseBody
    @MethodLog( remark = "修改配置信息" )
    public ResponseEntity<?> update(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String type = request.getParameter("type");
        if("sett".equals(type)) {
            // 将金额元转成分
            handleParamAmount(param, "drawMaxDayAmount", "maxDrawAmount", "minDrawAmount", "feeLevel", "drawFeeLimit");

        }
        rpcCommonService.rpcSysConfigService.update(param);
        return ResponseEntity.ok(BizResponse.buildSuccess());
    }

}