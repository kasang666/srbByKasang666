package com.ks.srb.base.config;
// -*-coding:utf-8 -*-

/*
 * File       : Swagger2Config.java
 * Time       ：2022/8/20 12:30
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class Swagger2Config {

    @Bean
    public Docket adminApiConfig(){
        return new Docket(DocumentationType.SWAGGER_2);
    }
}
