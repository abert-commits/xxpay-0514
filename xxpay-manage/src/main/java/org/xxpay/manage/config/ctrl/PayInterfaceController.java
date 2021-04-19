package org.xxpay.manage.config.ctrl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.annotation.MethodLog;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.BizResponse;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.entity.PayInterface;
import org.xxpay.core.entity.PayPassage;
import org.xxpay.core.entity.PayPassageAccount;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;
import org.xxpay.manage.common.util.SeqUtils;
import org.xxpay.manage.config.service.CommonConfigService;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author: dingzhiwei
 * @date: 18/05/03
 * @description: 支付接口
 */
@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/config/pay_interface")
public class PayInterfaceController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    @Autowired
    private CommonConfigService commonConfigService;

    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayInterface payInterface = getObject(param, PayInterface.class);
        int count = rpcCommonService.rpcPayInterfaceService.count(payInterface);
        if(count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<PayInterface> payInterfaceList = rpcCommonService.rpcPayInterfaceService.select((getPageIndex(param) -1) * getPageSize(param), getPageSize(param), payInterface);
        // 支付接口类型Map
        Map payInterfaceTypeMap = commonConfigService.getPayInterfaceTypeMap();
        // 支付类型Map
        Map payTypeMap = commonConfigService.getPayTypeMap();
        // 转换前端显示
        List<JSONObject> objects = new LinkedList<>();
        for(PayInterface info : payInterfaceList) {
            JSONObject object = (JSONObject) JSON.toJSON(info);
            object.put("payTypeName", payTypeMap.get(info.getPayType()));               // 转换支付类型名称
            object.put("ifTypeName", payInterfaceTypeMap.get(info.getIfTypeCode()));   // 转换接口类型名称
            objects.add(object);
        }
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(objects, count));
    }

    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String ifCode = getStringRequired(param, "ifCode");
        PayInterface payInterface = rpcCommonService.rpcPayInterfaceService.findByCode(ifCode);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payInterface));
    }

    @RequestMapping("/update")
    @ResponseBody
    @MethodLog( remark = "修改支付接口信息" )
    public ResponseEntity<?> update(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayInterface payInterface = getObject(param, PayInterface.class);
        rpcCommonService.rpcPayInterfaceService.update(payInterface);
        //修改支付通道接口代码
        PayPassage payPassage = new PayPassage();
        payPassage.setPayInterfaceId(payInterface.getId());
        payPassage.setIfCode(payInterface.getIfCode());
        rpcCommonService.rpcPayPassageService.updatePassage(payPassage);

        //根据支付接口id  查询支付通道Id
        PayPassage payPassageOld = rpcCommonService.rpcPayPassageService.findByPayInterfaceId(payPassage.getPayInterfaceId());
        //修改支付通道账户接口代码
        PayPassageAccount payPassageAccount = new PayPassageAccount();
        payPassageAccount.setPayPassageId(payPassageOld.getId());
        payPassageAccount.setIfCode(payPassage.getIfCode());
        int count = rpcCommonService.rpcPayPassageAccountService.updateByPrimaryKeyAccount(payPassageAccount);
        if (count > 0) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    @RequestMapping("/add")
    @ResponseBody
    @MethodLog( remark = "新增支付接口信息" )
    public ResponseEntity<?> add(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayInterface payInterface = getObject(param, PayInterface.class);
        payInterface.setId(Integer.valueOf((int) SeqUtils.nextId()));
        int count = rpcCommonService.rpcPayInterfaceService.add(payInterface);
        if(count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    public static void main(String[] args) {
        System.out.println(SeqUtils.nextId());
    }
}
