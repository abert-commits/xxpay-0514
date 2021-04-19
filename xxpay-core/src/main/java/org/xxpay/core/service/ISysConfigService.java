package org.xxpay.core.service;

import com.alibaba.fastjson.JSONObject;
import org.xxpay.core.entity.SysConfig;

import java.util.List;

/**
 * @author: dingzhiwei
 * @date: 18/08/22
 * @description: 系统参数配置接口
 */
public interface ISysConfigService {


    List<SysConfig> select(String type);

    JSONObject getSysConfigObj(String type);

    int updateAll(List<SysConfig> sysConfigList);

    int update(JSONObject obj);

    SysConfig findCode(String code);
}
