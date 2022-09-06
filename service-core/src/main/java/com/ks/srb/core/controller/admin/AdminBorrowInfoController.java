package com.ks.srb.core.controller.admin;
// -*-coding:utf-8 -*-

/*
 * File       : AdminBorrowInfoController.java
 * Time       ：2022/9/6 18:22
 * Author     ：hhs
 * version    ：java8
 * Description：
 */


import com.ks.common.result.R;
import com.ks.srb.core.pojo.entity.BorrowInfo;
import com.ks.srb.core.service.BorrowInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "会员借款申请审核")
@Slf4j
@RestController
@RequestMapping("/admin/core/borrowInfo")
public class AdminBorrowInfoController {

    @Autowired
    private BorrowInfoService borrowInfoService;

    @ApiOperation("获取借款信息申请列表")
    @GetMapping("/list")
    public R list(){
        List<BorrowInfo> borrowInfoList = this.borrowInfoService.getBorrowInfoList();
        return R.success().data("list", borrowInfoList);
    }
}
