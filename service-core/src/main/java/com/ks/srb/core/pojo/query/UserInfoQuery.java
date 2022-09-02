package com.ks.srb.core.pojo.query;
// -*-coding:utf-8 -*-

/*
 * File       : UserInfoQuery.java
 * Time       ：2022/9/2 18:34
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("查询对象")
public class UserInfoQuery {

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "状态")
    private Integer status;

    @ApiModelProperty(value = "1: 投资人， 2: 借款人")
    private Integer userType;
}
