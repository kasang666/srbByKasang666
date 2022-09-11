package com.ks.srb.base.DTO;
// -*-coding:utf-8 -*-

/*
 * File       : SmsDTO.java
 * Time       ：2022/9/11 22:18
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "短信")
public class SmsDTO {

    @ApiModelProperty(value = "手机号")
    private String mobile;

    @ApiModelProperty(value = "消息内容")
    private String message;
}