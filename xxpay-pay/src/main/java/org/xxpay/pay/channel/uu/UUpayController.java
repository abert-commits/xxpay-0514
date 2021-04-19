package org.xxpay.pay.channel.uu;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.pay.ctrl.common.BaseController;
import org.xxpay.pay.service.RpcCommonService;


@Controller
public class UUpayController extends BaseController {

    private static final MyLog _log = MyLog.getLog(UUpayController.class);

    @Autowired
    private RpcCommonService rpcCommonService;


}
