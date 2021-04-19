package org.xxpay.manage.merchantgroup.ctrl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.annotation.MethodLog;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.BizResponse;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.entity.*;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 18/05/05
 * @description: 商户支付通道
 */
@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/mch_group_pay_passage")
public class MchGroupPayPassageController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Long mchGroupId = getLongRequired(param, "mchGroupId");
        MchGroup mchGroup = rpcCommonService.rpcMchGroupService.findByMchGroupId(mchGroupId.intValue());
        if (mchGroup == null) {
            return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_SERVICE_MCHGROUP_NOT_EXIST));
        }

        //// 得到代理商已经配置的支付通道
        //List<AgentPassage> agentPassageList = new LinkedList<>();
        //if(mchInfo.getAgentId() != null) {
        //    agentPassageList = rpcCommonService.rpcAgentPassageService.selectAllByAgentId(mchInfo.getAgentId());
        //}
        //Map<String, AgentPassage> agentPassageMap = new HashMap<>();
        //for(AgentPassage agentPassage : agentPassageList) {
        //    agentPassageMap.put(String.valueOf(agentPassage.getProductId()), agentPassage);
        //}

        // 得到商户组已经配置的支付通道
        List<MchGroupPayPassage> mchGroupPayPassageList = rpcCommonService.rpcMchGroupPayPassageService.selectAllByMchGroupId(mchGroupId);
        Map<String, MchGroupPayPassage> mchGroupPayPassageMap = new HashMap<>();
        for (MchGroupPayPassage mchGroupPayPassage : mchGroupPayPassageList) {
            mchGroupPayPassageMap.put(String.valueOf(mchGroupPayPassage.getProductId()), mchGroupPayPassage);
        }


        // 得到所有支付产品
        PayProduct queryPayProduct = new PayProduct();
        queryPayProduct.setStatus(MchConstant.PUB_YES);
        List<PayProduct> payProductList = rpcCommonService.rpcPayProductService.selectAll(queryPayProduct);

        // 合并后返回的商户支付通道
        List<JSONObject> objects = new LinkedList<>();
        for (PayProduct payProduct : payProductList) {
            MchGroupPayPassage mchGroupPayPassage = mchGroupPayPassageMap.get(String.valueOf(payProduct.getId()));
            //PayProduct payProduct = payProductMap.get(String.valueOf(mchPayPassage.getProductId()));
            if (mchGroupPayPassage == null) {
                mchGroupPayPassage = new MchGroupPayPassage();
                mchGroupPayPassage.setMchGroupId(mchGroupId);
                mchGroupPayPassage.setProductId(payProduct.getId());
                mchGroupPayPassage.setProductType(payProduct.getProductType());
                mchGroupPayPassage.setMchRate(payProduct.getMchRate());
                mchGroupPayPassage.setIfMode(payProduct.getIfMode() == null ? ((byte) 1) : payProduct.getIfMode());
                mchGroupPayPassage.setStatus(MchConstant.PUB_NO);
            }
            JSONObject object = (JSONObject) JSON.toJSON(mchGroupPayPassage);
            object.put("productName", payProduct.getProductName());
            //if(agentPassageMap.get(String.valueOf(mchPayPassage.getProductId())) != null) {
            //    object.put("agentRate", agentPassageMap.get(String.valueOf(mchPayPassage.getProductId())).getAgentRate());
            //}else if(payProduct.getAgentRate() != null) {
            //    object.put("agentRate", payProduct.getAgentRate());
            //}
            objects.add(object);
        }
        return ResponseEntity.ok(XxPayResponse.buildSuccess(objects));
    }

    /**
     * 查询
     *
     * @return
     */
    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Long mchGroupId = getLongRequired(param, "mchGroupId");
        MchGroup mchGroupInfo = rpcCommonService.rpcMchGroupService.findByMchGroupId(mchGroupId.intValue());
        if (mchGroupInfo == null) {
            return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_SERVICE_MCHGROUP_NOT_EXIST));
        }
        Integer productId = getIntegerRequired(param, "productId");
//        AgentPassage agentPassage = null;
//        if (mchGroupInfo.getAgentId() != null) {
//            agentPassage = rpcCommonService.rpcAgentPassageService.findByAgentIdAndProductId(mchGroupInfo.getAgentId(), productId);
//        }
        PayProduct payProduct = rpcCommonService.rpcPayProductService.findById(productId);
        if (payProduct == null) return ResponseEntity.ok(XxPayResponse.buildSuccess(null));
        MchGroupPayPassage mchGroupPayPassage = rpcCommonService.rpcMchGroupPayPassageService.findByMchGroupIdAndProductId(mchGroupId, productId);
        if (mchGroupPayPassage == null) {
            mchGroupPayPassage = new MchGroupPayPassage();
            mchGroupPayPassage.setMchGroupId(mchGroupId);
            mchGroupPayPassage.setProductId(productId);
            mchGroupPayPassage.setProductType(payProduct.getProductType());
            mchGroupPayPassage.setMchRate(payProduct.getMchRate());
            mchGroupPayPassage.setIfMode(payProduct.getIfMode() == null ? ((byte) 1) : payProduct.getIfMode());
            mchGroupPayPassage.setPayPassageId(payProduct.getPayPassageId());
            mchGroupPayPassage.setPayPassageAccountId(payProduct.getPayPassageAccountId());
            mchGroupPayPassage.setPollParam(payProduct.getPollParam());
            mchGroupPayPassage.setStatus(MchConstant.PUB_NO);
        }
        JSONObject object = (JSONObject) JSON.toJSON(mchGroupPayPassage);
        object.put("productName", payProduct.getProductName());
//        if (agentPassage != null) {
//            object.put("agentRate", agentPassage.getAgentRate());
//        } else if (payProduct.getAgentRate() != null) {
//            object.put("agentRate", payProduct.getAgentRate());
//        } else {
//            object.put("agentRate", "");
//        }
//        object.put("agentId", mchInfo.getAgentId() == null ? "" : mchInfo.getAgentId());
        return ResponseEntity.ok(XxPayResponse.buildSuccess(object));
    }

    @RequestMapping("/update")
    @ResponseBody
    @MethodLog(remark = "配置商户支付通道")
    public ResponseEntity<?> update(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        MchGroupPayPassage mchPayPassage = getObject(param, MchGroupPayPassage.class);
        int count;
        if (mchPayPassage.getId() != null) {
            // 修改
            if (mchPayPassage.getIfMode() == 1) { // 单独
                mchPayPassage.setPollParam(null);
                if (mchPayPassage.getPayPassageAccountId() == null) mchPayPassage.setPayPassageAccountId(0);
            } else if (mchPayPassage.getIfMode() == 2) { // 轮询
                mchPayPassage.setPayPassageId(null);
                mchPayPassage.setPayPassageAccountId(null);
            }
            count = rpcCommonService.rpcMchGroupPayPassageService.update(mchPayPassage);
        } else {
            // 新增
            if (mchPayPassage.getIfMode() == 1) { // 单独
                mchPayPassage.setPollParam(null);
            } else if (mchPayPassage.getIfMode() == 2) { // 轮询
                mchPayPassage.setPayPassageId(null);
                mchPayPassage.setPayPassageAccountId(null);
            }
            count = rpcCommonService.rpcMchGroupPayPassageService.add(mchPayPassage);
        }
        if (count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    /**
     * 轮询查询
     *
     * @return
     */
    @RequestMapping("/poll_get")
    @ResponseBody
    public ResponseEntity<?> getPoll(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Long mchGroupId = getLongRequired(param, "mchGroupId");
        Integer productId = getIntegerRequired(param, "productId");
        PayProduct payProduct = rpcCommonService.rpcPayProductService.findById(productId);
        if (payProduct == null) {
            return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_MGR_PAY_PRODUCT_NOT_EXIST));
        }

        MchGroupPayPassage mchPayPassage = rpcCommonService.rpcMchGroupPayPassageService.findByMchGroupIdAndProductId(mchGroupId, productId);
        String pollParam = "";
        if (mchPayPassage != null) {
            pollParam = mchPayPassage.getPollParam();
        }
        // 得到该类型下的支付通道
        String payType = payProduct.getPayType();
        List<PayPassage> payPassageList = rpcCommonService.rpcPayPassageService.selectAllByPayType(payType);
        if (CollectionUtils.isEmpty(payPassageList)) {
            return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_MGR_PAY_PASSAGE_NOT_EXIST));
        }
        // 得到已经配置的轮询参数,放入map中
        Map<String, JSONObject> pollMap = new HashMap<>();

        if (StringUtils.isNotBlank(pollParam)) {
            JSONArray array = JSONArray.parseArray(pollParam);
            if (CollectionUtils.isNotEmpty(array)) {
                for (int i = 0; i < array.size(); i++) {
                    JSONObject object = array.getJSONObject(i);
                    String pid = object.getString("payPassageId");
                    pollMap.put(pid, object);
                }
            }
        }
        //
        List<JSONObject> objects = new LinkedList<>();
        for (PayPassage payPassage : payPassageList) {
            JSONObject object = (JSONObject) JSON.toJSON(payPassage);
            String payPassgeId = payPassage.getId().toString();

            JSONObject pollObj = pollMap.get(payPassgeId);
            if (pollObj == null) {
                object.put("LAY_CHECKED", false);
                object.put("weight", 1);
            } else {
                object.put("LAY_CHECKED", true);
                object.put("weight", pollObj.getIntValue("weight"));
            }
            objects.add(object);

        }
        return ResponseEntity.ok(XxPayResponse.buildSuccess(objects));
    }

}
