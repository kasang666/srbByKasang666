package com.ks.srb.sms.client.callback;
// -*-coding:utf-8 -*-

/*
 * File       : CoreUserInfoFallback.java
 * Time       ：2022/9/3 12:47
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import com.ks.srb.sms.client.CoreUserInfoClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CoreUserInfoFallback implements CoreUserInfoClient {
    @Override
    public boolean checkMobile(String mobile) {
        log.error("远程调用失败，服务熔断");
        return false;
    }
}
