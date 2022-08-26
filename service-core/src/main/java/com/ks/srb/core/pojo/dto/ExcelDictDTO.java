package com.ks.srb.core.pojo.dto;
// -*-coding:utf-8 -*-

/*
 * File       : ExcelDictDTO.java
 * Time       ：2022/8/25 9:24
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ExcelDictDTO {
    @ExcelProperty(value = "id")
    private Long id;
    @ExcelProperty(value = "上级id")
    private Long parentId;
    @ExcelProperty(value = "名称")
    private String name;
    @ExcelProperty(value = "值")
    private Integer value;
    @ExcelProperty(value = "编码")
    private String dictCode;

}
