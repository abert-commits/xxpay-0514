package org.xxpay.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 *
 */
@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages={"org.xxpay"})
public class XxPayServiceAppliaction {
    public static void main(String[] args) {
        System.out.println("日你姐的个逼的！！！");
        SpringApplication.run(XxPayServiceAppliaction.class, args);
    }
}
