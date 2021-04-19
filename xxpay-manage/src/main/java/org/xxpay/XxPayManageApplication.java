package org.xxpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
@ServletComponentScan
public class XxPayManageApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        System.out.println("佛祖保佑，永无bug，永不宕机");
        SpringApplication.run(XxPayManageApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        application.listeners();
        return application.sources(applicationClass);
    }

    private static Class<XxPayManageApplication> applicationClass = XxPayManageApplication.class;

}