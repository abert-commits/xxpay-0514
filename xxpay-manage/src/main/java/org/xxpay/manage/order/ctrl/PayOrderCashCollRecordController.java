package org.xxpay.manage.order.ctrl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.LockUtil;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.common.util.StrUtil;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayOrderCashCollRecord;
import org.xxpay.core.entity.PayPassageAccount;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;
import org.xxpay.manage.secruity.JwtUser;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/pay_cash_coll_record")
public class PayOrderCashCollRecordController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;


    @Autowired
    private RedisTemplate redisTemplate;




    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        //资金归集起止时间
        Date createTimeStart = null;
        Date createTimeEnd = null;
        Integer passageAccountId = getInteger(param, "passageAccountId");

        String ymd = DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD);
        String createTimeStartStr = getString(param, "createTimeStart");
        if (StringUtils.isNotBlank(createTimeStartStr)) {
            createTimeStart = DateUtil.str2date(createTimeStartStr);
        } else {
            createTimeStart = DateUtil.str2date(ymd + " 00:00:00");
        }
        String createTimeEndStr = getString(param, "createTimeEnd");
        if (StringUtils.isNotBlank(createTimeEndStr)) createTimeEnd = DateUtil.str2date(createTimeEndStr);
        PayOrderCashCollRecord record = getObject(param, PayOrderCashCollRecord.class);
        int count = rpcCommonService.rpcPayOrderCashCollRecordService.count(record, createTimeStart, createTimeEnd);
        if (count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<PayOrderCashCollRecord> result = rpcCommonService.rpcPayOrderCashCollRecordService.select((getPageIndex(param) - 1) * getPageSize(param), getPageSize(param), record, createTimeStart, createTimeEnd);
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(result, count));
    }


    /**
     * 资金归集订单统计
     *
     * @return
     */
    @RequestMapping("/count")
    @ResponseBody
    public ResponseEntity<?> count(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String payOrderId = getString(param, "payOrderId");
        String channelOrderNo = getString(param, "channelOrderNo");
        String transInUserId = getString(param, "transInUserId");
        Integer status = getInteger(param, "status");
        Integer passageAccountId = getInteger(param, "passageAccountId");

        //资金归集起止时间
        Date createTimeStart = null;
        Date createTimeEnd = null;
        String ymd = DateUtil.date2Str(new Date(), DateUtil.FORMAT_YYYY_MM_DD);
        String createTimeStartStr = getString(param, "createTimeStart");
        if (StringUtils.isNotBlank(createTimeStartStr)) {
            createTimeStart = DateUtil.str2date(createTimeStartStr);
        } else {
            createTimeStart = DateUtil.str2date(ymd + " 00:00:00");
        }


        String createTimeEndStr = getString(param, "createTimeEnd");
        if (StringUtils.isNotBlank(createTimeEndStr)) createTimeEnd = DateUtil.str2date(createTimeEndStr);

        Map<String, Object> map = new HashMap<>();
        if (payOrderId != null) {
            map.put("payOrderId", payOrderId);
        }

        if (channelOrderNo != null) {
            map.put("channelOrderNo", channelOrderNo);
        }

        if (transInUserId != null) {
            map.put("transInUserId", transInUserId);
        }

        if (createTimeStart != null) {
            map.put("createTimeStart", createTimeStart);
        }

        if (createTimeEnd != null) {
            map.put("createTimeEnd", createTimeEnd);
        }

        if (passageAccountId != null) {
            map.put("passageAccountId", passageAccountId);
        }

        map.put("status", 1);
        Map transSuccessMap = rpcCommonService.rpcPayOrderCashCollRecordService.transSuccess(map);

        map.put("status", 0);
        Map transFailMap = rpcCommonService.rpcPayOrderCashCollRecordService.transSuccess(map);

        JSONObject obj = new JSONObject();
        if (transSuccessMap != null) {
            obj.put("transSuccessAmount", transSuccessMap.get("transSuccessAmount")); //分账成功总金额
        } else {
            obj.put("transSuccessAmount", 0); //分账成功总金额
        }

        obj.put("totalAmount", 0); //应分金额
        if (transFailMap != null) {
            obj.put("transFailAmount", transFailMap.get("transSuccessAmount")); //分账失败总金额
        } else {
            obj.put("transFailAmount", 0); //分账失败总金额
        }

        //延迟结算
        map.put("status", 2);
        Map allLateMap = rpcCommonService.rpcPayOrderCashCollRecordService.transSuccess(map);

        if (allLateMap != null) {
            obj.put("transLateAmount", allLateMap.get("transSuccessAmount"));

        } else {
            obj.put("transLateAmount", 0);
        }

        map.put("status", 1);
        map.put("type", -1);
        Map shoudongMap = rpcCommonService.rpcPayOrderCashCollRecordService.transSuccess(map);

        if (shoudongMap != null) {
            obj.put("shoudongAmount", shoudongMap.get("transSuccessAmount"));

        } else {
            obj.put("shoudongAmount", 0);
        }

        Map refundMap = rpcCommonService.rpcPayOrderService.count4Refund(map);

        if (refundMap != null) {
            obj.put("refundTotalAmount", refundMap.get("totalAmount"));
        } else {
            obj.put("refundTotalAmount", "0");
        }

        return ResponseEntity.ok(XxPayResponse.buildSuccess(obj));
    }


    /**
     * 支付宝单笔转账
     *
     * @return
     */
    @RequestMapping("/tranInAmount")
    @ResponseBody
    public ResponseEntity<?> tranInAmount(HttpServletRequest request) {
        JSONObject resObj = new JSONObject();
        try {
            JwtUser jwtUser = getUser();
            if (jwtUser.getIsSuperAdmin() != 1) {
                resObj.put("retMsg", "权限不足,请联系超级管理员");
                resObj.put("retCode", "fail");
                return ResponseEntity.ok(XxPayResponse.buildSuccess(resObj));
            }

            SortedMap map = new TreeMap();
            String transAmount = request.getParameter("transAmount");
            String payPassageAccountId = request.getParameter("payPassageAccountId");
            String dateTemp = DateUtil.getSeqString();//时间错
            map.put("transAmount", transAmount);
            map.put("dateTemp", dateTemp);
            map.put("payPassageAccountId", payPassageAccountId);
            if (StringUtils.isBlank(transAmount)) {
                return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_SERVICE_AMOUNT_ERROR));
            }

            Integer myAmount = Integer.parseInt(transAmount);
            if (myAmount <= 0 && myAmount >= 50000) {
                return ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_SERVICE_AMOUNT_ERROR));
            }


            String sign = PayDigestUtil.getSign(map, "4F6B1A7BE25A3D8FB3E1149213EDF60D");
            map.put("sign", sign.toUpperCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String res = XXPayUtil.doPostQueryCmd(mainConfig.getPayUrl() + "/trans/transAmount", sendMsg);
            resObj = JSONObject.parseObject(res);
            return ResponseEntity.ok(XxPayResponse.buildSuccess(resObj));
        } catch (Exception ex) {
            ex.printStackTrace();
            resObj.put("retMsg", "系统异常：" + ex.getMessage());
            resObj.put("retCode", "fail");
            return ResponseEntity.ok(XxPayResponse.buildSuccess(resObj));
        }
    }

    private final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDp6clRDrwDaUbvJasISy8TPqBQgwOg5okXg+r9qysJEs/7PpPPEHTeKmg4HU+ujwkBiKJj1OzdJjd7SV4lhKPJlMQFA+48mZA28rMhqLfSRGwLQ3Vq5iHt1BPDLwwFtNmoQX5uGa0/BOpreC86qfGgENBarBSZEdV24KSfyEw47QIDAQAB";


    @RequestMapping("/querymerchantSplit")
    @ResponseBody
    public ResponseEntity<?> querymerchantSplit(HttpServletRequest request) {
        String msg = null;
        String logPrefix = "订单分账查询";
        JSONObject retObj = new JSONObject();
        try {

            JSONObject param = getJsonParam(request);
            String payOrderId = getString(param, "orderId");

            PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderId);
            PayPassageAccount payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(payOrder.getPassageAccountId());


            JSONObject alipayConfig = JSONObject.parseObject(payPassageAccount.getParam());
            String payOrderJson = JSONObject.toJSONString(payOrder);
            byte[] data = payOrderJson.getBytes();
            byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
            byte[] alipayConfigByte = payPassageAccount.getParam().getBytes();

            byte[] encodedDataByConfif = RSAUtils.encryptByPublicKey(alipayConfigByte, publicKey);
            String payOrderRsa = Base64Utils.encode(encodedData);
            String alipayConfigRsa = Base64Utils.encode(encodedDataByConfif);

            Map map = new HashMap();
            map.put("payOrder", payOrderRsa);
            map.put("alipayConfig", alipayConfigRsa);
            String sendMsg = XXPayUtil.mapToString(map);
            String res = XXPayUtil.doPostQueryCmd(alipayConfig.getString("reqUrl") + "/querymerchantSplit", sendMsg);
            JSONObject resObj = JSONObject.parseObject(res);

            if (resObj.getString("status").equals("fail")) {
                retObj.put("msg", "查询失败，支付宝返回：" + resObj.getString("msg"));
                return ResponseEntity.ok(retObj);
            }

            if (resObj.getString("status").equals("3")) {
                retObj.put("msg", "延迟结算,未分账成功!");
                return ResponseEntity.ok(retObj);
            }

            retObj.put("msg", "分账成功,成功金额：" + resObj.getString("amount"));
            return ResponseEntity.ok(retObj);
        } catch (Exception e) {
            retObj.put("msg", "查询订单分账信息异常");
            return ResponseEntity.ok(retObj);
        }
    }


    /**
     * 查询触发风控自动关闭的通道子账户
     *
     * @param request
     * @return
     */
    @RequestMapping("/queryPassageAccountIdColose")
    @ResponseBody
    public ResponseEntity<?> queryPassageAccountIdColose(HttpServletRequest request) {
        String key = "passageAccountIdClose";
        List<Long> list = (List<Long>) redisTemplate.opsForValue().get(key);
        if (list == null) {
            list = new LinkedList<>();
        }
        return ResponseEntity.ok(XxPayResponse.buildSuccess(list));
    }


    /**
     * 开启触发风控自动关闭的通道子账户
     *
     * @param request
     * @return
     */
    @RequestMapping("/openPassageAccountId")
    @ResponseBody
    public ResponseEntity<?> openPassageAccountId(HttpServletRequest request) {
        String key = "passageAccountIdClose";
        JSONObject retObj = new JSONObject();
        JSONObject param = getJsonParam(request);
        Long passageAccountId = getLong(param, "passageAccountId");
        List<Long> list = (List<Long>) redisTemplate.opsForValue().get(key);
        if (list == null) {
            retObj.put("retMsg", "未找到触发风控自动关闭的通道子账户,请前往支付通道下的子账户核实!");
            return ResponseEntity.ok(XxPayResponse.buildSuccess(retObj));
        }

        List<Long> data = list.stream().filter(a -> a.longValue() == passageAccountId.longValue()).collect(Collectors.toList());
        if (data == null || data.size() == 0) {
            retObj.put("retMsg", "未找到触发风控自动关闭的通道子账户,请前往支付通道下的子账户核实!");
            return ResponseEntity.ok(XxPayResponse.buildSuccess(retObj));
        }

        PayPassageAccount account = new PayPassageAccount();
        account.setId(Integer.parseInt(String.valueOf(passageAccountId)));
        account.setStatus(MchConstant.PUB_YES);
        rpcCommonService.rpcPayPassageAccountService.update(account);
        list.remove(passageAccountId);
        redisTemplate.opsForValue().set(key, list, 10, TimeUnit.MINUTES);

        retObj.put("retMsg", MessageFormat.format("通道子账户:{0},开启成功！", passageAccountId));
        return ResponseEntity.ok(XxPayResponse.buildSuccess(retObj));
    }


//     LockUtil.lock(KEY + payOrder.getMchId(), 6);
//                try {
//        updatePayOrderRows = rpcCommonService.rpcPayOrderService.updateStatus4Success(payOrder.getPayOrderId(), StrUtil.toString(params.get("trade_no"), null));
//    } catch (Exception e) {
//
//    } finally {
//        //释放锁
//        LockUtil.unlock(KEY + payOrder.getMchId());
//    }


    /**
     * 订单单笔转账补发
     *
     * @return
     */
    @RequestMapping("/reissueTransAmount")
    @ResponseBody
    public ResponseEntity<?> reissueTransAmount(HttpServletRequest request) {
        JSONObject resObj = new JSONObject();
        String key = "";
        SortedMap map = new TreeMap();
        String id = request.getParameter("id");
        String dateTemp = DateUtil.getSeqString();//时间错
        map.put("dateTemp", dateTemp);
        map.put("id", id);
        try {
            //加锁 休眠进程 避免大并发 导致掉单
            LockUtil.lock(id, 6);
            PayOrderCashCollRecord record = rpcCommonService.rpcPayOrderCashCollRecordService.findById(Integer.parseInt(id));
            if (record.getStatus() == 1) {
                resObj.put("retMsg", "该资金归集订单已分账成功，请勿重复点击！");
                resObj.put("retCode", "fail");
                return ResponseEntity.ok(XxPayResponse.buildSuccess(resObj));
            }

            map.put("transAmount", String.valueOf(record.getTransInAmount()));
            String sign = PayDigestUtil.getSign(map, "4F6B1A7BE25A3D8FB3E1149213EDF60D");
            map.put("sign", sign.toUpperCase());
            String sendMsg = XXPayUtil.mapToString(map).replace(">", "");
            String res = XXPayUtil.doPostQueryCmd(mainConfig.getPayUrl() + "/trans/reissueTransAmount", sendMsg);
            resObj = JSONObject.parseObject(res);
            return ResponseEntity.ok(XxPayResponse.buildSuccess(resObj));
        } catch (Exception ex) {
            ex.printStackTrace();
            resObj.put("retMsg", "系统异常：" + ex.getMessage());
            resObj.put("retCode", "fail");
            return ResponseEntity.ok(XxPayResponse.buildSuccess(resObj));
        } finally {
            //释放锁
            LockUtil.unlock(id);
        }
    }
}
