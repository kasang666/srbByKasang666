package com.ks.srb.core;
// -*-coding:utf-8 -*-

/*
 * File       : ServiceCoreApplication.java
 * Time       ：2022/8/20 11:41
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.ks.srb")  // 由于其他的模块会被导入，所以其他模块的bean也会被扫描到
public class ServiceCoreApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceCoreApplication.class, args);
    }
}
