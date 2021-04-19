package org.xxpay.pay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/**
 *
 */
@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages={"org.xxpay"})
public class XxPayPayAppliaction {
    public static void main(String[] args) {
        SpringApplication.run(XxPayPayAppliaction.class, args);
    }
}
