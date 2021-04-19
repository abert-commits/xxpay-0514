package org.xxpay.pay.channel.alipay;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.CertAlipayRequest;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayFundTransToaccountTransferModel;
import com.alipay.api.domain.AlipayTradeRoyaltyRelationBindModel;
import com.alipay.api.domain.RoyaltyEntity;
import com.alipay.api.request.AlipayFundTransToaccountTransferRequest;
import com.alipay.api.request.AlipayTradeOrderSettleRequest;
import com.alipay.api.request.AlipayTradeRoyaltyRelationBindRequest;
import com.alipay.api.response.AlipayFundTransToaccountTransferResponse;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.alipay.api.response.AlipayTradeOrderSettleResponse;
import com.alipay.api.response.AlipayTradeRoyaltyRelationBindResponse;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.*;
import org.xxpay.pay.channel.BaseCashColl;
import org.xxpay.pay.util.Util;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AlipayCashCollService extends BaseCashColl {

    private static final MyLog _log = MyLog.getLog(AlipayCashCollService.class);
    private final String aliPayUrl = "https://openapi.alipay.com/gateway.do";
    private final String privateKey = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAOnpyVEOvANpRu8lqwhLLxM+oFCDA6DmiReD6v2rKwkSz/s+k88QdN4qaDgdT66PCQGIomPU7N0mN3tJXiWEo8mUxAUD7jyZkDbysyGot9JEbAtDdWrmIe3UE8MvDAW02ahBfm4ZrT8E6mt4Lzqp8aAQ0FqsFJkR1XbgpJ/ITDjtAgMBAAECgYEA15mK21GPbj19CjRX/p791ukkbtEzaPzUY35N/F3mnsheNx+osXRjo9rGkOJDbYudK3K66vV5FSWCgfpP8pjdNM7ycahKwFg952MdtNpty3zpyFDkcWeMjVBSso5wtyqvgILfr4qgS3aHMintLSwSQtUjt5DhulzSyX3Z5YsAI8ECQQD2an3sAsSz4z6RfGlIr5tJc5Mi300btwpROGNmUVxAwbqpqq0UeSOgJEzmoMburS7X/v82Dn4QIfGQc0FZk3mlAkEA8wLOsgUUfsJfp7zcdxwD135EDnYm9jRib2vk9DBGulKV1MkbPMF8Z/3yn9UySH8b8pm3y7o7Ur5iuFb84xtPqQJBANQufp9rAtWjJ40/A6mDDMQCsP+mKE9lHY0ycOT5yeY46vKN9NtcNEEBAPbWGnYKyftTp450jDh4AfnQRMVNJ8ECQQCZ8eRZGCjEqIQKcfVEK2YvpJiehLDn9YWKSlKPcunLbTfnxcLQeU5DXrfOEzQ4gvWEeWba085y+5L0bn7jrFCJAkACF7+rox6W+wkuTfrSp4JAf0brfWwJhTV9QhKAYOzPjHU3xQHPcLq8h9rUitBbo1k8gfP+CW0lve7OZf0ecJl5";
    private final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDp6clRDrwDaUbvJasISy8TPqBQgwOg5okXg+r9qysJEs/7PpPPEHTeKmg4HU+ujwkBiKJj1OzdJjd7SV4lhKPJlMQFA+48mZA28rMhqLfSRGwLQ3Vq5iHt1BPDLwwFtNmoQX5uGa0/BOpreC86qfGgENBarBSZEdV24KSfyEw47QIDAQAB";

    @Override
    public JSONObject coll(PayOrder payOrder) throws Exception {

        JSONObject result = new JSONObject();
        result.put("result", "fail");
        try {
            PayPassageAccount account = rpcCommonService.rpcPayPassageAccountService.findById(payOrder.getPassageAccountId());
            if (account.getCashCollStatus() != MchConstant.PUB_YES) {
                result.put("msg", "未开启需资金归集服务");
                String msgSend = MessageFormat.format("预警消息->支付产品:{0}->下的通道子账户:{1}。未开启资金归集服务，请运营人员核查！订单号：{2}", payOrder.getProductId(), payOrder.getPassageAccountId(), payOrder.getPayOrderId());
                TelegramUtil.SendMsg(msgSend);
                return result;
            }

            PayCashCollConfig selectCondition = new PayCashCollConfig();
            selectCondition.setStatus(MchConstant.PUB_YES); //仅查询开启状态

            if (account.getCashCollMode() == 1) { //继承
                selectCondition.setBelongPayAccountId(0); //查询系统全局配置
            } else {
                selectCondition.setBelongPayAccountId(account.getId()); //查询子账户的特有配置账号
            }

            List<PayCashCollConfig> configList = rpcCommonService.rpcPayCashCollConfigService.selectAll(selectCondition);


            if (configList == null || configList.isEmpty()) {
                String msgSend = MessageFormat.format("预警消息->支付产品:{0}->下的通道子账户:{1}。资金归集账号未配置，请运营人员核查！订单号：{2}", payOrder.getProductId(), payOrder.getPassageAccountId(), payOrder.getPayOrderId());
                TelegramUtil.SendMsg(msgSend);
                result.put("msg", "资金归集账号未配置");
                return result;
            }

            configList = configList.stream().filter(g -> g.getType() != 2).collect(Collectors.toList());
            if (configList == null || configList.isEmpty()) {
                String msgSend = MessageFormat.format("预警消息->支付产品:{0}->下的通道子账户:{1}。资金归集账号未配置，请运营人员核查！订单号:{2}", payOrder.getProductId(), payOrder.getPassageAccountId(), payOrder.getPayOrderId());
                TelegramUtil.SendMsg(msgSend);
                result.put("msg", "资金归集账号未配置");
                return result;
            }

            //判断归集配置信息 是否合法
            BigDecimal totalPercentage = BigDecimal.ZERO;
            BigDecimal everyPercentage = new BigDecimal(100).divide(new BigDecimal(configList.size()));

//            int junfen = 0;
//            if (configList.size() == 1) {
//                //如果只有一个分账配置，取百分之百
//                junfen++;
//                totalPercentage.add(everyPercentage);
//
//            } else {
//                for (PayCashCollConfig config : configList) {
//                    BigDecimal transInPercentage = config.getTransInPercentage();
//                    if (transInPercentage.compareTo(BigDecimal.ZERO) <= 0) {
//                        result.put("msg", "资金归集账号配置有误，比例不得小于等于0");
//                        return result;
//                    }
//
//                    totalPercentage.add(transInPercentage);
//                }
//            }

            if (totalPercentage.compareTo(new BigDecimal(100)) > 0) {
                result.put("msg", "资金归集账号配置有误，总比例不得高于100%");
                return result;
            }

            //是否全部分配完成
            boolean isAllColl = totalPercentage.equals(new BigDecimal(100));


            //组装请求报文
            String aliPayConfigJson = getPayParam(payOrder);
            AlipayConfig alipayConfig = new AlipayConfig(aliPayConfigJson);

            byte[] alipayConfigByte = aliPayConfigJson.getBytes();
            byte[] encodedDataByConfif = RSAUtils.encryptByPublicKey(alipayConfigByte, publicKey);
            String alipayConfigRsa = Base64Utils.encode(encodedDataByConfif);
            Map map = new HashMap();
            map.put("alipayConfig", alipayConfigRsa);
            Long availableAmount =new Long(0);
            //订单可分账余额 = 订单金额 - 通道费用
            if (account.getRemark()!=null && account.getRemark().equals("MYFL"))
            {
                availableAmount = payOrder.getAmount() - XXPayUtil.calOrderMultiplyRate(payOrder.getAmount(), new BigDecimal(0.01));
            }else {
                availableAmount = payOrder.getAmount() - XXPayUtil.calOrderMultiplyRate(payOrder.getAmount(), new BigDecimal(0.6));
            }

            //累加已分账金额
            Long sumCollAmount = 0L;
            List<PayOrderCashCollRecord> recordList = new ArrayList<>(); //返回记录结果集
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < configList.size(); i++) {

                PayCashCollConfig config = configList.get(i);
                PayOrderCashCollRecord record = new PayOrderCashCollRecord();
                record.setPayOrderId(payOrder.getPayOrderId());
                record.setChannelOrderNo(payOrder.getChannelOrderNo());
                record.setRequestNo(payOrder.getPayOrderId());
                record.setTransInUserName(config.getTransInUserName());
                record.setTransInUserAccount(config.getTransInUserAccount());
                record.setTransInUserId(config.getTransInUserId());
                record.setType(config.getType());
                record.setPassageAccountId(String.valueOf(payOrder.getPassageAccountId()));

                //使用比例方式 计算本账户归集金额
//                if (junfen == 0) {
//                    //等于0，表示不是均分
//                    everyPercentage = config.getTransInPercentage();
//                }
//
//                //如果是均分，但是比例又小于50的话，表示是新分账，就按新分账自己设置的比例来
//                if (junfen == 1 && config.getTransInPercentage().longValue() < 50) {
//                    everyPercentage = config.getTransInPercentage();
//                }


                Long collAmount = XXPayUtil.calOrderMultiplyRate(availableAmount, everyPercentage);

                //如果分配比例为100%，并且当前为最后一个，将剩余金额作为本账户的归集金额
                if (isAllColl && i == (configList.size() - 1)) {
                    collAmount = availableAmount - sumCollAmount;
                }

                record.setTransInPercentage(everyPercentage);
                record.setTransInAmount(collAmount);
                recordList.add(record);

                if (collAmount <= 0) continue; //当金额小于等于0， 仅记录， 不上送分账金额为0的 分账账户信息。

                //判断该账号的分账类型是否为转账，如果为转账，执行转账业务
                if (config.getType() == 1) {
                    try {
                        //这里改成API请求
                        String recordJson = JSONObject.toJSONString(record);
                        byte[] data = recordJson.getBytes();
                        byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
                        String recordRsa = Base64Utils.encode(encodedData);

                        map.put("payOrderCashColl", recordRsa);
                        String sendMsg = XXPayUtil.mapToString(map);
                        String res = XXPayUtil.doPostQueryCmd(alipayConfig.getReqUrl() + "/alipaytrans", sendMsg);
                        JSONObject tranRes = JSONObject.parseObject(res);
                        boolean isSuccess = "success".equals(tranRes.getString("state"));
                        record.setStatus(isSuccess ? MchConstant.PUB_YES : MchConstant.PUB_NO);
                        record.setRemark(tranRes.getString("msg"));
                        //加入资金统计
                        result.put("result", "success");//这里的success没有起到判断是否真正成功的意义，只方便外部使用
                        continue;
                    } catch (Exception ex) {
                        result.put("result", "success");//这里的success没有起到判断是否真正成功的意义，只方便外部使用
                        record.setStatus(MchConstant.PUB_NO);
                        record.setRemark("支付宝单笔转账发生异常:" + ex.getMessage());
                        _log.error("支付宝单笔转账发生异常：", ex);
                        continue;
                    }
                }

                JSONObject singleAccount = new JSONObject();
                singleAccount.put("trans_in", config.getTransInUserId());


                singleAccount.put("amount", AmountUtil.convertCent2Dollar(collAmount + "")); //单位：元
                singleAccount.put("desc", payOrder.getPayOrderId() + "订单分账");
                jsonArray.add(singleAccount);
                sumCollAmount += collAmount;
            }

            result.put("records", recordList);
            if (jsonArray.size() > 0) {
                String merchantSplitJson = jsonArray.toJSONString();
                byte[] data = merchantSplitJson.getBytes();
                byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
                String merchantSplitRsa = Base64Utils.encode(encodedData);
                map.put("royalty_parameters", merchantSplitRsa);
                map.put("out_request_no", payOrder.getPayOrderId());
                map.put("trade_no", payOrder.getChannelOrderNo());
                map.put("passageAccountId", String.valueOf(payOrder.getPassageAccountId()));
                String sendMsg = XXPayUtil.mapToString(map);
                String res = XXPayUtil.doPostQueryCmd(alipayConfig.getReqUrl() + "/merchantSplit", sendMsg);
                result = JSONObject.parseObject(res);
                result.put("records", recordList);
            }

            return result;

        } catch (AlipayApiException e) {
            e.printStackTrace();
            _log.error("资金归集渠道异常：", e);
            throw new Exception(e.getMessage());
        } catch (Exception e) {
            _log.error("资金归集渠道异常：", e);
            e.printStackTrace();
            throw new Exception(e.getMessage());
        }
    }

    @Override
    public JSONObject relationbind(PayCashCollConfig payProduct) {
        JSONObject result = new JSONObject();
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("商家分账关系绑定,子账户ID:" + payProduct.getBelongPayAccountId() + "，分配支付账号：" + payProduct.getTransInUserId() + "，ID：" + payProduct.getTransInUserName());
        try {
            PayPassageAccount payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(payProduct.getBelongPayAccountId());
            if (payPassageAccount == null) {
                result.put("errDes", "绑定商家分账关系失败，未找到:" + payProduct.getBelongPayAccountId() + "通道子账户");
                stringBuffer.append("绑定商家分账关系失败，未找到:" + payProduct.getBelongPayAccountId() + "通道子账户");
                result.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return result;
            }

            AlipayConfig alipayConfig = new AlipayConfig(payPassageAccount.getParam());
            byte[] alipayConfigByte = payPassageAccount.getParam().getBytes();
            byte[] encodedDataByConfi = RSAUtils.encryptByPublicKey(alipayConfigByte, publicKey);
            String alipayConfigRsa = Base64Utils.encode(encodedDataByConfi);
            Map map = new HashMap();
            map.put("alipayConfig", alipayConfigRsa);
            String payProductJson = JSONObject.toJSONString(payProduct);
            byte[] data = payProductJson.getBytes();
            byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
            String payProductRsa = Base64Utils.encode(encodedData);
            map.put("payCashCollConfig", payProductRsa);
            String sendMsg = XXPayUtil.mapToString(map);
            String res = XXPayUtil.doPostQueryCmd(alipayConfig.getReqUrl() + "/relationbind", sendMsg);
            result = JSONObject.parseObject(res);
            return result;

        } catch (Exception ex) {
            result.put("errDes", "AlipayCashCollService=》绑定商家分账关系发生异常");
            stringBuffer.append("AlipayCashCollService=》绑定商家分账关系发生异常[" + ex.getMessage() + "]");
            result.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return result;
        } finally {
            _log.info(stringBuffer.toString());
        }
    }

    /**
     * 单笔转账
     *
     * @param record
     * @param account
     * @return
     */
    public JSONObject alipaytrans(PayOrderCashCollRecord record, PayPassageAccount account) {
        String logPrefix = "【支付宝转账】";
        String transOrderId = DateUtil.getRevTime();//请求唯一流水号
        JSONObject retObj = buildRetObj();
        try {
            AlipayConfig alipayConfig = new AlipayConfig(account.getParam());
            CertAlipayRequest certAlipayRequest = new CertAlipayRequest();
            certAlipayRequest.setServerUrl(this.aliPayUrl);  //gateway:支付宝网关（固定）https://openapi.alipay.com/gateway.do
            certAlipayRequest.setAppId(alipayConfig.getAppId());  //APPID 即创建应用后生成,详情见创建应用并获取 APPID
            certAlipayRequest.setPrivateKey(alipayConfig.getPrivateKey());  //开发者应用私钥，由开发者自己生成
            certAlipayRequest.setFormat(AlipayConfig.FORMAT);  //参数返回格式，只支持 json 格式
            certAlipayRequest.setCharset(AlipayConfig.CHARSET);  //请求和签名使用的字符编码格式，支持 GBK和 UTF-8

            AlipayFundTransToaccountTransferRequest request = new AlipayFundTransToaccountTransferRequest();
            AlipayFundTransToaccountTransferModel model = new AlipayFundTransToaccountTransferModel();
            AlipayClient alipayClient = null;
            try {

                String appCertPublicKeyPath = GetAppCertPublicKey(record.getPayOrderId(), alipayConfig.getAppId());
                String alipayCertPublicKeyPath = GetAlipayCertPublicKey(record.getPayOrderId(), alipayConfig.getAppId());
                String alipayRootCertPath = GetAlipayRootCert(record.getPayOrderId(), alipayConfig.getAppId());
                certAlipayRequest.setSignType(AlipayConfig.SIGNTYPE);  //商户生成签名字符串所使用的签名算法类型，目前支持 RSA2 和 RSA，推荐商家使用 RSA2。
                certAlipayRequest.setCertPath(appCertPublicKeyPath); //应用公钥证书路径（app_cert_path 文件绝对路径）
                certAlipayRequest.setAlipayPublicCertPath(alipayCertPublicKeyPath); //支付宝公钥证书文件路径（alipay_cert_path 文件绝对路径）
                certAlipayRequest.setRootCertPath(alipayRootCertPath);  //支付宝CA根证书文件路径（alipay_root_cert_path 文件绝对路径）
                alipayClient = new DefaultAlipayClient(certAlipayRequest);
            } catch (Exception ex) {
                retObj.put("msg", "单笔转账封装支付宝参数发生异常:" + ex.getMessage());
                retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
                return retObj;
            }

            Random random = new Random();
            model.setOutBizNo(DateUtil.getRevTime() + random.nextInt(10000));
            model.setPayeeType("ALIPAY_LOGONID");                            // 收款方账户类型
            model.setPayeeAccount(record.getTransInUserAccount());              // 收款方账户
            model.setAmount(AmountUtil.convertCent2Dollar(record.getTransInAmount().toString()));//分转元
            model.setPayerShowName("支付转账");
            model.setPayeeRealName(record.getTransInUserName());
            model.setRemark(record.getRemark());
            request.setBizModel(model);
            retObj.put("transOrderId", transOrderId);
            retObj.put("state", "fail");

            AlipayFundTransToaccountTransferResponse response = alipayClient.certificateExecute(request);
            if (response.isSuccess()) {
                retObj.put("state", "success");
                retObj.put("msg", "转账成功");
                retObj.put("channelOrderNo", response.getOrderId());
            } else {
                //出现业务错误
                _log.info("{}返回失败", logPrefix);
                _log.info("sub_code:{},sub_msg:{}", response.getSubCode(), response.getSubMsg());
                retObj.put("msg", response.getSubCode() + response.getSubMsg());
                retObj.put("channelErrCode", response.getSubCode());
            }

            retObj.put("msg", response.getSubMsg() + "|" + response.getMsg());

        } catch (AlipayApiException e) {
            _log.error(e, "");
            retObj.put("msg", e.getErrMsg());
            retObj = buildFailRetObj();
        }

        return retObj;
    }


    /**
     * 现金红包分发
     *
     * @param payOrder
     * @return
     * @throws Exception
     */
    @Override
    public JSONObject collRedEnvelope(PayOrder payOrder) throws Exception {
        JSONObject result = new JSONObject();
        result.put("result", "fail");
        try {
            PayPassageAccount account = rpcCommonService.rpcPayPassageAccountService.findById(payOrder.getPassageAccountId());
            if (account.getCashCollStatus() != MchConstant.PUB_YES) {
                result.put("msg", "未开启需资金归集服务");
                String msgSend = MessageFormat.format("预警消息->支付产品:{0}->下的通道子账户:{1}。未开启资金归集服务，请运营人员核查！订单号：{2}", payOrder.getProductId(), payOrder.getPassageAccountId(), payOrder.getPayOrderId());
                TelegramUtil.SendMsg(msgSend);
                return result;
            }


            //判断当前订单是否已经有分发红包的记录，如果有的话，表示已经执行了分发红包的mq，这里不再重复执行，留给运营在后台补发。
            List<PayOrderCashCollRecord> list = rpcCommonService.rpcPayOrderCashCollRecordService.selectByOrderId(payOrder.getPayOrderId());
            //Status  Type PayAccountId TransInUserIds
//            PayCashCollConfig selectCondition = new PayCashCollConfig();
//            selectCondition.setType(2);
            Map<String, Object> condtionMap = new HashMap<>();
            condtionMap.put("Status", String.valueOf(MchConstant.PUB_YES));
            condtionMap.put("Type", "2");
            if (list != null && list.size() > 0) {
                List<String> categoryTypeList = list.stream().map(e -> e.getTransInUserId()).collect(Collectors.toList());
                condtionMap.put("TransInUserIds", categoryTypeList);
            }

//            selectCondition.setStatus(MchConstant.PUB_YES); //仅查询开启状态
            if (account.getCashCollMode() == 1) { //继承
                condtionMap.put("PayAccountId", "0");
            } else {
//                selectCondition.setBelongPayAccountId(account.getId()); //查询子账户的特有配置账号
                condtionMap.put("PayAccountId", account.getId());
            }

            //获取系统配置=>现金红包拆分基础值
            SysConfig sysConfig = rpcCommonService.sysConfigService.findCode("redenvelope");
            if (sysConfig == null) {
                String msgSend = MessageFormat.format("预警消息->现金红包分发失败=>订单号:{0},失败原因:系统参数表未配置现金红包拆分的阈值。请联系技术配置！！！", payOrder.getPayOrderId());
                TelegramUtil.SendMsg(msgSend);
                result.put("msg", "系统参数表未配置现金红包拆分的阈值");
                return result;
            }


            BigDecimal threshold = new BigDecimal(sysConfig.getValue());
            //计算当前订单金额要拆分几个进行红包发放
            BigDecimal limit = new BigDecimal(payOrder.getAmount()).divide(new BigDecimal(100)).divide(threshold).setScale(0, BigDecimal.ROUND_HALF_UP);
            List<PayCashCollConfig> configList = rpcCommonService.rpcPayCashCollConfigService.select(0, limit.intValue(), condtionMap);
            if (configList == null || configList.isEmpty()) {
                String msgSend = MessageFormat.format("预警消息->现金红包分发失败=>订单号:{0},失败原因:现金红包领取人账号未配置，请运营人员进行配置！！！速度配置！！！", payOrder.getPayOrderId());
                TelegramUtil.SendMsg(msgSend);
                result.put("msg", "现金红包领取人账号未配置");
                return result;
            }

            //组装请求报文
            String aliPayConfigJson = getPayParam(payOrder);
            AlipayConfig alipayConfig = new AlipayConfig(aliPayConfigJson);
            byte[] alipayConfigByte = aliPayConfigJson.getBytes();
            byte[] encodedDataByConfif = RSAUtils.encryptByPublicKey(alipayConfigByte, publicKey);
            String alipayConfigRsa = Base64Utils.encode(encodedDataByConfif);
            Map map = new HashMap();
            map.put("alipayConfig", alipayConfigRsa);
            List<PayOrderCashCollRecord> recordList = new ArrayList<>(); //返回记录结果集
            for (int i = 0; i < configList.size(); i++) {
                Boolean flag = ChekCashUnpcaking(payOrder);//检测现金红包是否领取完毕
                if (flag) break;
                Long collAmount = CashUnpacking(payOrder);// 现金红包拆包,拿到当前需要分发的金额
                PayCashCollConfig config = configList.get(i);
                PayOrderCashCollRecord record = new PayOrderCashCollRecord();
                record.setPayOrderId(payOrder.getPayOrderId());
                record.setChannelOrderNo(payOrder.getChannelOrderNo());
                record.setRequestNo(payOrder.getPayOrderId());
                record.setTransInUserName(config.getTransInUserName());
                record.setTransInUserAccount(config.getTransInUserAccount());
                record.setTransInUserId(config.getTransInUserId());
                record.setType(config.getType());
                record.setPassageAccountId(String.valueOf(payOrder.getPassageAccountId()));
                record.setTransInPercentage(configList.get(i).getTransInPercentage());
                record.setTransInAmount(collAmount);
                try {
                    //现金红包拆分
                    String recordJson = JSONObject.toJSONString(record);
                    byte[] data = recordJson.getBytes();
                    byte[] encodedData = RSAUtils.encryptByPublicKey(data, publicKey);
                    String recordRsa = Base64Utils.encode(encodedData);
                    map.put("payOrderCashColl", recordRsa);
                    String sendMsg = XXPayUtil.mapToString(map);
                    //请求中转服务器的红包领取接口
                    String res = XXPayUtil.doPostQueryCmd(alipayConfig.getReqUrl() + "/collRedEnvelope", sendMsg);
                    JSONObject tranRes = JSONObject.parseObject(res);
                    boolean isSuccess = "success".equals(tranRes.getString("state"));
                    record.setStatus(isSuccess ? MchConstant.PUB_YES : MchConstant.PUB_NO);
                    record.setRemark(tranRes.getString("msg"));
                } catch (Exception ex) {
                    result.put("result", "success");//这里的success没有起到判断是否真正成功的意义，只方便外部使用
                    record.setStatus(MchConstant.PUB_NO);
                    record.setRemark("支付宝单笔转账发生异常:" + ex.getMessage());
                    _log.error("支付宝单笔转账发生异常：", ex);
                }

                rpcCommonService.rpcPayOrderCashCollRecordService.add(record);
                recordList.add(record);
            }

            result.put("result", "success");
            result.put("records", recordList);
            return result;

        } catch (AlipayApiException e) {
            _log.error("现金红包分发,发生异常：", getExceptionInfo(e));
            throw new Exception(e.getMessage());
        } catch (Exception e) {
            _log.error("现金红包分发,发生异常：", getExceptionInfo(e));
            throw new Exception(e.getMessage());
        }
    }
}
