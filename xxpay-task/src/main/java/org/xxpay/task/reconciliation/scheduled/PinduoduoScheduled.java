package org.xxpay.task.reconciliation.scheduled;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.xxpay.core.common.util.*;
import org.xxpay.core.entity.*;
import org.xxpay.task.common.service.RpcCommonService;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * 拼多多定时
 */
@Component
public class PinduoduoScheduled {
    private static final MyLog _log = MyLog.getLog(PinduoduoScheduled.class);

    @Autowired
    private RpcCommonService rpcCommonService;

    // 获取 下单账号 总数
    static int userTotal = 0;
    // 记录当前 取了第几个了
    static int userCurrent = 0;

    public static int getUser() {
        userCurrent += 1;
        if (userCurrent > userTotal) {
            return (userCurrent = 1);
        }
        return userCurrent;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    private Nginx getRedisNginx() {
        List<Nginx> nginxList = (List<Nginx>) redisTemplate.opsForValue().get(PDDUtils.NginxList);
        WeightUtil<Nginx> wt = new WeightUtil<Nginx>();
        if (nginxList == null || nginxList.size() < 1) {
            Nginx nginx = new Nginx();
            nginx.setStatus("1");
            nginxList = rpcCommonService.rpcINginxService.select(0, 50000, nginx);
            redisTemplate.opsForValue().set(PDDUtils.NginxList, nginxList);
            return wt.choose(nginxList);
        }
        return wt.choose(nginxList);
    }

    /**
     * 执行预下订单 操作
     */
    // @Scheduled(cron="0 0 1 * * ?")   // 每日10:45执行
    //@Scheduled(cron = "0/1 * * * * ?") // 每秒执行一次
    public void buildBalanceTask() {
        _log.info("预订单数据 生成,开始...");
        PinduoduoPreOrders preOrders = new PinduoduoPreOrders();
        preOrders.setStatus(0);
        preOrders.setEndTime(new Date());
        preOrders.setStartingTime(new Date());
        //查询所有未过期账号
        PinduoduoUser user = new PinduoduoUser();
        user.setStatus(true);
        user.setIs_expired(false);
        user.setIs_limit(false);
        userTotal = rpcCommonService.rpcIPinduoduoUserService.count(user);
        int userIndex = getUser();
        List<PinduoduoUser> user1 = rpcCommonService.rpcIPinduoduoUserService.select(userIndex - 1, 1, user);
        //查询所有没有结束 停止的 预下订单
        List<PinduoduoPreOrders> preOrdersList = rpcCommonService.rpcIPinduoduoPreOrderService.select(0, 5000, preOrders);
        for (int i = 0; i < preOrdersList.size(); i++) {
            // 判断是否该 下单了   上一次时间 加上频率 是否大于等于当前时间
            PinduoduoPreOrders preOrders1 = preOrdersList.get(i);
            if (preOrders1.getCompletionsNumber()!=null&&preOrders1.getTotalOrders()!=null) {
                if (preOrders1.getCompletionsNumber() >= preOrders1.getTotalOrders()) {
                    continue;
                }
            }

            PinduoduoUser $user = user1.get(0);
            Date lastTime = preOrders1.getLastTime();
            if (lastTime != null) {
                lastTime.setTime(lastTime.getTime() + (preOrders1.getFrequency() * 1000));
            }
            //如果该下单了  访问pdd生成订单
            if (lastTime == null || lastTime.getTime() <= new Date().getTime()) {
                System.out.println("进来了............................");

                //获取商品
                PinduoduoGoods goods = rpcCommonService.rpcIPinduoduoGoodsService.getGoods(preOrders1.getGoodsId().intValue());
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("address_id", $user.getAddress_id());

                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("order_amount", 1);
                jsonObject.put("attribute_fields", jsonObject1);
                JSONArray jsonArray = new JSONArray();
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("goods_id", goods.getGoods_id());
                jsonObject2.put("sku_id", goods.getSku_id());
                jsonObject2.put("sku_number", 1);
                jsonArray.add(jsonObject2);
                jsonObject.put("goods", jsonArray);

                jsonObject.put("group_id", goods.getGroup_id());
                jsonObject.put("pay_app_id", preOrders1.getPayCode());

                //{"address_id":18931203703,"goods":
                // [{"sku_id":45946053001,"sku_number":1,"goods_id":"2334309537"}],
                // "group_id":"3435150028","anti_content":"0aoAfaNdOOcYY9T8zatAd9CJtdCPDw4tGpTjccLpw_Vfh_Zn1-1nCYKGbEh38ZpNkL5ZNIUZTR5CdRCGRCt2nOTKQG4ojV-qHLl5X3mwCl9KC8Aoc114wIIYUIT2Piixj2njSFXM-LPTfWjTxEDei9a5XlLxXRBBKoFhNt1xHh81K1fU0tZG9Pi-eBJsUSUXFddX_Mrt47wpn_vUFEeiy0lkKRB6uQxp2OD2HekkytPlIr20ulon9O58zynVP5GBYo8fPSNx82cXvbMP_U3zZHQfHwQ4KsymtHCZexYTCC5BAsvK3wroSCWySOztASZPMtwC6Q4RZ8MTdE3WqI6K9SScRHSbjOxTNQ7GnAauuWjunOyWfeqSuAk6md9QP9HDTGJwWt_CFATUKA3e-nQYy8uaEeIjQDlLzW6n06BJ0bXfe6Q97CnXJEX6fSVm_Qurvq0HCXqrVPhpM4UfkJVT79Qq1QBTIKFPnY3hfb0CX98zfBh9LBU59z1Kz4YjkArh-35ZqDkb7gVkPoHwvlYnF1-lashSqX_ea_uZPSC98vIx-tyQLCYMugzjHyqw53wrfVz1cweXzxYA7oJ6Q_8gkXqCCbcLIAsw1TS1A41tmWydHULddyDoEKcAFVaGpwE_T4u2YFTYGina5TT93SlkgjLVDC1_kB3mZrpDXnscpMlJxncemgJQLYAJj6PQWrqnrhFGB6bJdmj-fRSM5UugvFuvLWvAx60EbWQpiArbYrh23Lp8xNLEDWDOxilLlFS8o6hEfBrikrxvouW4wPrC7MNY0dm4LHAuUqGrX9GBnpQAMpm6JLHJ7-aFbyQcCup_CNjfb8lDYGsLFd"
                // ,"pay_app_id":38,"is_app":"0","version":1,"page_id":"10004_1587628749310_nc3h5ljk79","duoduo_type":0,"biz_type":0,"attribute_fields":
                // {"create_order_token":"88adf932bfb4530d785af707410d3d2b","create_order_msg":"RnLUAzkLJm0+k9RNM1mpFAhTSvGKvBssdGUsKjLFMCs1yjlnK1at6bJKBFrClZF3CT74ZqUx/WoIMfcJXUkU+ABdOHGQ/87egddF9LunRRzxGFXX2V9uPIPIGOBekXtnxSJRWmttH/Rish5gIwkZM+wVA0L3eXVIS5tun+iLFjaQxFV56xhaG8V8WtxCV82Yc/VEKZrEPe6n4n6OqmZ+OdXXTEs7JHtJaapaoEpg2K69IL1CSllx4PzhX1pib29ERJU24MiR8s4zc6poYfmGncHJMFSRuF/1WV5/Qb2Ys/LpSDsxuuWIws2mUOd+f0mBiwMXFR6EX53F1Fv29CddK2FrHAyF9oRW2WRhPP4bViE0DHhnDL2KHAaTVDe1gkS338TPr7bgr0+oaZtoGcaBNYLkOfHwG5PRUO24DyZHwNHVja/rXtFM400xPI+ypgp9qraNixSa1P32DajoL9EQJkyA9qk2JfR/2IlO8ir5w/5fRtAsEQtneZkKzuvPgd5qFhIxWsbcNOln/yw6OteyrOnIpQnhS9LktfywZJuSSUSF3ijw8ipP5gGThqBIFPqW_v1","page_from":0,"create_order_check":"afFJVnFjcg-r1hSSNz3FFrEQIm-8iN9jAU341PugbaDjYiST78fM0IZSA2IJ7yj7"
                // ,"original_front_env":0,"current_front_env":1},"source_channel":"0","source_type":0}
                Nginx nginx = getRedisNginx();
                JSONObject orderJson = PDDUtils.orderUrl($user.getUid(), $user.getAccess_token(), $user.getUin(), jsonObject.toJSONString(), nginx.getNginxIP(), nginx.getNginxPort());
                PinduoduoOrders orders = new PinduoduoOrders();
                orders.setOrderSn(orderJson.getString("order_sn"));
                orders.setFpId(orderJson.getString("fp_id"));
                orders.setTotal(orderJson.getInteger("order_amount"));
                orders.setStatus(0);
                orders.setStoresId(goods.getStores_id());
                orders.setPhone($user.getPhone());
                orders.setCtime(new Date());
                orders.setUserId($user.getId());
                orders.setIsPay(false);
                orders.setPayType(Byte.valueOf(preOrders1.getPayCode()));
                orders.setgId(goods.getId());
                rpcCommonService.rpcIPinduoduoOrderService.add(orders);
                preOrders1.setLastTime(new Date());
                long completionsNumber = preOrders1.getCompletionsNumber() == null ? 0 : preOrders1.getCompletionsNumber();
                preOrders1.setCompletionsNumber(completionsNumber + 1);
                rpcCommonService.rpcIPinduoduoPreOrderService.update(preOrders1);
            }

        }
    }

    /**
     * 查询订单状态
     */
    //@Scheduled(fixedDelay=5000) // 每5秒执行一次
    public void getAndUpdateOrderByOrderSN() {
        _log.info("查询5分钟内的订单数据,开始...");
        //查询五分钟以内的 待支付的订单
        PinduoduoOrders orders = new PinduoduoOrders();
        orders.setStatus(0);
        long mtime=new Date().getTime();
        mtime=mtime-5*1000*60;
        Date mDate=new Date();
        mDate.setTime(mtime);
        orders.setMtime(mDate);
        List<PinduoduoOrders> ordersList=rpcCommonService.rpcIPinduoduoOrderService.getOrdersByStatus(orders);



    }
}
