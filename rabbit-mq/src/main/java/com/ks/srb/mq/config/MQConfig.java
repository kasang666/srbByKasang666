package com.ks.srb.mq.config;
// -*-coding:utf-8 -*-

/*
 * File       : MQConfig.java
 * Time       ：2022/9/11 22:15
 * Author     ：hhs
 * version    ：java8
 * Description：
 */


import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {
    @Bean
    public MessageConverter messageConverter(){
        //json字符串转换器
        return new Jackson2JsonMessageConverter();
    }
}
