package com.ks.srb.core.pojo.vo;
// -*-coding:utf-8 -*-

/*
 * File       : LoginVO.java
 * Time       ：2022/9/2 9:18
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "登陆对象")
public class LoginVO {
    @ApiModelProperty(value = "用户类型")
    private Integer userType;

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "密码")
    private String password;
}
