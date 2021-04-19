package org.xxpay.manage.sys.ctrl;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.domain.BizResponse;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.TelegramUtil;
import org.xxpay.core.entity.SysUser;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;
import org.xxpay.manage.sys.service.SysUserService;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/sys/announcement")
public class AnnouncementController extends BaseController {

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RpcCommonService rpcCommonService;

    /**
     * 查询用户信息
     *
     * @return
     */
    @RequestMapping("/pushMsg")
    @ResponseBody
    public ResponseEntity<?> pushMsg(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String type = getStringRequired(param, "type");
        String msg = getStringRequired(param, "msg");
        String result = TelegramUtil.PushMsg(type, msg);
        return ResponseEntity.ok(BizResponse.build(result));
    }
}
