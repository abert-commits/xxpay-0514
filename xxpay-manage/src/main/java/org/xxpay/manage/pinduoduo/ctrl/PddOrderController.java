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
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.entity.*;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/pdd/order")
public class PddOrderController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    /**
     * 订单列表
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Date createTimeStart = param.getDate("createTimeStart");
        Date createTimeEnd = param.getDate("createTimeEnd");
        PinduoduoOrders orders=getObject(param,PinduoduoOrders.class);
        int count = rpcCommonService.rpcIPinduoduoOrderService.count(orders,createTimeStart,createTimeEnd);
        if(count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<PinduoduoOrders> refundOrderList = rpcCommonService.rpcIPinduoduoOrderService.select((getPageIndex(param) -1) * getPageSize(param),
                getPageSize(param), orders,createTimeStart,createTimeEnd);
        List<PinduoduoStores> PinduoduoStores = rpcCommonService.rpcIPinduoduoStoresService.select(0,50000,null);
        refundOrderList.stream().forEach(g ->{
            PinduoduoStores.stream().forEach(s ->{
                if(g.getStoresId()==s.getId()){
                    g.setStoresName(s.getName());
                }
            });
        });

        return ResponseEntity.ok(XxPayPageRes.buildSuccess(refundOrderList, count));
    }


}
