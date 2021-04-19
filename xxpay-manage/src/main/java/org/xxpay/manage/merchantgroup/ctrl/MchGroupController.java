package org.xxpay.manage.merchantgroup.ctrl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxpay.core.common.annotation.MethodLog;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.BizResponse;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.MD5Util;
import org.xxpay.core.entity.AgentInfo;
import org.xxpay.core.entity.MchGroup;
import org.xxpay.core.entity.MchInfo;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/mch_group")
public class MchGroupController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;


    /**
     * 根据商户组ID查询商户组信息
     *
     * @return
     */
    @RequestMapping("/getMchGroupById")
    @ResponseBody
    public ResponseEntity<?> getMchGroupById(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer mchGroupId = getInteger(param, "mchGroupId");
        MchGroup mchInfo = rpcCommonService.rpcMchGroupService.findByMchGroupId(mchGroupId);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(mchInfo));
    }


    /**
     * 根据商户组名称查询商户组信息
     *
     * @return
     */
    @RequestMapping("/getMchGroupByName")
    @ResponseBody
    public ResponseEntity<?> getMchGroupByName(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String mchGroupName = getStringRequired(param, "mchGroupName");
        MchGroup mchInfo = rpcCommonService.rpcMchGroupService.findByGroupName(mchGroupName);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(mchInfo));
    }


    /**
     * 查询所有商户组信息
     *
     * @return
     */
    @RequestMapping("/queryMchGroups")
    @ResponseBody
    public ResponseEntity<?> queryMchGroups() {
        List<MchGroup> mchInfo = rpcCommonService.rpcMchGroupService.selectAll();
        return ResponseEntity.ok(XxPayResponse.buildSuccess(mchInfo));
    }


    /**
     * 新增商户信息
     *
     * @return
     */
    @RequestMapping("/addMchGroup")
    @ResponseBody
    @MethodLog(remark = "新增商户组")
    public ResponseEntity<?> addMchGroup(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        MchGroup mchGroupInfo = getObject(param, MchGroup.class);
        // 确认商户组不能重复
        if (rpcCommonService.rpcMchGroupService.findByGroupName(mchGroupInfo.getGroupName()) != null) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_MCH_GROUPNAME_USED));
        }

        //判断是否设置了商户组费率
        if (mchGroupInfo.getRate() == null || mchGroupInfo.getRate().compareTo(new BigDecimal(0)) == 0) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_MCH_GROUPRATE_USED));

        }

        // 判断代理商是否正确
        int count = rpcCommonService.rpcMchGroupService.add(mchGroupInfo);
        if (count != 1) ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
        return ResponseEntity.ok(BizResponse.buildSuccess());
    }


    /**
     * 修改商户组
     *
     * @return
     */
    @RequestMapping("/updateMchGroup")
    @ResponseBody
    @MethodLog(remark = "修改商户组")
    public ResponseEntity<?> updateMchGroup(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        MchGroup mchGroupInfo = getObject(param, MchGroup.class);
        // 确认商户组不能重复
        MchGroup mchGroupOld = rpcCommonService.rpcMchGroupService.findByGroupName(mchGroupInfo.getGroupName());
        if (mchGroupOld != null && mchGroupInfo.getGroupId().intValue() != mchGroupOld.getGroupId().intValue()) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_MCH_GROUPNAME_USED));
        }

        //判断是否设置了商户组费率
        if (mchGroupInfo.getRate() == null || mchGroupInfo.getRate().compareTo(new BigDecimal(0)) == 0) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_MCH_GROUPRATE_USED));
        }

        // 判断代理商是否正确
        int count = rpcCommonService.rpcMchGroupService.update(mchGroupInfo);
        if (count != 1) ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
        return ResponseEntity.ok(BizResponse.buildSuccess());
    }
}




