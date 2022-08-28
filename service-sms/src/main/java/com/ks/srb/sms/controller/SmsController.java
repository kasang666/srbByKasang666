package com.ks.srb.sms.controller;
// -*-coding:utf-8 -*-

/*
 * File       : SmsController.java
 * Time       ：2022/8/27 11:24
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import com.ks.common.result.R;
import com.ks.srb.sms.service.SmsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@Api(tags = "sms短信服务API")
@Slf4j
@RestController
@RequestMapping("/api/sms")
public class SmsController {

    @Autowired
    private SmsService smsService;

    @ApiOperation(value = "获取短信验证码")
    @GetMapping("send/{mobile}")
    public R sendSms(
            @ApiParam(value = "电话号码", required = true)
            @PathVariable String mobile
    ){
        this.smsService.sendSms(mobile);
        return R.success();
    }

}
