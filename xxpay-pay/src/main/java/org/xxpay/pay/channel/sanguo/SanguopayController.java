package org.xxpay.pay.channel.sanguo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.pay.ctrl.common.BaseController;
import org.xxpay.pay.service.RpcCommonService;


@Controller
public class SanguopayController extends BaseController {

    private static final MyLog _log = MyLog.getLog(SanguopayController.class);

    @Autowired
    private RpcCommonService rpcCommonService;


}
