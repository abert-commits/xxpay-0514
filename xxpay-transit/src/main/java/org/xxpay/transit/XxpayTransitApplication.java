package org.xxpay.transit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class XxpayTransitApplication {

    public static void main(String[] args) {
        SpringApplication.run(XxpayTransitApplication.class, args);
    }

}
