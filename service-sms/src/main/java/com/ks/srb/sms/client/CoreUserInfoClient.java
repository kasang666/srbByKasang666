package com.ks.srb.sms.client;
// -*-coding:utf-8 -*-

/*
 * File       : CoreUserInfoClient.java
 * Time       ：2022/9/3 10:58
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import com.ks.srb.sms.client.callback.CoreUserInfoFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "service-core", fallback = CoreUserInfoFallback.class)
public interface CoreUserInfoClient {
    @GetMapping("/api/core/userInfo/checkMobile/{mobile}")
    boolean checkMobile(@PathVariable String mobile);
}
