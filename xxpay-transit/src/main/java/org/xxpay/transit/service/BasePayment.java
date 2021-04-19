package org.xxpay.transit.service;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GetObjectRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.util.FileUtils;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.AlipayConfig;
import org.xxpay.core.entity.PayOrder;
import org.xxpay.core.entity.PayPassageAccount;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @author: dingzhiwei
 * @date: 17/12/24
 * @description:
 */
@Component
public abstract class BasePayment {

    private static final MyLog _log = MyLog.getLog(BasePayment.class);
    @Autowired
    public PayConfig payConfig;
    @Autowired
    private OOSConfig oosConfig;



    public abstract String getChannelName();

    public String getOrderId(PayOrder payOrder) {
        return null;
    }

    public Long getAmount(PayOrder payOrder) {
        return null;
    }

    public JSONObject pay(PayOrder payOrder) {
        return null;
    }

    public JSONObject query(PayOrder payOrder) {
        return null;
    }

    public JSONObject close(PayOrder payOrder) {
        return null;
    }

//    /**
//     * 获取三方支付配置信息
//     * 如果是平台账户,则使用平台对应的配置,否则使用商户自己配置的渠道
//     * @param payOrder
//     * @return
//     */
//    public String getPayParam(PayOrder payOrder) {
//        String payParam = "";
//        PayPassageAccount payPassageAccount = rpcCommonService.rpcPayPassageAccountService.findById(payOrder.getPassageAccountId());
//        if(payPassageAccount != null && payPassageAccount.getStatus() == MchConstant.PUB_YES) {
//            payParam = payPassageAccount.getParam();
//        }
//        if(StringUtils.isBlank(payParam)) {
//            throw new ServiceException(RetEnum.RET_MGR_PAY_PASSAGE_ACCOUNT_NOT_EXIST);
//        }
//        return payParam;
//    }


    protected JSONObject buildRetObj() {
        JSONObject retObj = new JSONObject();
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        return retObj;
    }

    protected JSONObject buildFailRetObj() {
        JSONObject retObj = new JSONObject();
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
        return retObj;
    }

    protected JSONObject buildRetObj(String retValue, String retMsg) {
        JSONObject retObj = new JSONObject();
        retObj.put(PayConstant.RETURN_PARAM_RETCODE, retValue);
        retObj.put(PayConstant.RETURN_PARAM_RETMSG, retMsg);
        return retObj;
    }

    /**
     * 解析支付宝回调请求的数据
     *
     * @param request
     * @return
     */
    public Map buildNotifyData(HttpServletRequest request) {
        //获取支付宝POST过来反馈信息
        Map<String, String> params = new HashMap<String, String>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
            params.put(name, valueStr);
        }
        return params;
    }

    /**
     * 获取appCertPublicKey证书路径
     *
     * @param payOrderId
     * @param appId
     * @return
     */
    protected String GetAppCertPublicKey(String payOrderId, String appId) {
        // 创建OSSClient实例。
        OSS ossClient = null;

        try {
            String prentPath = this.getClass().getResource("/").getPath();
            String filePath = prentPath + "certfile/" + appId + "_appcertpublickey.crt";
            filePath = URLDecoder.decode(filePath, "UTF-8");
            _log.info("GetAppCertPublicKey地址：" + filePath);
            File file = new File(filePath);
            _log.info("===========================1111111：");

            //如果文件存在，直接返回
            if (file.exists()) {
                return filePath;
            }
            _log.info("===========================2222222：");

            org.xxpay.core.entity.AlipayConfig condtion = new org.xxpay.core.entity.AlipayConfig();
            condtion.setAPPID(appId);
            condtion.setStatus(1);
            _log.info("===========================33333333："+condtion);

            //不存在的话，查询oos
            _log.info("oos appCertPublickeyBytes： "+appId + "_appcertpublickey.crt" );

            ossClient= new OSSClientBuilder().build(oosConfig.getEndpoint(), oosConfig.getAccessKeyId(), oosConfig.getAccessKeySecret());
            // 下载OSS文件到本地文件。如果指定的本地文件存在会覆盖，不存在则新建。
            ossClient.getObject(new GetObjectRequest(oosConfig.getBucketName(),  appId + "_appcertpublickey.crt"),file);

         /*   //不存在的话，查询数据库
            _log.info("===========================4444444444："+rpcCommonService);

            org.xxpay.core.entity.AlipayConfig model = rpcCommonService.rpcAliPayConfigService.findAliPayConfig(condtion);
            _log.info("===========================55555555555："+model);

            if (model != null && StringUtils.isNotBlank(model.getAppCertPublickeyFileName())) {

                byte[] appCertPublickeyBytes = rpcCommonService.rpcAliPayConfigService.selectById(model.getId()).getAppCertPublickey();
                _log.info("===========================66666666666："+appCertPublickeyBytes);
                FileUtils.byteToFile(appCertPublickeyBytes, filePath);
            }else {
                _log.info("GetAppCertPublicKey地址：未获取到数据库证书信息" );
            }
            */

            return filePath;

        } catch (Exception ex) {
            ex.printStackTrace();
            _log.info("获取AppCertPublicKey发生异常,订单号:" + payOrderId + ",异常信息：" + ex.getMessage());
            return "error:获取appCertPublicKey发生异常";
        }finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }


    /**
     * 获取alipayCertPublicKey证书路径
     *
     * @param payOrderId
     * @param appId
     * @return
     */
    protected String GetAlipayCertPublicKey(String payOrderId, String appId) {
        // 创建OSSClient实例。
        OSS ossClient = null;

        try {

            String prentPath = this.getClass().getResource("/").getPath();
            String filePath = prentPath + "certfile/" + appId + "_alipaycertpublickey.crt";
            _log.info("GetAlipayCertPublicKey地址：" + filePath);
            filePath = URLDecoder.decode(filePath, "UTF-8");
            File file = new File(filePath);
            //如果文件存在，直接返回
            if (file.exists()) {
                return filePath;
            }

            org.xxpay.core.entity.AlipayConfig condtion = new org.xxpay.core.entity.AlipayConfig();
            condtion.setAPPID(appId);
            condtion.setStatus(1);
            //不存在的话，查询oos
            _log.info("oos appCertPublicKey名字： "+appId + "_alipaycertpublickey.crt" );

            ossClient= new OSSClientBuilder().build(oosConfig.getEndpoint(), oosConfig.getAccessKeyId(), oosConfig.getAccessKeySecret());
            // 下载OSS文件到本地文件。如果指定的本地文件存在会覆盖，不存在则新建。
            ossClient.getObject(new GetObjectRequest(oosConfig.getBucketName(),  appId + "_alipaycertpublickey.crt"),file);

           /* //不存在的话，查询数据库
            AlipayConfig model = rpcCommonService.rpcAliPayConfigService.findAliPayConfig(condtion);
            if (model != null && StringUtils.isNotBlank(model.getAliPayCertPublickeyFileName())) {
                byte[] aliPayCerPubliceKeyBytes = rpcCommonService.rpcAliPayConfigService.selectById(model.getId()).getAliPayCertPublickey();
                FileUtils.byteToFile(aliPayCerPubliceKeyBytes, filePath);
            }else {
                _log.info("appCertPublicKey地址：未获取到数据库证书信息" );
            }
*/

            return filePath;
        } catch (Exception ex) {
            ex.printStackTrace();
            _log.info("获取appCertPublicKey发生异常,订单号:" + payOrderId + ",异常信息：" + ex.getMessage());
            return "error:获取appCertPublicKey发生异常";
        }finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }


    /**
     * 获取AlipayRootCert证书路径
     *
     * @param payOrderId
     * @param appId
     * @return
     */
    protected String GetAlipayRootCert(String payOrderId, String appId) {
        try {
            String prentPath = this.getClass().getResource("/").getPath();
            String myPath = prentPath + "certfile/alipayrootcert.crt";
            myPath = URLDecoder.decode(myPath, "UTF-8");
            _log.info("GetAlipayRootCert地址：" + myPath);
            return myPath;
        } catch (Exception ex) {
            ex.printStackTrace();
            _log.info("获取appCertPublicKey发生异常,订单号:" + payOrderId + ",异常信息：" + ex.getMessage());
            return "error:获取appCertPublicKey发生异常";
        }
    }
}
