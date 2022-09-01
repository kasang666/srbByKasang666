package com.ks.srb.core;
// -*-coding:utf-8 -*-

/*
 * File       : TestDemo.java
 * Time       ：2022/8/26 22:42
 * Author     ：hhs
 * version    ：java8
 * Description：
 */


import com.ks.srb.core.mapper.DictMapper;
import org.junit.Test;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

public class TestDemo {
    @Resource
    private DictMapper dictMapper;

    @Test
    public void testFun(){
        System.out.println(DigestUtils.md5DigestAsHex("123456".getBytes(StandardCharsets.UTF_8)));
    }

}
