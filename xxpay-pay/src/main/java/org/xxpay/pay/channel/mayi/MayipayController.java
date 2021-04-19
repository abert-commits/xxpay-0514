package org.xxpay.pay.channel.mayi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.pay.ctrl.common.BaseController;
import org.xxpay.pay.service.RpcCommonService;


@Controller
public class MayipayController extends BaseController {

    private static final MyLog _log = MyLog.getLog(MayipayController.class);

    @Autowired
    private RpcCommonService rpcCommonService;


}
