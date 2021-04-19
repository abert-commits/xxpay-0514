package org.xxpay.task.reconciliation.scheduled;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.util.MyLog;
import org.xxpay.core.entity.MchBalance;
import org.xxpay.core.entity.MchInfo;
import org.xxpay.task.common.service.RpcCommonService;


import java.util.*;

/**
 * 结余  定时生成
 */
@Component
public class BalanceScheduled {
    private static final MyLog _log = MyLog.getLog(BalanceScheduled.class);

    @Autowired
    private RpcCommonService rpcCommonService;


    /**
     * 执行结余 操作
     */
     @Scheduled(cron="0 0 0 * * ?")   // 每日凌晨执行
    //@Scheduled(cron="0 0/1 * * * ?") // 每分钟执行一次
    public void buildBalanceTask() {
        _log.info("结余数据 生成,开始...");
        //查询所有商户
        List<MchInfo> mchInfoList=rpcCommonService.rpcMchInfoService.select(0,1000,null);
        //循环根据商户查询 商户的余额 ,最后一单,充值金额, 转账金额,提现金额，提现笔数，提现手续费,昨天余额
        for (int i = 0; i < mchInfoList.size(); i++) {
            JSONObject map=new JSONObject();
            Date date=new Date();//取时间
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY,0);
            calendar.set(Calendar.MINUTE,0);
            calendar.set(Calendar.SECOND,0);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            calendar.set(Calendar.MILLISECOND,0);
            map.put("startDay",calendar.getTime());

            calendar.add(Calendar.DAY_OF_MONTH, 1);
            map.put("tomorrow",calendar.getTime());

            calendar.setTime(date);
            calendar.set(Calendar.HOUR_OF_DAY,23);
            calendar.set(Calendar.MINUTE,59);
            calendar.set(Calendar.SECOND,59);
            calendar.set(Calendar.MILLISECOND,999);
            calendar.add(Calendar.DAY_OF_MONTH, -1);
            map.put("lastDay",calendar.getTime());
            map.put("MchId",mchInfoList.get(i).getMchId());
            MchBalance mchBalance=rpcCommonService.rpcMchBalanceService.selectSynthesis(map);
            if(mchBalance!=null){
                //保存数据库
                mchBalance.setName(mchInfoList.get(i).getName());
                mchBalance.setCreateTime(new Date());
                rpcCommonService.rpcMchBalanceService.insertSelective(mchBalance);
            }
            System.out.println(map.toJSONString());
        }
    }

}
