package org.xxpay.mch.user.ctrl;

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
import org.xxpay.core.common.domain.BizResponse;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.MySeq;
import org.xxpay.core.common.util.ObjectValidUtil;
import org.xxpay.core.entity.MchApp;
import org.xxpay.mch.common.ctrl.BaseController;
import org.xxpay.mch.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 17/12/13
 * @description:
 */
@RestController
@RequestMapping(Constant.MCH_CONTROLLER_ROOT_PATH + "/app")
@PreAuthorize("hasRole('"+ MchConstant.MCH_ROLE_NORMAL+"')")
public class MchAppController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        MchApp mchApp = getObject(param, MchApp.class);
        if(mchApp == null) mchApp = new MchApp();
        mchApp.setMchId(getUser().getId());
        int count = rpcCommonService.rpcMchAppService.count(mchApp);
        if(count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<MchApp> mchAppList = rpcCommonService.rpcMchAppService.select((getPageIndex(param) - 1) * getPageSize(param), getPageSize(param), mchApp);
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(mchAppList, count));
    }

    /**
     * 查询应用信息
     * @return
     */
    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String appId = getStringRequired(param, "appId");
        MchApp mchApp = rpcCommonService.rpcMchAppService.findByMchIdAndAppId(getUser().getId(), appId);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(mchApp));
    }

    @RequestMapping("/add")
    @ResponseBody
    @MethodLog( remark = "新增应用" )
    public ResponseEntity<?> add(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String appName = getStringRequired(param, "appName");
        String remark = param.getString("remark");
        MchApp mchApp = new MchApp();
        mchApp.setMchId(getUser().getId());
        mchApp.setMchType(getUser().getType());
        mchApp.setAppId(MySeq.getUUID());
        mchApp.setAppName(appName);
        mchApp.setStatus(MchConstant.PUB_YES);
        mchApp.setRemark(remark);
        int count = rpcCommonService.rpcMchAppService.add(mchApp);
        if(count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    @RequestMapping("/update")
    @ResponseBody
    @MethodLog( remark = "修改应用" )
    public ResponseEntity<?> update(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String appId = getStringRequired(param, "appId");
        String appName = param.getString("appName");
        String remark = param.getString("remark");
        Byte status = getByte(param, "status");
        if(!ObjectValidUtil.isValid(appId)) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_PARAM_ERROR));
        }
        MchApp mchApp = new MchApp();
        if(StringUtils.isNotBlank(appName)) mchApp.setAppName(appName);
        if(status != null && status.byteValue() == MchConstant.PUB_YES) {
            mchApp.setStatus(MchConstant.PUB_YES);
        }else if(status != null && status.byteValue() == MchConstant.PUB_NO) {
            mchApp.setStatus(MchConstant.PUB_NO);
        }
        mchApp.setRemark(remark);
        int count = rpcCommonService.rpcMchAppService.updateByMchIdAndAppId(getUser().getId(), appId, mchApp);
        if(count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

}
