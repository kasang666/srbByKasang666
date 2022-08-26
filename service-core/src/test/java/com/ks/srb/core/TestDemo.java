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
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

@SpringBootTest
@RunWith(SpringRunner.class)
public class TestDemo {
    @Resource
    private DictMapper dictMapper;

    @Test
    public void testFun(){
        System.out.println(dictMapper);
    }

}
