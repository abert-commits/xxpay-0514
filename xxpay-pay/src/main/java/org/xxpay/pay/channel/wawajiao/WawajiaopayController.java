package org.xxpay.pay.channel.wawajiao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.pay.ctrl.common.BaseController;
import org.xxpay.pay.service.RpcCommonService;


@Controller
public class WawajiaopayController extends BaseController {

    private static final MyLog _log = MyLog.getLog(WawajiaopayController.class);

    @Autowired
    private RpcCommonService rpcCommonService;


}
