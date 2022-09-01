package com.ks.srb.oss;
// -*-coding:utf-8 -*-

/*
 * File       : OSSSpringBootApplication.java
 * Time       ：2022/8/29 19:57
 * Author     ：hhs
 * version    ：java8
 * Description：
 */


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.ks.srb", "com.ks.common"})
@SpringBootApplication
public class OSSSpringBootApplication {
    public static void main(String[] args) {
        SpringApplication.run(OSSSpringBootApplication.class, args);
    }
}
