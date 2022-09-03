package com.ks.srb.sms;
// -*-coding:utf-8 -*-

/*
 * File       : ServiceSmsSpringBootApplication.java
 * Time       ：2022/8/27 10:44
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import com.ks.srb.base.config.DefaultFeignConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


@EnableFeignClients(defaultConfiguration = DefaultFeignConfiguration.class)
@ComponentScan({"com.ks.srb", "com.ks.common"})
@SpringBootApplication
public class ServiceSmsSpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceSmsSpringBootApplication.class, args);
    }
}
