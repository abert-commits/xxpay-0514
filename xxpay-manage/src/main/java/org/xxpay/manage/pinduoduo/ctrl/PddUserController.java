package org.xxpay.manage.pinduoduo.ctrl;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.annotation.MethodLog;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.BizResponse;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.PDDUtils;
import org.xxpay.core.common.util.RandomName;
import org.xxpay.core.common.util.WeightUtil;
import org.xxpay.core.entity.Nginx;
import org.xxpay.core.entity.PinduoduoAddress;
import org.xxpay.core.entity.PinduoduoOrders;
import org.xxpay.core.entity.PinduoduoUser;
import org.xxpay.manage.common.ctrl.BaseController;
import org.xxpay.manage.common.service.RpcCommonService;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping(Constant.MGR_CONTROLLER_ROOT_PATH + "/pdd/user")
public class PddUserController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;
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

    @RequestMapping("/add")
    @ResponseBody
    @MethodLog(remark = "新增下单账号信息")
    public ResponseEntity<?> add(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String phone = getString(param, "phone");
        String code = getString(param, "code");
        // 模拟登录拼多多
        Nginx nginx = getRedisNginx();
        String result = PDDUtils.login(phone, code, nginx.getNginxIP(), nginx.getNginxPort());
        JSONObject res = JSONObject.parseObject(result);
        //手机或者验证码不对
        if (res.get("error_msg") != null) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_PDD_CODE_ERROR));
        }
        PinduoduoUser user = new PinduoduoUser();
        user.setPhone(phone);
        user.setAccess_token(res.getString("access_token"));
        user.setAcid(res.getString("acid"));
        user.setUid(res.getLong("uid"));
        user.setUin(res.getString("uin"));
        user.setStatus(true);
        user.setIs_expired(false);
        user.setExpired_limit_noaddr("<span style=\"color:green\">正常</span>");
        user.setCtime(new Date());
        int count = rpcCommonService.rpcIPinduoduoUserService.add(user);
        if (count == 1) return ResponseEntity.ok(BizResponse.buildSuccess());
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    @RequestMapping("/getAddressTpl")
    @ResponseBody
    @MethodLog(remark = "获取地址模板")
    public ResponseEntity<?> getAddressTpl(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String phone = getString(param, "phone");
        Integer region_id = getInteger(param, "region_id");

        PinduoduoUser user = new PinduoduoUser();
        user.setPhone(phone);

        List<PinduoduoUser> users = rpcCommonService.rpcIPinduoduoUserService.selectUser(user);
        Nginx nginx = getRedisNginx();

        JSONObject result = PDDUtils.getAddressTpl(region_id, users.get(0).getUid(), users.get(0).getAccess_token(), nginx.getNginxIP(), nginx.getNginxPort());
        return ResponseEntity.ok(XxPayResponse.buildSuccess(result));
    }

    @RequestMapping("/saveAddress")
    @ResponseBody
    @MethodLog(remark = "保存地址模板")
    public ResponseEntity<?> saveAddress(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String phone = getString(param, "phone");

        String address_name = param.getString("address_name");
        Short address_province = param.getShort("address_province");
        Short address_city = param.getShort("address_city");
        Short address_district = param.getShort("address_district");
        String address_concret = param.getString("address_concret");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", address_name);
        jsonObject.put("mobile", phone);
        jsonObject.put("address", address_concret);
        jsonObject.put("district_id", address_district);
        jsonObject.put("city_id", address_city);
        jsonObject.put("province_id", address_province);
        jsonObject.put("is_default", 1);

        PinduoduoUser user = new PinduoduoUser();
        user.setPhone(phone);
        List<PinduoduoUser> users = rpcCommonService.rpcIPinduoduoUserService.selectUser(user);
        Nginx nginx = getRedisNginx();

        JSONObject result = PDDUtils.saveAddress(users.get(0).getUid(), users.get(0).getAccess_token(), jsonObject.toJSONString(), nginx.getNginxIP(), nginx.getNginxPort());

        PinduoduoAddress address = new PinduoduoAddress();
        address.setAddress_city(address_city);
        address.setAddress_province(address_province);
        address.setAddress_district(address_district);
        address.setAddress_concret(address_concret);
        user.setAddress_id(result.getLong("default_id"));
        int count = rpcCommonService.rpcIPinduoduoUserService.addAddress(address, user);

        if (count == 1) return ResponseEntity.ok(XxPayResponse.buildSuccess(result));
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }

    /**
     * 列表
     *
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        PinduoduoUser user = new PinduoduoUser();
        if (!StringUtils.isBlank(param.getString("phone"))) {
            String phone = getString(param, "phone");
            user.setPhone(phone);
        }
        if (!StringUtils.isBlank(param.getString("is_limit"))) {
            String is_limit = param.getString("is_limit");
            user.setIs_limit(Boolean.valueOf(is_limit));
        }
        if (!StringUtils.isBlank(param.getString("is_expired"))) {
            String is_expired = param.getString("is_expired");
            user.setIs_expired(Boolean.valueOf(is_expired));
        }
        int count = rpcCommonService.rpcIPinduoduoUserService.count(user);

        if (count == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());
        List<PinduoduoUser> refundUserList = rpcCommonService.rpcIPinduoduoUserService.select((getPageIndex(param) - 1) * getPageSize(param),
                getPageSize(param), user);
        return ResponseEntity.ok(XxPayPageRes.buildSuccess(refundUserList, count));
    }


    @RequestMapping("/setUserStatus")
    @ResponseBody
    @MethodLog(remark = "获取地址模板")
    public ResponseEntity<?> setUserStatus(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer id = getInteger(param, "id");
        boolean status = param.getBoolean("val");

        PinduoduoUser user = new PinduoduoUser();
        user.setId(id);
        user.setStatus(status);
        int count = rpcCommonService.rpcIPinduoduoUserService.update(user);

        if (count == 1) return ResponseEntity.ok(XxPayResponse.buildSuccess(user));
        return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
    }


    @RequestMapping("/userImport")
    @ResponseBody
    @MethodLog(remark = "导入账号")
    public ResponseEntity<?> userImport(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        String content = getString(param, "content");
        String[] res = content.split("\n");
        if (res.length < 1) {
            return ResponseEntity.ok(BizResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
        }
        //成功个数
        int success = 0;
        //失败个数
        int err = 0;
        //失败 手机号
        StringBuilder errString = new StringBuilder();

        //查询全部地址
        List<PinduoduoAddress> address = rpcCommonService.rpcIPinduoduoUserService.getAddress();
        int j = address.size() - 1;
        for (int i = 0; i < res.length; i++) {
            if (j == 1) {
                //用完重新拿
                j = address.size() - 1;
            }
            String[] arr = res[i].split(",");
            PinduoduoUser user = new PinduoduoUser();
            user.setPhone(arr[0]);
            user.setUid(Long.valueOf(arr[1]));
            user.setAccess_token(arr[2]);
            user.setAdmin_uid(0);
            user.setCtime(new Date());
            //给当前账号 添加默认地址
            PinduoduoAddress address1 = address.get(j);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", RandomName.randomName(true, 2));
            jsonObject.put("mobile", arr[0]);
            jsonObject.put("address", address1.getAddress_concret());
            jsonObject.put("district_id", address1.getAddress_district());
            jsonObject.put("city_id", address1.getAddress_city());
            jsonObject.put("province_id", address1.getAddress_province());
            jsonObject.put("is_default", 1);
            Nginx nginx = getRedisNginx();

            String result = PDDUtils.saveAddress1(Long.valueOf(arr[1]), arr[2], jsonObject.toJSONString(), nginx.getNginxIP(), nginx.getNginxPort());
            if (result.contains("error")) {
                err++;
                errString.append("手机号: " + arr[0] + " 新增失败  \n ");
            } else {
                JSONObject jsonAddress = JSONObject.parseObject(result);

                user.setAddress_id(jsonAddress.getLong("default_id"));
                user.setStatus(true);
                user.setIs_expired(false);
                int count = rpcCommonService.rpcIPinduoduoUserService.add(user);
                if (count > 0) {
                    success++;
                    //新增成功
                    j--;
                }
            }
        }
        JSONObject res1 = new JSONObject();
        res1.put("success", success);
        res1.put("err", err);
        res1.put("errString", errString);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(res1));
    }


    @RequestMapping("/verification")
    @ResponseBody
    @MethodLog(remark = "验证账号token 是否过期")
    public ResponseEntity<?> verification(HttpServletRequest request) {
        //查询所有未过期账号
        PinduoduoUser user = new PinduoduoUser();
        user.setStatus(true);
        user.setIs_expired(false);
        user.setIs_limit(false);
        List<PinduoduoUser> users = rpcCommonService.rpcIPinduoduoUserService.selectUser(user);
        int count = 0;

        //查询账号是否过期
        for (int i = 0; i < users.size(); i++) {
            PinduoduoUser user1 = users.get(i);
            //创建Random类对象
            Random random = new Random();
            //产生随机数
            int number = random.nextInt(3) + 1;
            Nginx nginx = getRedisNginx();

            boolean resBoolean = false;
            switch (number) {
                case 1:
                    JSONObject res = PDDUtils.orderListUrl(user1.getAccess_token(), user1.getUid(), nginx.getNginxIP(), nginx.getNginxPort());
                    if (res.get("error_code") == null) {
                        resBoolean = true;
                    }
                    break;
                case 2:
                    JSONObject res1 = PDDUtils.recOrderList(user1.getAccess_token(), user1.getUid(), nginx.getNginxIP(), nginx.getNginxPort());
                    if (res1.get("error_code") == null) {
                        resBoolean = true;
                    }
                    break;
                default:
                    String html = PDDUtils.addressesList(user1.getAccess_token(), user1.getUid(), user1.getUin(), nginx.getNginxIP(), nginx.getNginxPort());
                    if (html.contains("设为默认")) {
                        resBoolean = true;
                    }
                    break;
            }
            // 如果过期修改用户状态
            if (!resBoolean) {
                user1.setStatus(false);
                user1.setIs_expired(true);
                user1.setIs_limit(true);
                rpcCommonService.rpcIPinduoduoUserService.update(user1);
                count++;
            }
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("count", count);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(jsonObject));
    }
}
