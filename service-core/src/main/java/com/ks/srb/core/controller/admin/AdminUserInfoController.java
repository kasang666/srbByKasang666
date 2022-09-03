package com.ks.srb.core.controller.admin;
// -*-coding:utf-8 -*-

/*
 * File       : AdminUserInfoController.java
 * Time       ：2022/9/2 19:24
 * Author     ：hhs
 * version    ：java8
 * Description：
 */


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ks.common.result.R;
import com.ks.srb.core.pojo.entity.UserInfo;
import com.ks.srb.core.pojo.query.UserInfoQuery;
import com.ks.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Api(tags = "后台用户管理")
//@CrossOrigin
@RestController
@RequestMapping("/admin/core/userInfo")
public class AdminUserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @ApiOperation("用户列表")
    @GetMapping("/list/{page}/{limit}")
    public R pageList(
            @ApiParam(value = "用户查询对象", required = false)
            UserInfoQuery userInfoQuery,
//GET 请求 当使用 @RequestParm 注解 和 不加注解时，只能接收到 params 携带的参数 ，参数放在 请求头 和请求体中均接收不到
//POST请求 当使用 @RequestParm 注解 和 不加注解时，只能接收到 params 和请求体xxx格式携带的参数，加注解无法接收到对象参数
//POST请求 当使用 @RequestBody注解 ，只能接收到请求体JSON格式和表单携带的参数 其他类型参数均接受不到
//POST请求接收一个参数 不可以使用 @RequestBody 注解
            @ApiParam(value = "查询页数", required = true)
            @PathVariable("page") Integer page,

            @ApiParam(value = "每页条数", required = true)
            @PathVariable("limit") Integer limit
                      ){
        Page<UserInfo> pageList = this.userInfoService.getPageList(userInfoQuery, page, limit);
        return R.success().data("pageModel", pageList);
    }


    @ApiOperation("锁定和解锁")
    @PutMapping("/lock/{id}/{status}")
    public R updateStatus(
            @ApiParam(value = "用户id", required = true)
            @PathVariable("id") Long id,
            @ApiParam(value = "用户状态", required = true)
            @PathVariable("status") Integer status){
        this.userInfoService.lock(id, status);
        return status == 1? R.success().msg("解锁成功！"): R.success().msg("锁定成功！");
    }

}
