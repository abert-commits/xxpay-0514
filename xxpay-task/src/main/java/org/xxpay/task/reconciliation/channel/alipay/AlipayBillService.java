package org.xxpay.task.reconciliation.channel.alipay;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayDataDataserviceBillDownloadurlQueryModel;
import com.alipay.api.request.AlipayDataDataserviceBillDownloadurlQueryRequest;
import com.alipay.api.response.AlipayDataDataserviceBillDownloadurlQueryResponse;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.constant.PayConstant;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.FileUtils;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.CheckBatch;
import org.xxpay.task.reconciliation.channel.BaseBill;
import org.xxpay.task.reconciliation.entity.ReconciliationEntity;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 18/1/18
 * @description:
 */
@Service
public class AlipayBillService extends BaseBill {

    private static final MyLog _log = MyLog.getLog(AlipayBillService.class);

    @Autowired
    private AlipayProperties alipayProperties;

    @Override
    public String getChannelName() {
        return null;
    }

    @Override
    public JSONObject downloadBill(JSONObject param, CheckBatch batch) {
        JSONObject map = new JSONObject();
        map.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_SUCCESS);
        String billDate = DateUtil.date2Str(batch.getBillDate(), DateUtil.FORMAT_YYYY_MM_DD);
        String payParam = param.getString("payParam");
        AlipayConfig alipayConfig = new AlipayConfig(payParam);
        AlipayClient client = new DefaultAlipayClient(alipayConfig.getReqUrl(), alipayConfig.getAppId(),
                alipayConfig.getPrivateKey(), AlipayConfig.FORMAT, AlipayConfig.CHARSET,
                alipayConfig.getAlipayPublicKey(), AlipayConfig.SIGNTYPE);
        AlipayDataDataserviceBillDownloadurlQueryRequest request = new AlipayDataDataserviceBillDownloadurlQueryRequest();//??????API?????????request???
        AlipayDataDataserviceBillDownloadurlQueryModel model = new AlipayDataDataserviceBillDownloadurlQueryModel();
        model.setBillDate(billDate);
        model.setBillType("trade");
        request.setBizModel(model);
        AlipayDataDataserviceBillDownloadurlQueryResponse response = null;//??????alipayClient??????API??????????????????response???
        try {
            response = client.execute(request);
        } catch (AlipayApiException e) {
            _log.error(e, "");
            map.put("errDes", "?????????????????????");
            map.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return map;
        }
        String billDownloadUrl = response.getBillDownloadUrl();
        _log.info("?????????????????????????????????:{}", billDownloadUrl);
        if(StringUtils.isBlank(billDownloadUrl)) {
            _log.info("?????????????????????URL??????");
            map.put("errDes", "?????????????????????URL??????");
            map.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
            return map;
        }
        // ????????????
        String filePath = alipayProperties.getBillPath() + billDate + ".zip";
        try {
            File file = FileUtils.saveFile(billDownloadUrl, new File(filePath));
            List<String> filePathList = FileUtils.unZipFiles(file, alipayProperties.getBillPath() + billDate + File.separator);
            System.out.println(filePathList);
            map.put("bill", bill2Reconciliation(filePathList, batch));
        } catch (IOException e) {
            _log.error(e, "");
            map.put("errDes", "????????????????????????");
            map.put(PayConstant.RETURN_PARAM_RETCODE, PayConstant.RETURN_VALUE_FAIL);
        }
        return map;
    }

    /**
     * ?????????????????????????????????????????????
     * @param filePathList
     * @return
     */
    List<ReconciliationEntity> bill2Reconciliation(List<String> filePathList, CheckBatch batch) {
        List<ReconciliationEntity> reconciliationEntityList = new LinkedList<>();
        if(CollectionUtils.isEmpty(filePathList)) return reconciliationEntityList;
        for(String filePath : filePathList) {
            if(filePath.contains("??????")) {
                // ??????????????????
                try {
                    List<List<String>> csvList = FileUtils.readCSVFile(filePath, "GBK");
                    for(int i=0; i < csvList.size(); i++) {
                        List<String> strList = csvList.get(i);
                        if(i > 4 && strList.size() > 10) {
                            if(strList.get(0).contains("??????")) {

                                // ??????????????????
                                batch.setBankTradeCount(Integer.parseInt(strList.get(2).trim()));
                                batch.setBankTradeAmount(new BigDecimal(strList.get(4).trim()).multiply(new BigDecimal(100)).longValue());
                                batch.setBankRefundAmount(null);
                                batch.setBankFee(new BigDecimal(strList.get(9).trim()).multiply(new BigDecimal(100)).longValue());

                                break;
                            }
                        }
                    }
                } catch (IOException e) {
                    _log.error(e, "");
                }
            }else {
                // ??????????????????
                try {
                    List<List<String>> csvList = FileUtils.readCSVFile(filePath, "GBK");
                    for(int i=0; i < csvList.size(); i++) {
                        List<String> strList = csvList.get(i);
                        if(i > 4 && strList.size() > 20) {
                            //
                            ReconciliationEntity entity = new ReconciliationEntity();
                            // ??????????????????
                            entity.setOrderTime(DateUtil.str2date(strList.get(4).trim()));
                            // ???????????????
                            entity.setBankTrxNo(strList.get(0).trim());
                            entity.setBankOrderNo(strList.get(0).trim());
                            // ??????????????????(???????????????success)
                            entity.setBankTradeStatus("success");
                            // ??????????????????(?????????)
                            entity.setBankAmount(new BigDecimal(strList.get(11).trim()).multiply(new BigDecimal(100)).longValue());
                            // ???????????????(?????????)
                            entity.setBankFee(new BigDecimal(strList.get(22).trim()).multiply(new BigDecimal(100)).longValue());
                            reconciliationEntityList.add(entity);
                        }
                    }
                } catch (IOException e) {
                    _log.error(e, "");
                }
            }
        }
        return reconciliationEntityList;
    }


}
