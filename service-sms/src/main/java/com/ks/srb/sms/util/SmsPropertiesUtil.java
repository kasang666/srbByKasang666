package com.ks.srb.sms.util;
// -*-coding:utf-8 -*-

/*
 * File       : SmsPropertiesUtil.java
 * Time       ：2022/8/27 10:29
 * Author     ：hhs
 * version    ：java8
 * Description：
 */


import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(value = "aliyun.sms")
public class SmsPropertiesUtil implements InitializingBean {
    /**
     * 读取阿里云短信配置文件
     */
    private String accessKeyId;
    private String accessKeySecret;
    private String signName;
    private String templateCode;

    public static String ACCESS_KEY_ID;
    public static String ACCESS_KEY_SECRET;
    public static String SIGN_NAME;
    public static String TEMPLATE_CODE;

    /**
     * 实现InitializingBean接口的目的是为了可以在spring将对象创建之后进行一些我们希望的操作
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        ACCESS_KEY_ID = this.accessKeyId;
        ACCESS_KEY_SECRET = this.accessKeySecret;
        SIGN_NAME = this.signName;
        TEMPLATE_CODE = this.templateCode;
    }
}
