package org.xxpay.pay.channel.huoshanpay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.pay.ctrl.common.BaseController;
import org.xxpay.pay.service.RpcCommonService;


@Controller
public class HuoshanpayController extends BaseController {

    private static final MyLog _log = MyLog.getLog(HuoshanpayController.class);

    @Autowired
    private RpcCommonService rpcCommonService;


}
