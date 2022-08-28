package com.ks.srb.sms;
// -*-coding:utf-8 -*-

/*
 * File       : SmsServiceTest.java
 * Time       ：2022/8/27 10:52
 * Author     ：hhs
 * version    ：java8
 * Description：
 */


import com.google.gson.Gson;
import com.ks.srb.sms.util.SmsPropertiesUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class SmsServiceTest {

    @Autowired
    private SmsPropertiesUtil smsPropertiesUtil;

    @Test
    public void propertiesTest(){
        System.out.println(SmsPropertiesUtil.ACCESS_KEY_ID);
        System.out.println(SmsPropertiesUtil.ACCESS_KEY_SECRET);
        System.out.println(SmsPropertiesUtil.SIGN_NAME);
        System.out.println(SmsPropertiesUtil.TEMPLATE_CODE);
    }

    @Test
    public void mapTest(){
        Map<String, String> map = new HashMap<>();
        map.put("code", "11111111111");
        System.out.println(new Gson().toJson(map));

    }

}
