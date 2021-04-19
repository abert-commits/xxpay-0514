package org.xxpay.agent.company.ctrl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.xxpay.agent.common.config.OOSConfig;
import org.xxpay.agent.common.ctrl.BaseController;
import org.xxpay.agent.common.service.RpcCommonService;
import org.xxpay.core.common.annotation.MethodLog;
import org.xxpay.core.common.constant.Constant;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.domain.BizResponse;
import org.xxpay.core.common.domain.XxPayPageRes;
import org.xxpay.core.common.domain.XxPayResponse;
import org.xxpay.core.common.util.AmountUtil;
import org.xxpay.core.common.util.DateUtil;
import org.xxpay.core.common.util.MD5Util;
import org.xxpay.core.entity.AgentInfo;
import org.xxpay.core.entity.AlipayConfig;
import org.xxpay.core.entity.MchInfo;
import org.xxpay.core.entity.PayOrder;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Controller
@RequestMapping(Constant.AGENT_CONTROLLER_ROOT_PATH + "/company")
public class CompanyController extends BaseController {

    @Autowired
    private RpcCommonService rpcCommonService;

    /**
     * 新增支付宝企业账号
     *
     * @return
     */
    @RequestMapping("/add")
    @ResponseBody
    @MethodLog(remark = "新增支付宝企业账号")
    public ResponseEntity<?> add(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        AlipayConfig alipayConfig = JSONObject.parseObject(JSONObject.toJSONString(param), AlipayConfig.class);
        //1:校验填写信息，
        if (StringUtils.isBlank(alipayConfig.getPID())) {
            return ResponseEntity.ok(new BizResponse(10001, "请填写支付宝企业合作伙伴ID=>PID"));
        }

        if (StringUtils.isBlank(alipayConfig.getCompanyName())) {
            return ResponseEntity.ok(new BizResponse(10001, "请填写支付宝企业名称"));
        }

        if (StringUtils.isBlank(alipayConfig.getAPPID())) {
            return ResponseEntity.ok(new BizResponse(10001, "请填写应用APPID"));
        }

        if (StringUtils.isBlank(alipayConfig.getEmail())) {
            return ResponseEntity.ok(new BizResponse(10001, "请填写支付宝邮箱账号"));
        }
        if (StringUtils.isBlank(alipayConfig.getEmail())) {
            return ResponseEntity.ok(new BizResponse(10001, "请填写私钥"));
        }

        //2：校验是否重复添加
        AlipayConfig condtion = new AlipayConfig();
//        condtion.setPID(alipayConfig.getPID());
        AlipayConfig oldModel = null;
//        oldModel = rpcCommonService.rpcAliPayConfigService.findAliPayConfig(condtion);
//        if (oldModel != null) {
//            return ResponseEntity.ok(new BizResponse(10001, "支付宝企业合作伙伴ID已存在，请勿重复添加！"));
//        }

        condtion = new AlipayConfig();
        condtion.setAPPID(alipayConfig.getAPPID());
        oldModel = rpcCommonService.rpcAliPayConfigService.findAliPayConfig(condtion);
        if (oldModel != null) {
            return ResponseEntity.ok(new BizResponse(10001, "应用APPID已存在，请勿重复添加！"));
        }

        condtion = new AlipayConfig();
        condtion.setEmail(alipayConfig.getEmail());
        oldModel = rpcCommonService.rpcAliPayConfigService.findAliPayConfig(condtion);
        if (oldModel != null) {
            return ResponseEntity.ok(new BizResponse(10001, "支付宝邮箱账号已存在，请勿重复添加！"));
        }

        AgentInfo agentInfo = rpcCommonService.rpcAgentInfoService.findByAgentId(this.getUser().getId());

        alipayConfig.setParentAgentId(String.valueOf(agentInfo.getParentAgentId()));
        alipayConfig.setAgentId(this.getUser().getId());
        alipayConfig.setCreateTime(new Date());
        alipayConfig.setUpdateTime(new Date());
        alipayConfig.setStatus(0);

        int count = rpcCommonService.rpcAliPayConfigService.insertSelective(alipayConfig);
        if (count != 1) ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
        return ResponseEntity.ok(BizResponse.buildSuccess());
    }


    /**
     * 修改支付宝企业账号
     *
     * @param request
     * @return
     */
    @RequestMapping("/update")
    @ResponseBody
    @MethodLog(remark = "修改支付宝企业账号")

    public ResponseEntity<?> update(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        AlipayConfig alipayConfig = JSONObject.parseObject(JSONObject.toJSONString(param), AlipayConfig.class);
        AlipayConfig odlAlipayConfig = rpcCommonService.rpcAliPayConfigService.selectById(alipayConfig.getId());
        if (odlAlipayConfig == null) {
            return ResponseEntity.ok(new BizResponse(10001, "支付宝企业信息异常！"));
        }

        //2：校验是否重复添加
        AlipayConfig condtion = new AlipayConfig();
        AlipayConfig oldmodel = null;
//        if (!StringUtils.isBlank(alipayConfig.getPID())) {
//            condtion.setPID(alipayConfig.getPID());
//            List<AlipayConfig> searchList = null;
//            oldmodel = rpcCommonService.rpcAliPayConfigService.findAliPayConfig(condtion);
//            if (oldmodel != null && oldmodel.getId() != alipayConfig.getId()) {
//                return ResponseEntity.ok(new BizResponse(10001, "支付宝企业合作伙伴ID已存在，请勿重复添加！"));
//            }
//        }

        if (!StringUtils.isBlank(alipayConfig.getAPPID())) {
            condtion = new AlipayConfig();
            condtion.setAPPID(alipayConfig.getAPPID());
            oldmodel = rpcCommonService.rpcAliPayConfigService.findAliPayConfig(condtion);
            if (oldmodel != null && alipayConfig.getId() != oldmodel.getId()) {
                return ResponseEntity.ok(new BizResponse(10001, "应用APPID已存在，请勿重复添加！"));
            }
        }
        if (!StringUtils.isBlank(alipayConfig.getEmail())) {
            condtion = new AlipayConfig();
            condtion.setEmail(alipayConfig.getEmail());
            oldmodel = rpcCommonService.rpcAliPayConfigService.findAliPayConfig(condtion);
            if (oldmodel != null && alipayConfig.getId() != oldmodel.getId()) {
                return ResponseEntity.ok(new BizResponse(10001, "支付宝邮箱账号已存在，请勿重复添加！"));
            }
        }


        int count = rpcCommonService.rpcAliPayConfigService.updateByPrimaryKeySelective(alipayConfig);
        if (count != 1) ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
        return ResponseEntity.ok(BizResponse.buildSuccess());
    }


    /**
     * 修改支付宝企业账号
     *
     * @param request
     * @return
     */
    @RequestMapping("/get")
    @ResponseBody
    @MethodLog(remark = "根据主键ID获取企业账号信息")
    public ResponseEntity<?> get(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer Id = getIntegerRequired(param, "id");
        Long agentId = getUser().getId();
        AlipayConfig condtion = new AlipayConfig();
        condtion.setAgentId(agentId);
        condtion.setId(Id);
        AlipayConfig model = rpcCommonService.rpcAliPayConfigService.findAliPayConfig(condtion);
        return ResponseEntity.ok(XxPayResponse.buildSuccess(model));
    }
    @Autowired
    private OOSConfig oosConfig;

    /**
     * 实现文件上传
     */
    @RequestMapping("/fileUpload")
    @ResponseBody
    public ResponseEntity<?> fileUpload(@RequestParam("fileName") MultipartFile file, HttpServletRequest request) throws IOException {
        JSONObject param = getJsonParam(request);

        if (file.isEmpty()) {
            return ResponseEntity.ok(XxPayPageRes.buildSuccess(RetEnum.RET_COMM_PARAM_ERROR));
        }
        String fileName = file.getOriginalFilename();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream bos = null;
        try {
            bos = file.getInputStream();
            byte[] byteData = new byte[1024];
            // 从in 这个流中读取数据，每次最多读取200个字节，赋给byteData,
            // 返回值是一次实际读取的字节个数赋给length, 比如现在的流中只有150个字节，则第一次循环的时候length = 150
            // 第二次条件判断 length = -1 , 结束读取操作
            int length = 0;
            while (-1 != (length = bos.read(byteData, 0, 1024))) {
                output.write(byteData, 0, length);
            }
            bos.close(); // 关闭流，一定要关闭
        } catch (IOException e) {
            e.printStackTrace();
        }
        AlipayConfig alipayConfig = rpcCommonService.rpcAliPayConfigService.selectById(param.getInteger("id"));
        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(oosConfig.getEndpoint(), oosConfig.getAccessKeyId(), oosConfig.getAccessKeySecret());

        //判断 上传文件为 私钥还是公钥
        if ("AliPayCertPublickey".equals(param.getString("type"))) {
            alipayConfig.setAliPayCertPublickeyFileName(fileName);
            alipayConfig.setAliPayCertPublickey(output.toByteArray());
            //oos 传输
            //AliPayCertPublickey 支付公钥
            // 上传内容到指定的存储空间（bucketName）并保存为指定的文件名称（objectName）。
            ossClient.putObject(oosConfig.getBucketName(), alipayConfig.getAPPID()+"_alipaycertpublickey.crt", file.getInputStream());
        } else {
            alipayConfig.setAppCertPublickeyFileName(fileName);
            alipayConfig.setAppCertPublickey(output.toByteArray());
            //oos 传输
            //AppCertPublickey 应用公钥
            // 上传内容到指定的存储空间（bucketName）并保存为指定的文件名称（objectName）。
            ossClient.putObject(oosConfig.getBucketName(),  alipayConfig.getAPPID()+"_appcertpublickey.crt", file.getInputStream());
        }
        // 关闭OSSClient。
        ossClient.shutdown();
        int count = rpcCommonService.rpcAliPayConfigService.updateByPrimaryKeySelective(alipayConfig);
        if (count != 1) ResponseEntity.ok(XxPayResponse.build(RetEnum.RET_COMM_OPERATION_FAIL));
        return ResponseEntity.ok(BizResponse.buildSuccess());
    }


    /**
     * 企业账号列表
     *
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public ResponseEntity<?> list(HttpServletRequest request) {
        JSONObject param = getJsonParam(request);
        Integer page = getInteger(param, "page");
        Integer limit = getInteger(param, "limit");
        AlipayConfig reqModel = JSONObject.parseObject(JSONObject.toJSONString(param), AlipayConfig.class);

        Long agentId = getUser().getId();
        reqModel.setAgentId(agentId);

        // 起止时间
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

        int count = rpcCommonService.rpcAliPayConfigService.count(reqModel, createTimeStart, createTimeEnd);

        reqModel.setAgentId(null);
        reqModel.setParentAgentId(String.valueOf(agentId));
        int parentCount = rpcCommonService.rpcAliPayConfigService.count(reqModel, createTimeStart, createTimeEnd);
        if ((parentCount + count) == 0) return ResponseEntity.ok(XxPayPageRes.buildSuccess());


        reqModel.setAgentId(agentId);
        reqModel.setParentAgentId(null);
        List<AlipayConfig> alipayConfigList = rpcCommonService.rpcAliPayConfigService.select(
                (getPageIndex(page) - 1) * getPageSize(limit), getPageSize(limit), reqModel, createTimeStart, createTimeEnd);


        reqModel.setAgentId(null);
        reqModel.setParentAgentId(String.valueOf(agentId));
        List<AlipayConfig> parentAlipayConfigList = rpcCommonService.rpcAliPayConfigService.select(
                (getPageIndex(page) - 1) * getPageSize(limit), getPageSize(limit), reqModel, createTimeStart, createTimeEnd);


        alipayConfigList.addAll(parentAlipayConfigList);

        // 转换前端显示
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();//new一个Calendar类,把Date放进去
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);//实现日期减一操作,也就是昨天

        List<JSONObject> objects = new LinkedList<>();
        AgentInfo agentInfo=rpcCommonService.rpcAgentInfoService.findByAgentId(agentId);
        if (agentInfo.getParentAgentId()!=null && agentInfo.getParentAgentId()!=0)
        {
            agentId=agentInfo.getParentAgentId();
        }
        for (AlipayConfig info : alipayConfigList) {
            JSONObject object = (JSONObject) JSON.toJSON(info);
            //累计交易总金额
            Map allMap = rpcCommonService.rpcPayOrderService.count4SuccessByAppId(agentId, info.getAPPID(), null, null);
            object.put("successTotalAmount", allMap.get("totalAmount"));
            //今日交易成功金额
            Map todayMap = rpcCommonService.rpcPayOrderService.count4SuccessByAppId(agentId, info.getAPPID(), ymd + " 00:00:00", ymd + " 23:59:59");
            object.put("todayTotalAmount", todayMap.get("totalAmount"));

            //今日总交易金额
            Map todayallMap = rpcCommonService.rpcPayOrderService.count4AllByAppId(agentId, info.getAPPID(), ymd + " 00:00:00", ymd + " 23:59:59");

            BigDecimal dayAllCount = new BigDecimal(String.valueOf(todayallMap.get("totalCount")));
            BigDecimal daySuccessCount = new BigDecimal(String.valueOf(todayMap.get("totalCount")));

            BigDecimal successRate = new BigDecimal(0);
            if (daySuccessCount.longValue() > 0) {
                successRate = daySuccessCount.divide(dayAllCount, 4, BigDecimal.ROUND_UP);
            }

            //昨日交易成功金额
            object.put("successRate", successRate.multiply(new BigDecimal("100")).doubleValue() + "%");
            String yesterdayDate = DateUtil.date2Str(calendar.getTime(), DateUtil.FORMAT_YYYY_MM_DD);

            //昨日交易金额
            Map yesterdayMap = rpcCommonService.rpcPayOrderService.count4SuccessByAppId(agentId, info.getAPPID(), yesterdayDate + " 00:00:00", yesterdayDate + " 23:59:59");
            object.put("yesterdayDateTotalAmount", yesterdayMap.get("totalAmount"));

            objects.add(object);
        }

        return ResponseEntity.ok(XxPayPageRes.buildSuccess(objects, count + parentCount));
    }
}
