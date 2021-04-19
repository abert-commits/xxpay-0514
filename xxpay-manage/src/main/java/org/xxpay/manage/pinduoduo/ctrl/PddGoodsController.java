package org.xxpay.manage.pinduoduo.ctrl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
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
import org.xxpay.core.common.util.PDDUtils;
import org.xxpay.core.common.util.WeightUtil;
import org.xxpay.core.entity.*;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/pdd/goods")
public class PddGoodsController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    @Autowired
    private RedisTemplate redisTemplate;


    private Nginx getRedisNginx(){
        List<Nginx> nginxList = (List<Nginx>) redisTemplate.opsForValue().get(PDDUtils.NginxList);
        WeightUtil<Nginx> wt = new WeightUtil<Nginx>();
        if(nginxList==null||nginxList.size()<1){
            Nginx nginx=new Nginx();
            nginx.setStatus("1");
            nginxList=rpcCommonService.rpcINginxService.select(0,50000,nginx);
            redisTemplate.opsForValue().set(PDDUtils.NginxList,nginxList);
            return wt.choose(nginxList);
        }
        return wt.choose(nginxList);
    }

    /**
     * 商品列表
     *
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PinduoduoGoods goods = getObject(param, PinduoduoGoods.class);
        int count = rpcCommonService.rpcIPinduoduoGoodsService.count(goods);
        if (count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<PinduoduoGoods> goodsList = rpcCommonService.rpcIPinduoduoGoodsService.select((getPageIndex(param) - 1) * getPageSize(param),
                getPageSize(param), goods);
        //获取全部 店铺表
        List<PinduoduoStores> storesList = rpcCommonService.rpcIPinduoduoStoresService.select(0, 5000000, null);

        goodsList.stream().forEach(g -> {
            storesList.stream().forEach(s -> {
                if (g.getStores_id() == s.getId()) {
                    g.setStores_name(s.getName());
                }
            });
        });

        return ResponseEntity.ok(XxPayPageRes.buildSuccess(goodsList, count));
    }


    @RequestMapping("/update")
    @ResponseBody
    @MethodLog(remark = "修改商品信息")
    public ResponseEntity<?> update(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PinduoduoGoods goods = getObject(param, PinduoduoGoods.class);
        int count = rpcCommonService.rpcIPinduoduoGoodsService.update(goods);
        if (count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }


    @RequestMapping("/delete")
    @ResponseBody
    @MethodLog(remark = "删除商品信息")
    public ResponseEntity<?> delete(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PinduoduoGoods goods = getObject(param, PinduoduoGoods.class);
        int count = rpcCommonService.rpcIPinduoduoGoodsService.delete(goods);
        if (count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    @RequestMapping("/add")
    @ResponseBody
    @MethodLog(remark = "新增商品信息")
    public ResponseEntity<?> add(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String phone = param.getString("phone");
        String goods_url = param.getString("goods_url");
        Integer stores_id = param.getInteger("stores_id");
        PinduoduoUser user = new PinduoduoUser();
        user.setPhone(phone);
        List<PinduoduoUser> users = rpcCommonService.rpcIPinduoduoUserService.selectUser(user);
        if (users.size() == 0 || StringUtils.isBlank(users.get(0).getAccess_token())) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_UNKNOWN_ERROR, "该手机号未获取到 账号"));
        }
        Nginx nginx=getRedisNginx();
        JSONObject res = PDDUtils.getGoodsUrl(goods_url, users.get(0).getAccess_token(),nginx.getNginxIP(),nginx.getNginxPort());
        JSONObject resGoods = res.getJSONObject("store").getJSONObject("initDataObj").getJSONObject("goods");
        JSONObject groupType = (JSONObject) resGoods.getJSONArray("groupTypes").get(0);
        JSONArray jsonArray = resGoods.getJSONArray("skus");
        List<PinduoduoGoods> goodsList = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            PinduoduoGoods goods = new PinduoduoGoods();
            goods.setStores_id(stores_id);
            goods.setGoods_url(goods_url);
            goods.setGroup_id(groupType.getLong("groupID"));
            goods.setGoods_id(resGoods.getLong("goodsID"));
            goods.setGoods_name(resGoods.getString("goodsName"));
            JSONObject sku = (JSONObject) jsonArray.get(i);
            goods.setSku_id(sku.getLong("skuID"));
            goods.setSku_id(sku.getLong("skuID"));
            goods.setNormal_price(Integer.parseInt(AmountUtil.convertDollar2Cent(sku.getString("normalPrice"))));
            goods.setError_count(0);
            goods.setIs_store_limit(false);
            goods.setStatus(true);
            goods.setCtime(new Date());
            String s = String.valueOf(System.currentTimeMillis());
            goods.setLast_use_time(Integer.parseInt(s.substring(0, s.length() - 3)));
            goods.setIs_upper(true);
            goodsList.add(goods);
        }
        rpcCommonService.rpcIPinduoduoGoodsService.adds(goodsList);
        return ResponseEntity.ok(BizResponse.buildSuccess());

    }


    @RequestMapping("/get")
    @ResponseBody
    @MethodLog(remark = "获取商品信息")
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String goods_id = getStringRequired(param, "goods_id");
        PayInterface payInterface = rpcCommonService.rpcPayInterfaceService.findByCode(goods_id);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(payInterface));
    }
}
