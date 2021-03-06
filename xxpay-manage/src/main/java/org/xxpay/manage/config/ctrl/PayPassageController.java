package org.xxpay.manage.config.ctrl;

import com.alibaba.fastjson.JSON;
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
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.entity.PayInterface;
import org.xxpay.core.entity.PayInterfaceType;
import org.xxpay.core.entity.PayPassage;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;
import org.xxpay.manage.config.service.CommonConfigService;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author: dingzhiwei
 * @date: 18/05/04
 * @description: ๆฏไป้้
 */
@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/config/pay_passage")
public class PayPassageController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    @Autowired
    private CommonConfigService commonConfigService;

    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayPassage payPassage = getObject(param, PayPassage.class);
        int count = rpcCommonService.rpcPayPassageService.count(payPassage);
        if(count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<PayPassage> payPassageList = rpcCommonService.rpcPayPassageService.select((getPageIndex(param) -1) * getPageSize(param), getPageSize(param), payPassage);
        // ๆฏไปๆฅๅฃ็ฑปๅMap
        Map payInterfaceTypeMap = commonConfigService.getPayInterfaceTypeMap();
        // ๆฏไปๆฅๅฃMap
        Map payInterfaceMap = commonConfigService.getPayInterfaceMap();
        // ๆฏไป็ฑปๅMap
        Map payTypeMap = commonConfigService.getPayTypeMap();

        // ่ฝฌๆขๅ็ซฏๆพ็คบ
        List<JSONObject> objects = new LinkedList<>();
        for(PayPassage info : payPassageList) {
            JSONObject object = (JSONObject) JSON.toJSON(info);
            object.put("payTypeName", payTypeMap.get(info.getPayType()));               // ่ฝฌๆขๆฏไป็ฑปๅๅ็งฐ
            object.put("ifTypeName", payInterfaceTypeMap.get(info.getIfTypeCode()));    // ่ฝฌๆขๆฅๅฃ็ฑปๅๅ็งฐ
            object.put("ifName", payInterfaceMap.get(info.getIfCode()));                // ่ฝฌๆขๆฏไปๆฅๅฃๅ็งฐ
            objects.add(object);
        }
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(objects, count));
    }

    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer id = getIntegerRequired(param, "id");
        PayPassage payPassage = rpcCommonService.rpcPayPassageService.findById(id);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payPassage));
    }

    @RequestMapping("/update")
    @ResponseBody
    @MethodLog( remark = "ไฟฎๆนๆฏไป้้" )
    public ResponseEntity<?> update(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayPassage payPassage = getObject(param, PayPassage.class);
        String ifCode = payPassage.getIfCode();
        PayInterface payInterface = rpcCommonService.rpcPayInterfaceService.findByCode(ifCode);
        if(payInterface == null) {
            return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_MGR_PAY_INTERFACE_NOT_EXIST));
        }
        payPassage.setIfTypeCode(payInterface.getIfTypeCode()); // ่ฎพ็ฝฎๆฏไปๆฅๅฃ็ฑปๅ

        int count = rpcCommonService.rpcPayPassageService.update(payPassage);
        if(count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    @RequestMapping("/risk_update")
    @ResponseBody
    @MethodLog( remark = "ไฟฎๆนๆฏไป้้้ฃๆง" )
    public ResponseEntity<?> updateRisk(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayPassage payPassage = getObject(param, PayPassage.class);
        Long maxDayAmount = payPassage.getMaxDayAmount();
        Long maxEveryAmount = payPassage.getMaxEveryAmount();
        Long minEveryAmount = payPassage.getMinEveryAmount();
        // ๅ่ฝฌๅ
        if(maxDayAmount != null) payPassage.setMaxDayAmount(maxDayAmount * 100);
        if(maxEveryAmount != null) payPassage.setMaxEveryAmount(maxEveryAmount * 100);
        if(minEveryAmount != null) payPassage.setMinEveryAmount(minEveryAmount * 100);
        int count = rpcCommonService.rpcPayPassageService.update(payPassage);
        if(count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    @RequestMapping("/rate_update")
    @ResponseBody
    @MethodLog( remark = "ไฟฎๆนๆฏไป้้่ดน็" )
    public ResponseEntity<?> updateRate(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer id = getIntegerRequired(param, "id");
        String passageRate = getStringRequired(param, "passageRate");
        PayPassage payPassage = new PayPassage();
        payPassage.setId(id);
        payPassage.setPassageRate(new BigDecimal(passageRate));
        int count = rpcCommonService.rpcPayPassageService.updateRate(payPassage);
        if(count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    @RequestMapping("/add")
    @ResponseBody
    @MethodLog( remark = "ๆฐๅขๆฏไป้้" )
    public ResponseEntity<?> add(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayPassage payPassage = getObject(param, PayPassage.class);
        String ifCode = payPassage.getIfCode();
        PayInterface payInterface = rpcCommonService.rpcPayInterfaceService.findByCode(ifCode);
        if(payInterface == null) {
            ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_MGR_PAY_INTERFACE_NOT_EXIST));
        }
        payPassage.setIfTypeCode(payInterface.getIfTypeCode()); // ่ฎพ็ฝฎๆฏไปๆฅๅฃ็ฑปๅ
        payPassage.setPayInterfaceId(payInterface.getId());
        int count = rpcCommonService.rpcPayPassageService.add(payPassage);
        if(count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    /**
     * ๆ?นๆฎๆฏไป้้ID,่ทๅๆฏไป่ดฆๅท้็ฝฎๅฎไนๆ่ฟฐ
     * @param request
     * @return
     */
    @RequestMapping("/pay_config_get")
    @ResponseBody
    public ResponseEntity<?> getPayConfig(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer payPassageId = getIntegerRequired(param, "payPassageId");
        PayPassage payPassage = rpcCommonService.rpcPayPassageService.findById(payPassageId);
        if(payPassage == null) {
            return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_COMM_RECORD_NOT_EXIST));
        }
        String ifCode = payPassage.getIfCode();
        String ifTypeCode = payPassage.getIfTypeCode();

        // ๅฆๆๆฅๅฃ้็ฝฎไบๅฎไนๆ่ฟฐ,ๅไฝฟ็จๆฅๅฃ
        PayInterface payInterface = rpcCommonService.rpcPayInterfaceService.findByCode(ifCode);
        if(payInterface != null && StringUtils.isNotBlank(payInterface.getParam())) {
            // ๆฏไปๆฅๅฃ็ฑปๅMap
            Map payInterfaceTypeMap = commonConfigService.getPayInterfaceTypeMap();
            JSONObject object = (JSONObject) JSON.toJSON(payInterface);
            object.put("ifTypeName", payInterfaceTypeMap.get(payInterface.getIfTypeCode()));
            return ResponseEntity.ok(XxPayResponse.buildSuccess(object));
        }
        // ไฝฟ็จๆฅๅฃ็ฑปๅ้็ฝฎ็ๅฎไนๆ่ฟฐ
        PayInterfaceType payInterfaceType = rpcCommonService.rpcPayInterfaceTypeService.findByCode(ifTypeCode);
        if(payInterfaceType != null && StringUtils.isNotBlank(payInterfaceType.getParam())) {
            // ๆฏไปๆฅๅฃ็ฑปๅMap
            Map payInterfaceTypeMap = commonConfigService.getPayInterfaceTypeMap();
            JSONObject object = (JSONObject) JSON.toJSON(payInterfaceType);
            object.put("ifTypeName", payInterfaceTypeMap.get(payInterfaceType.getIfTypeCode()));
            return ResponseEntity.ok(XxPayResponse.buildSuccess(object));
        }
        return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_COMM_RECORD_NOT_EXIST));
    }

}
