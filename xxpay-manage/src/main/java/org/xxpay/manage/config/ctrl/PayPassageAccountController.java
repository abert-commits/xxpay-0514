package org.xxpay.manage.config.ctrl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.JsonUtil;
import org.xxpay.core.entity.AgentAccount;
import org.xxpay.core.entity.PayCashCollConfig;
import org.xxpay.core.entity.PayPassage;
import org.xxpay.core.entity.PayPassageAccount;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;
import org.xxpay.manage.config.service.CommonConfigService;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: dingzhiwei
 * @date: 18/05/04
 * @description: 支付通道账户
 */
@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/config/pay_passage_account")
public class PayPassageAccountController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    @Autowired
    private CommonConfigService commonConfigService;

    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayPassageAccount payPassageAccount = getObject(param, PayPassageAccount.class);
        int count = rpcCommonService.rpcPayPassageAccountService.count(payPassageAccount);
        if (count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<PayPassageAccount> payPassageAccountList = rpcCommonService.rpcPayPassageAccountService.select((getPageIndex(param) - 1) * getPageSize(param), getPageSize(param), payPassageAccount);
        // 支付接口类型Map
        Map payInterfaceTypeMap = commonConfigService.getPayInterfaceTypeMap();
        // 支付接口Map
        Map payInterfaceMap = commonConfigService.getPayInterfaceMap();
        // 转换前端显示
        List<JSONObject> objects = new LinkedList<>();
        String ymd = DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD);
        for (PayPassageAccount info : payPassageAccountList) {
            JSONObject object = (JSONObject) JSON.toJSON(info);
            object.put("ifTypeName", payInterfaceTypeMap.get(info.getIfTypeCode()));    // 转换接口类型名称
            object.put("ifName", payInterfaceMap.get(info.getIfCode()));                // 转换支付接口名称
            // 今日总交易金额是否满足
            long dayAmount = rpcCommonService.rpcPayOrderService.sumAmount4PayPassageAccount(info.getId(),
                    DateUtil.str2date(ymd + " 00:00:00"),
                    DateUtil.str2date(ymd + " 23:59:59"));

            object.put("totalAmount", AmountUtil.convertCent2Dollar(String.valueOf(dayAmount)));
            objects.add(object);
        }
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(objects, count));
    }

    @RequestMapping("/get")
    @ResponseBody
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer id = getIntegerRequired(param, "id");
        PayPassageAccount payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(id);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payPassageAccount));
    }

    @RequestMapping("/update")
    @ResponseBody
    @MethodLog(remark = "修改支付通道子账户")
    public ResponseEntity<?> update(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayPassageAccount payPassageAccount = getObject(param, PayPassageAccount.class);

        if (payPassageAccount.getRiskMode() != null && payPassageAccount.getRiskMode() == 2) {
            Long maxDayAmount = payPassageAccount.getMaxDayAmount();
            Long maxEveryAmount = payPassageAccount.getMaxEveryAmount();
            Long minEveryAmount = payPassageAccount.getMinEveryAmount();
            // 元转分
            if (maxDayAmount != null) payPassageAccount.setMaxDayAmount(maxDayAmount * 100);
            if (maxEveryAmount != null) payPassageAccount.setMaxEveryAmount(maxEveryAmount * 100);
            if (minEveryAmount != null) payPassageAccount.setMinEveryAmount(minEveryAmount * 100);
        }

        int count = rpcCommonService.rpcPayPassageAccountService.update(payPassageAccount);
        if (count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    @RequestMapping("/add")
    @ResponseBody
    @MethodLog(remark = "新增支付通道子账户")
    public ResponseEntity<?> add(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PayPassageAccount payPassageAccount = getObject(param, PayPassageAccount.class);
        Integer payPassageId = getIntegerRequired(param, "payPassageId");
        PayPassage payPassage = rpcCommonService.rpcPayPassageService.findById(payPassageId);
        if (payPassage == null) {
            return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_MGR_PAY_PASSAGE_NOT_EXIST));
        }
        payPassageAccount.setIfCode(payPassage.getIfCode());            // 设置支付接口代码
        payPassageAccount.setIfTypeCode(payPassage.getIfTypeCode());    // 设置接口类型代码
        payPassageAccount.setPassageRate(payPassage.getPassageRate());  // 设置通道费率
        int count = rpcCommonService.rpcPayPassageAccountService.add(payPassageAccount);
        if (count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }


    /**
     * @return
     */
    @MethodLog(remark = "获取原生支付宝的通道子账户")
    @RequestMapping("/queryAlipayPayPassageAccount")
    @ResponseBody
    public ResponseEntity<?> queryAlipayPayPassageAccount() {

        PayPassageAccount payPassageAccount = new PayPassageAccount();
        payPassageAccount.setStatus((byte) 1);

        List<PayPassageAccount> list = rpcCommonService.rpcPayPassageAccountService.selectAll(payPassageAccount);
        if (list != null && list.size() > 0) {
            list = list.stream().filter(item -> item.getIfCode().contains("alitstpay")).collect(Collectors.toList());
        }
        return ResponseEntity.ok(XxPayResponse.buildSuccess(list));
    }


    /**
     * 批量修改通道子账户状态
     *
     * @param ids
     * @param status
     * @return
     */
    @MethodLog(remark = "批量修改通道子账户状态")
    @RequestMapping("/saveStatus")
    @ResponseBody
    public ResponseEntity<?> saveStatus(String ids, byte status) {

        List<Integer> listIds = JSONObject.parseArray(ids, Integer.class);
        int count = rpcCommonService.rpcPayPassageAccountService.updatesStatus(listIds, status);
        if (count > 0) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    /**
     * 复制分账账号
     *
     * @param belongPayAccountId
     * @return
     */
    @MethodLog(remark = "复制分账账号")
    @RequestMapping("/copy")
    @ResponseBody
    public ResponseEntity<?> copy(Integer belongPayAccountId, Integer payAccountId) {
        if (belongPayAccountId == null) {
            return ResponseEntity.ok(new BizResponse(10001, "复制源为空 请填写复制源"));
        }
        if (payAccountId == null) {
            return ResponseEntity.ok(new BizResponse(10001, "目标源为空 请联系管理员"));
        }
        PayCashCollConfig myPayCashCollConfig = new PayCashCollConfig();
        //查询当前子账户是否有资金归集配置
        myPayCashCollConfig.setBelongPayAccountId(payAccountId);
        List<PayCashCollConfig> myPayCashCollConfigs = rpcCommonService.rpcPayCashCollConfigService.selectAll(myPayCashCollConfig);

        PayCashCollConfig cashCollConfig = new PayCashCollConfig();
        cashCollConfig.setBelongPayAccountId(belongPayAccountId);
        List<PayCashCollConfig> payCashCollConfigs = rpcCommonService.rpcPayCashCollConfigService.selectAll(cashCollConfig);
        if (payCashCollConfigs.size() < 1) {
            return ResponseEntity.ok(new BizResponse(10001, "复制源 分账为空 请重新填写"));
        }
        for (int i = 0; i < myPayCashCollConfigs.size(); i++) {
            for (int j = 0; j < payCashCollConfigs.size(); j++) {
                if(myPayCashCollConfigs.get(i).getTransInUserId().equals(payCashCollConfigs.get(j).getTransInUserId())){
                    payCashCollConfigs.remove(payCashCollConfigs.get(j));
                }
            }
        }

        payCashCollConfigs.stream().forEach(i -> {
            i.setId(null);
            i.setBelongPayAccountId(payAccountId);
            i.setStatus((byte) 0);
            rpcCommonService.rpcPayCashCollConfigService.add(i);
        });

        return ResponseEntity.ok(BizResponse.buildSuccess());
    }

}
