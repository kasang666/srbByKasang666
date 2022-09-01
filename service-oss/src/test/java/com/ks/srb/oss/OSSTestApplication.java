package com.ks.srb.oss;
// -*-coding:utf-8 -*-

/*
 * File       : OSSTestApplication.java
 * Time       ：2022/8/28 17:20
 * Author     ：hhs
 * version    ：java8
 * Description：
 */


import com.ks.srb.oss.util.OSSPropertiesUtil;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;


//@RunWith(SpringRunner.class)
@SpringBootTest
public class OSSTestApplication {
    @Test
    public void testProperties() {
        System.out.println(OSSPropertiesUtil.ACCESS_KEY_ID);
        System.out.println(OSSPropertiesUtil.ACCESS_KEY_SECRET);
        System.out.println(OSSPropertiesUtil.ENDPOINT);
        System.out.println(OSSPropertiesUtil.BUCKET_NAME);
    }

    @Test
    public void testLocalDateTime(){
        System.out.println(LocalDate.now().toString().replace("-", "/"));
    }

}
