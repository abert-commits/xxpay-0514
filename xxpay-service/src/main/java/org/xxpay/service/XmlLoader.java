package org.xxpay.service;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Created with IntelliJ IDEA.
 * User: zhanglei
 * Date: 18/3/8 下午10:46
 * Description: 配置文件加载器
 */
@Configuration
@ImportResource(locations = {"classpath*:application-datasource.xml"})
public class XmlLoader {
}
