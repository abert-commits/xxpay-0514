package org.xxpay.manage.pinduoduo.ctrl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.annotation.MethodLog;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.BizResponse;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.entity.PinduoduoPreOrders;
import org.xxpay.core.entity.PinduoduoStores;
import org.xxpay.core.entity.PinduoduoUser;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/pdd/preOrders")
public class PddPreOrdersController extends BaseController {
    @Autowired
    private RpcCommonService rpcCommonService;
    /**
     * 列表
     *
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    @MethodLog
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PinduoduoPreOrders preOrders = getObject(param,PinduoduoPreOrders.class);

        int count = rpcCommonService.rpcIPinduoduoPreOrderService.count(preOrders);

        if (count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<PinduoduoPreOrders> preOrdersList = rpcCommonService.rpcIPinduoduoPreOrderService.select((getPageIndex(param) - 1) * getPageSize(param),
                getPageSize(param), preOrders);
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(preOrdersList, count));
    }

    @RequestMapping("/add")
    @ResponseBody
    @MethodLog(remark = "新增预订单")
    public ResponseEntity<?> add(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PinduoduoPreOrders preOrders=getObject(param,PinduoduoPreOrders.class);
        preOrders.setCtime(new Date());
        preOrders.setStatus(0);
        int count = rpcCommonService.rpcIPinduoduoPreOrderService.add(preOrders);
        if (count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    @RequestMapping("/update")
    @ResponseBody
    public ResponseEntity<?> update(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PinduoduoPreOrders orders =getObject(param,PinduoduoPreOrders.class);

        int count = rpcCommonService.rpcIPinduoduoPreOrderService.update(orders);

        if (count == 1) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }
}
