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
import org.xxpay.core.common.util.PDDUtils;
import org.xxpay.core.entity.PinduoduoOrders;
import org.xxpay.core.entity.PinduoduoStores;
import org.xxpay.core.entity.PinduoduoUser;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/pdd/stores")
public class PddStoresController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    /**
     * 列表
     *
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PinduoduoStores stores = new PinduoduoStores();
        if (!StringUtils.isBlank(param.getString("name"))) {
            String name = getString(param, "name");
            stores.setName(name);
        }

        int count = rpcCommonService.rpcIPinduoduoStoresService.count(stores);

        if (count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<PinduoduoStores> pinduoduoStoresList = rpcCommonService.rpcIPinduoduoStoresService.select((getPageIndex(param) - 1) * getPageSize(param),
                getPageSize(param), stores);
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(pinduoduoStoresList, count));
    }


    @RequestMapping("/update")
    @ResponseBody
    public ResponseEntity<?> update(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PinduoduoStores stores = new PinduoduoStores();
        if (!StringUtils.isBlank(param.getString("name"))) {
            String name = getString(param, "name");
            stores.setName(name);
        }
        if (!StringUtils.isBlank(param.getString("store_remain_total"))) {
            Long store_remain_total = getLong(param, "store_remain_total");
            stores.setStore_remain_total(store_remain_total);
        }
        if (!StringUtils.isBlank(param.getString("status"))) {
            boolean status = param.getBoolean("status");
            stores.setStatus(status);
        }
        stores.setId(param.getInteger("id"));
        int count = rpcCommonService.rpcIPinduoduoStoresService.update(stores);

        if (count == 1) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }
    @RequestMapping("/add")
    @ResponseBody
    @MethodLog(remark = "新增商铺")
    public ResponseEntity<?> add(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String name = getString(param, "name");
        PinduoduoStores stores=new PinduoduoStores();
        stores.setName(name);
        stores.setCtime(new Date());

        int count = rpcCommonService.rpcIPinduoduoStoresService.add(stores);
        if (count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

}
