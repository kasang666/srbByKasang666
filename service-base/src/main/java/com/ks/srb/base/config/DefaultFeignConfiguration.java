package com.ks.srb.base.config;
// -*-coding:utf-8 -*-

/*
 * File       : DefaultFeignConfiguration.java
 * Time       ：2022/9/3 11:47
 * Author     ：hhs
 * version    ：java8
 * Description：
 */


import feign.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DefaultFeignConfiguration {
    @Bean
    public Logger.Level feignLogLevel(){
        return Logger.Level.FULL; // 日志级别为BASIC
    }
}
