package com.ks.srb.core.controller.admin;
// -*-coding:utf-8 -*-

/*
 * File       : AdminLendController.java
 * Time       ：2022/9/8 8:40
 * Author     ：hhs
 * version    ：java8
 * Description：
 */


import com.ks.common.result.R;
import com.ks.srb.core.pojo.entity.Lend;
import com.ks.srb.core.service.LendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Api(tags = "标的后台管理")
@Slf4j
@RestController
@RequestMapping("/admin/core/lend")
public class AdminLendController {

    @Autowired
    private LendService lendService;

    @ApiOperation("获取标的列表")
    @GetMapping("/list")
    public R list(){
        List<Lend> list =  this.lendService.getList();
        return R.success().data("list", list);
    }

    @ApiOperation("获取标的详情")
    @GetMapping("/show/{id}")
    public R getDetailById(@ApiParam(value = "标的id", required = true)
                           @PathVariable Long id){
        Map<String, Object> lendDetail = this.lendService.getDetailById(id);
        return R.success().data("lendDetail", lendDetail);
    }


    @ApiOperation("放款接口")
    @GetMapping("/makeLoan/{id}")
    public R makeLoan(@ApiParam(value = "标的id", required = true)
                      @PathVariable Long id){
        this.lendService.makeLoan(id);
        return R.success();
    }

}
