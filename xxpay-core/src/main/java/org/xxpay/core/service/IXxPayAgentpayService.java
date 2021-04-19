package org.xxpay.core.service;

import com.alibaba.fastjson.JSONObject;
import org.xxpay.core.entity.MchAgentpayRecord;
import org.xxpay.core.entity.MchInfo;

import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 2018/5/29
 * @description: 代付接口
 */
public interface IXxPayAgentpayService {

    /**
     * 发起单笔代付申请
     * @param mchAgentpayRecord
     * @return
     */
    int applyAgentpay(MchInfo mchInfo, MchAgentpayRecord mchAgentpayRecord);

    /**
     * 发起批量代付申请
     * @param mchInfo
     * @param totalApplyNum      总笔数
     * @param totalApplyAmount   总申请金额
     * @param applyMchAgentpayRecordList     申请代付集合
     * @return
     */
    JSONObject batchApplyAgentpay(MchInfo mchInfo, Integer totalApplyNum, Long totalApplyAmount, List<MchAgentpayRecord> applyMchAgentpayRecordList);

}
