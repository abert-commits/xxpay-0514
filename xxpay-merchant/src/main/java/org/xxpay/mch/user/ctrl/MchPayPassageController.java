package org.xxpay.mch.user.ctrl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.entity.MchPayPassage;
import org.xxpay.core.entity.PayProduct;
import org.xxpay.mch.common.ctrl.BaseController;
import org.xxpay.mch.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 18/02/11
 * @description:
 */
@RestController
@RequestMapping(Constant.MCH_CONTROLLER_ROOT_PATH + "/mch_pay_passage")
@PreAuthorize("hasRole('"+ MchConstant.MCH_ROLE_NORMAL+"')")
public class MchPayPassageController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        Long mchId = getUser().getId();

        // 支付产品很多时,要考虑内存溢出问题
        List<PayProduct> payProductList = rpcCommonService.rpcPayProductService.selectAll();
        Map<String, PayProduct> payProductMap = new HashMap<>();
        for(PayProduct product : payProductList) {
            payProductMap.put(String.valueOf(product.getId()), product);
        }

        // 得到商户配置的支付通道
        List<MchPayPassage> mchPayPassageList = rpcCommonService.rpcMchPayPassageService.selectAllByMchId(mchId);
        Map<String, MchPayPassage> mchPayPassageMap = new HashMap<>();
        for(MchPayPassage mchPayPassage : mchPayPassageList) {
            mchPayPassageMap.put(String.valueOf(mchPayPassage.getProductId()), mchPayPassage);
        }

        List<JSONObject> objects = new LinkedList<>();
        for(MchPayPassage mchPayPassage : mchPayPassageList) {
            JSONObject object = (JSONObject) JSON.toJSON(mchPayPassage);
            if(payProductMap.get(String.valueOf(mchPayPassage.getProductId())) != null) {
                object.put("productName", payProductMap.get(String.valueOf(mchPayPassage.getProductId())).getProductName());
            }
            objects.add(object);
        }
        return ResponseEntity.ok(XxPayResponse.buildSuccess(objects));
    }

}
