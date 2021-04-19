package org.xxpay.pay.ctrl.payment;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.constant.PayEnum;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.PayCashCollConfig;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayOrderCashCollRecord;
import org.xxpay.core.entity.PayPassageAccount;
import org.xxpay.pay.channel.PaymentInterface;
import org.xxpay.pay.channel.alipay.AlipayCashCollService;
import org.xxpay.pay.channel.alipay.AlipayConfig;
import org.xxpay.pay.ctrl.common.BaseController;
import org.xxpay.pay.mq.rocketmq.normal.BaseNotify5MchPay;
import org.xxpay.pay.service.RpcCommonService;
import org.xxpay.pay.service.TransOrderService;
import org.xxpay.pay.util.SpringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dingzhiwei jmdhappy@126.com
 * @version V1.0
 * @Description: 转账
 * @date 2017-10-30
 * @Copyright: www.xxpay.org
 */
@RestController
public class TransOrderController extends BaseController {

    private final MyLog _log = MyLog.getLog(TransOrderController.class);

    @Autowired
    private TransOrderService transOrderService;

    @Autowired
    private RpcCommonService rpcCommonService;

    @Autowired
    private AlipayCashCollService alipayCashCollService;


    @Autowired
    public BaseNotify5MchPay baseNotify4MchPay;

    /**
     * 统一转账接口:
     * 1)先验证接口参数以及签名信息
     * 2)验证通过创建支付订单
     * 3)根据商户选择渠道,调用支付服务进行下单
     * 4)返回下单数据
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/trans/create_order")
    public String transOrder(HttpServletRequest request) {
        _log.info("###### 开始接收商户统一转账请求 ######");
        try {
            JSONObject po = getJsonParam(request);
            return transOrderService.createTransOrder(po);
        } catch (Exception e) {
            _log.error(e, "");
            return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "支付中心系统异常", null, null));
        }
    }


    private final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDp6clRDrwDaUbvJasISy8TPqBQgwOg5okXg+r9qysJEs/7PpPPEHTeKmg4HU+ujwkBiKJj1OzdJjd7SV4lhKPJlMQFA+48mZA28rMhqLfSRGwLQ3Vq5iHt1BPDLwwFtNmoQX5uGa0/BOpreC86qfGgENBarBSZEdV24KSfyEw47QIDAQAB";

    /**
     * 支付宝单笔转账
     * 1)先验证接口参数以及签名信息
     *
     * @param request
     * @return
     */
    @RequestMapping("/api/trans/transAmount")
    @ResponseBody
    public String transAmount(HttpServletRequest request) {
        _log.info("######支付宝单笔转账 ######");
        String logPrefix = "【支付宝单笔转账】";
        String myKey = "4F6B1A7BE25A3D8FB3E1149213EDF60D";
        try {
            JSONObject po = getJsonParam(request);
            _log.info("{}请求参数:{}", logPrefix, po);
            SortedMap map = new TreeMap();
            String transAmount = request.getParameter("transAmount");
            String dateTemp = request.getParameter("dateTemp");
            String payPassageAccountId = request.getParameter("payPassageAccountId");
            String reqSign1 = request.getParameter("sign");


            map.put("transAmount", transAmount);
            map.put("dateTemp", dateTemp);
            map.put("payPassageAccountId", payPassageAccountId);
            String sign = PayDigestUtil.getSign(map, myKey).toUpperCase();//内部私用Key
            //签名验证
            if (!sign.equals(reqSign1)) {
                _log.info("{}参数校验不通过:{}", logPrefix, "请求失败，验证签名不通过!");
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "请求失败，验证签名不通过!", null, PayEnum.ERR_0121.getCode(), ""));
            }

            PayOrderCashCollRecord record = new PayOrderCashCollRecord();
            PayPassageAccount payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(Integer.parseInt(payPassageAccountId));

            if (payPassageAccount == null) {
                _log.info("{}参数校验不通过:{}", logPrefix, "未找到[" + payPassageAccountId + "]通道子账户信息");
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "未找到[" + payPassageAccountId + "]通道子账户信息!", null, PayEnum.ERR_0014.getCode(), ""));
            }


            if (payPassageAccount.getCashCollStatus() != MchConstant.PUB_YES) {
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "该账户未开启需资金归集服务!", null, PayEnum.ERR_0014.getCode(), ""));
            }

            PayCashCollConfig selectCondition = new PayCashCollConfig();
            selectCondition.setStatus(MchConstant.PUB_YES); //仅查询开启状态
            if (payPassageAccount.getCashCollMode() == 1) { //继承
                selectCondition.setBelongPayAccountId(0); //查询系统全局配置
            } else {
                selectCondition.setBelongPayAccountId(payPassageAccount.getId()); //查询子账户的特有配置账号
            }

            selectCondition.setBelongPayAccountId(Integer.parseInt(payPassageAccountId));
            selectCondition.setType(1);
            List<PayCashCollConfig> configList = rpcCommonService.rpcPayCashCollConfigService.selectAll(selectCondition);
            if (configList == null) {
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "未找到通道子账户下的资金归集配置!", null, PayEnum.ERR_0014.getCode(), ""));
            }

            PayCashCollConfig collConfig = configList.get(0);
            //实例化资金归集信息
            record.setTransInUserName(collConfig.getTransInUserName());
            record.setTransInUserAccount(collConfig.getTransInUserAccount());
            record.setType(-1);
            record.setPassageAccountId(payPassageAccountId);
            record.setCreateTime(new Date());
            record.setTransInAmount(Long.valueOf(transAmount) * 100);
            record.setTransInPercentage(new BigDecimal(100));
            record.setPayOrderId("SD" + DateUtil.getRevTime());
            record.setTransInUserId(collConfig.getTransInUserId());
            record.setPassageAccountId(String.valueOf(collConfig.getBelongPayAccountId()));
            record.setRemark("转账");

            //这里改成API请求
            String recordJson = JSONObject.toJSONString(record);
            byte[] data = recordJson.getBytes();
            byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
            String recordRsa = Base64Utils.encode(encodedData);
            map.put("payOrderCashColl", recordRsa);

            AlipayConfig alipayConfig = new AlipayConfig(payPassageAccount.getParam());
            byte[] alipayConfigByte = payPassageAccount.getParam().getBytes();
            byte[] encodedDataByConfif = RSAUtils.encryptByPublicKey(alipayConfigByte, publicKey);
            String alipayConfigRsa = Base64Utils.encode(encodedDataByConfif);
            map.put("alipayConfig", alipayConfigRsa);
            String sendMsg = XXPayUtil.mapToString(map);
            String res = XXPayUtil.doPostQueryCmd(alipayConfig.getReqUrl() + "/alipaytrans", sendMsg);
            JSONObject tranRes = JSONObject.parseObject(res);

            boolean isSuccess = "success".equals(tranRes.getString("state"));
            record.setStatus(isSuccess ? MchConstant.PUB_YES : (byte) -1);
            record.setRemark("后台手动转账=>" + tranRes.getString("msg"));
            record.setChannelOrderNo(tranRes.getString("channelOrderNo"));
            rpcCommonService.rpcPayOrderCashCollRecordService.add(record);

            Map resMap = new HashMap();
            resMap.put("retMsg", tranRes.getString("msg"));
            resMap.put("retCode", tranRes.getString("state"));
            resMap.put("dateTemp", DateUtil.getSeqString());
            String resSign = PayDigestUtil.getSign(map, "4F6B1A7BE25A3D8FB3E1149213EDF60D");
            resMap.put("sign", sign.toUpperCase());
            return JSON.toJSONString(resMap);
        } catch (Exception e) {
            _log.error(e, "");
            return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "系统异常", null, PayEnum.ERR_0010.getCode(), "请联系技术人员查看"));
        }
    }


    /**
     * 支付订单补发单笔转账
     * 1)先验证接口参数以及签名信息
     *
     * @param request
     * @return
     */
    @RequestMapping("/api/trans/reissueTransAmount")
    @ResponseBody
    public String reissueTransAmount(HttpServletRequest request) {
        _log.info("######支付宝单笔转账 ######");
        String logPrefix = "【支付宝单笔转账】";
        String myKey = "4F6B1A7BE25A3D8FB3E1149213EDF60D";
        try {
            JSONObject po = getJsonParam(request);
            _log.info("{}请求参数:{}", logPrefix, po);
            SortedMap map = new TreeMap();
            String transAmount = request.getParameter("transAmount");
            String dateTemp = request.getParameter("dateTemp");
            String id = request.getParameter("id");
            String reqSign1 = request.getParameter("sign");


            map.put("transAmount", transAmount);
            map.put("dateTemp", dateTemp);
            map.put("id", id);
            String sign = PayDigestUtil.getSign(map, myKey).toUpperCase();//内部私用Key
            //签名验证
            if (!sign.equals(reqSign1)) {
                _log.info("{}参数校验不通过:{}", logPrefix, "请求失败，验证签名不通过!");
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "请求失败，验证签名不通过!", null, PayEnum.ERR_0121.getCode(), ""));
            }

            PayOrderCashCollRecord payOrderCashCollRecord = rpcCommonService.rpcPayOrderCashCollRecordService.findById(Integer.parseInt(id));
            PayPassageAccount payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(Integer.valueOf(payOrderCashCollRecord.getPassageAccountId()));

            if (payPassageAccount == null) {
                _log.info("{}参数校验不通过:{}", logPrefix, "未找到[" + payOrderCashCollRecord.getPassageAccountId() + "]通道子账户信息");
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "未找到[" + payOrderCashCollRecord.getPassageAccountId() + "]通道子账户信息!", null, PayEnum.ERR_0014.getCode(), ""));
            }


            if (payPassageAccount.getCashCollStatus() != MchConstant.PUB_YES) {
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "该账户未开启需资金归集服务!", null, PayEnum.ERR_0014.getCode(), ""));
            }


            PayCashCollConfig selectCondition = new PayCashCollConfig();
            selectCondition.setStatus(MchConstant.PUB_YES); //仅查询开启状态
            if (payPassageAccount.getCashCollMode() == 1) { //继承
                selectCondition.setBelongPayAccountId(0); //查询系统全局配置
            } else {
                selectCondition.setBelongPayAccountId(payPassageAccount.getId()); //查询子账户的特有配置账号
            }

            selectCondition.setBelongPayAccountId(Integer.parseInt(payOrderCashCollRecord.getPassageAccountId()));
            selectCondition.setType(1);
            List<PayCashCollConfig> configList = rpcCommonService.rpcPayCashCollConfigService.selectAll(selectCondition);
            if (configList == null) {
                return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "未找到通道子账户下的资金归集配置!", null, PayEnum.ERR_0014.getCode(), ""));
            }


            PayCashCollConfig collConfig = configList.get(0);
            payOrderCashCollRecord.setTransInUserName(collConfig.getTransInUserName());
            payOrderCashCollRecord.setTransInUserAccount(collConfig.getTransInUserAccount());
            payOrderCashCollRecord.setTransInUserId(collConfig.getTransInUserId());

            //这里改成API请求
            String recordJson = JSONObject.toJSONString(payOrderCashCollRecord);
            byte[] data = recordJson.getBytes();
            byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
            String recordRsa = Base64Utils.encode(encodedData);
            map.put("payOrderCashColl", recordRsa);

            AlipayConfig alipayConfig = new AlipayConfig(payPassageAccount.getParam());
            byte[] alipayConfigByte = payPassageAccount.getParam().getBytes();
            byte[] encodedDataByConfif = RSAUtils.encryptByPublicKey(alipayConfigByte, publicKey);
            String alipayConfigRsa = Base64Utils.encode(encodedDataByConfif);
            map.put("alipayConfig", alipayConfigRsa);
            String sendMsg = XXPayUtil.mapToString(map);
            String res = XXPayUtil.doPostQueryCmd(alipayConfig.getReqUrl() + "/alipaytrans", sendMsg);
            JSONObject tranRes = JSONObject.parseObject(res);
            boolean isSuccess = "success".equals(tranRes.getString("state"));
            //实例化资金归集信息
            payOrderCashCollRecord.setStatus(isSuccess ? MchConstant.PUB_YES : MchConstant.PUB_NO);
            payOrderCashCollRecord.setRemark(tranRes.getString("msg"));
            payOrderCashCollRecord.setChannelOrderNo(tranRes.getString("channelOrderNo"));
            int count = rpcCommonService.rpcPayOrderCashCollRecordService.updateById(payOrderCashCollRecord);

            //补单后，判断该订单下的资金归集是否全部处理完毕
            List<PayOrderCashCollRecord> list = rpcCommonService.rpcPayOrderCashCollRecordService.selectByOrderId(payOrderCashCollRecord.getPayOrderId());
            int failCount = 0;
            for (PayOrderCashCollRecord item : list) {
                if (item.getStatus() == MchConstant.PUB_NO) {
                    failCount++;
                }
            }

            PayOrder payOrder = rpcCommonService.rpcPayOrderService.findByPayOrderId(payOrderCashCollRecord.getPayOrderId());
            if (failCount == 0 && payOrder.getStatus() == PayConstant.PAY_STATUS_SUCCESS && count > 0) {
                //处理完成，通知下游商户
                baseNotify4MchPay.doNotify(payOrder, false);
            }

            Map resMap = new HashMap();
            resMap.put("retMsg", tranRes.getString("msg"));
            resMap.put("retCode", tranRes.getString("state"));
            resMap.put("dateTemp", DateUtil.getSeqString());
            String resSign = PayDigestUtil.getSign(map, "4F6B1A7BE25A3D8FB3E1149213EDF60D");
            resMap.put("sign", sign.toUpperCase());
            return JSON.toJSONString(resMap);
        } catch (Exception e) {
            _log.error(e, "");
            return XXPayUtil.makeRetFail(XXPayUtil.makeRetMap(PayConstant.RETURN_VALUE_FAIL, "系统异常", null, PayEnum.ERR_0010.getCode(), "请联系技术人员查看"));
        }
    }
}
