package com.ks.srb.core.controller.admin;
// -*-coding:utf-8 -*-

/*
 * File       : AdminUserLoginRecordController.java
 * Time       ：2022/9/2 21:28
 * Author     ：hhs
 * version    ：java8
 * Description：
 */


import com.ks.common.result.R;
import com.ks.srb.core.pojo.entity.UserLoginRecord;
import com.ks.srb.core.service.UserLoginRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "用户登录日志管理")
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/admin/core/userLoginRecord")
public class AdminUserLoginRecordController {
    @Autowired
    private UserLoginRecordService userLoginRecordService;

    @ApiOperation("获取用户登录日志top50")
    @GetMapping("/listTop50/{id}")
    public R getUserLoginRecordTop50(@ApiParam("用户id") @PathVariable Long id){
        List<UserLoginRecord> list = this.userLoginRecordService.getUserLoginRecordTop50(id);
        return R.success().data("list", list);
    }

}
