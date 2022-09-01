package com.ks.srb.core.pojo.vo;
// -*-coding:utf-8 -*-

/*
 * File       : RegisterVO.java
 * Time       ：2022/9/1 15:33
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "注册对象")
public class RegisterVO {


    @ApiModelProperty("用户类型（1： 投资人， 2： 借款人）")
    private Integer userType;

    @ApiModelProperty("手机号码")
    private String mobile;

    @ApiModelProperty("验证码")
    private String code;

    @ApiModelProperty("密码")
    private String password;

}
