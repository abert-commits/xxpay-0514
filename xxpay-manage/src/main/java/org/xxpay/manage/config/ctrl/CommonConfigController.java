package org.xxpay.manage.config.ctrl;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.entity.*;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author: dingzhiwei
 * @date: 18/1/17
 * @description: 通用配置
 */
@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/config/common")
public class CommonConfigController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    /**
     * 所有支付类型列表
     * @param request
     * @return
     */
    @RequestMapping("/pay_type_all")
    @ResponseBody
    public ResponseEntity<?> payTypeaAll(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayType payType = getObject(param, PayType.class);
        List<PayType> payTypeList = rpcCommonService.rpcPayTypeService.selectAll(payType);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payTypeList));
    }

    /**
     * 所有支付产品列表
     * @param request
     * @return
     */
    @RequestMapping("/pay_product_all")
    @ResponseBody
    public ResponseEntity<?> payProductAll(HttpServletRequest request) {
        PayProduct payProduct = new PayProduct();
        payProduct.setStatus(MchConstant.PUB_YES);
        List<PayProduct> payProductList = rpcCommonService.rpcPayProductService.selectAll(payProduct);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payProductList));
    }

    /**
     * 所有支付接口类型列表
     * @param request
     * @return
     */
    @RequestMapping("/pay_interface_type_all")
    @ResponseBody
    public ResponseEntity<?> payInterfaceTypeAll(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayInterfaceType payInterfaceType = getObject(param, PayInterfaceType.class);
        List<PayInterfaceType> payInterfaceTypeList = rpcCommonService.rpcPayInterfaceTypeService.selectAll(payInterfaceType);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payInterfaceTypeList));
    }

    /**
     * 所有支付接口列表
     * @param request
     * @return
     */
    @RequestMapping("/pay_interface_all")
    @ResponseBody
    public ResponseEntity<?> payInterfaceAll(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayInterface payInterface = getObject(param, PayInterface.class);
        List<PayInterface> payInterfaceList = rpcCommonService.rpcPayInterfaceService.selectAll(payInterface);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payInterfaceList));
    }

    /**
     * 所有支付通道列表
     * @param request
     * @return
     */
    @RequestMapping("/pay_passage_all")
    @ResponseBody
    public ResponseEntity<?> payPassageAll(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayPassage payPassage = getObject(param, PayPassage.class);
        List<PayPassage> payPassageList = rpcCommonService.rpcPayPassageService.selectAll(payPassage);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payPassageList));
    }


    /**
     * 所有商户名称
     * @param request
     * @return
     */
    @RequestMapping("/mch_name_all")
    @ResponseBody
    public ResponseEntity<?> mchNameAll(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        MchInfo mchInfo = getObject(param, MchInfo.class);
        List<MchInfo> payPassageList = rpcCommonService.rpcPayPassageService.selectAll(mchInfo);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payPassageList));
    }

    /**
     * 所有代付通道列表
     * @param request
     * @return
     */
    @RequestMapping("/agentpay_passage_all")
    @ResponseBody
    public ResponseEntity<?> agentpayPassageAll(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        AgentpayPassage agentpayPassage = getObject(param, AgentpayPassage.class);
        agentpayPassage.setStatus(MchConstant.PUB_YES);
        List<AgentpayPassage> agentpayPassageList = rpcCommonService.rpcAgentpayPassageService.selectAll(agentpayPassage);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(agentpayPassageList));
    }

    /**
     * 根据产品ID得到对应的支付通道列表
     * @param request
     * @return
     */
    @RequestMapping("/pay_passage_product")
    @ResponseBody
    public ResponseEntity<?> payPassage4ProductId(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer productId = getInteger(param, "productId");
        PayProduct payProduct = rpcCommonService.rpcPayProductService.findById(productId);
        if(payProduct == null) {
            return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_MGR_PAY_PRODUCT_NOT_EXIST));
        }
        String payType = payProduct.getPayType();
        List<PayPassage> payPassageList = rpcCommonService.rpcPayPassageService.selectAllByPayType(payType);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payPassageList));
    }

    /**
     * 根据支付通道ID得到子账户列表
     * @param request
     * @return
     */
    @RequestMapping("/pay_passage_account")
    @ResponseBody
    public ResponseEntity<?> payPassageAccount4PassageId(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer payPassageId = getInteger(param, "payPassageId");
        List<PayPassageAccount> payPassageAccountList = new LinkedList<>();
        if(payPassageId != null) {
            payPassageAccountList = rpcCommonService.rpcPayPassageAccountService.selectAllByPassageId(payPassageId);
        }
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payPassageAccountList));
    }

    /**
     * 根据代付通道ID得到子账户列表
     * @param request
     * @return
     */
    @RequestMapping("/agentpay_passage_account")
    @ResponseBody
    public ResponseEntity<?> agentpayPassageAccount4PassageId(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer agentpayPassageId = getInteger(param, "agentpayPassageId");
        List<AgentpayPassageAccount> agentpayPassageAccountList = new LinkedList<>();
        if(agentpayPassageId != null) {
            agentpayPassageAccountList = rpcCommonService.rpcAgentpayPassageAccountService.selectAllByPassageId(agentpayPassageId);
        }
        return ResponseEntity.ok(XxPayResponse.buildSuccess(agentpayPassageAccountList));
    }

}
