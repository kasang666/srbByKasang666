package com.ks.srb.core.controller.admin;
// -*-coding:utf-8 -*-

/*
 * File       : AdminDictController.java
 * Time       ：2022/8/25 10:36
 * Author     ：hhs
 * version    ：java8
 * Description：
 */


import com.alibaba.excel.EasyExcel;
import com.ks.common.exception.BusinessException;
import com.ks.common.result.R;
import com.ks.common.result.ResponseEnum;
import com.ks.srb.core.pojo.dto.ExcelDictDTO;
import com.ks.srb.core.pojo.entity.Dict;
import com.ks.srb.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;


@Slf4j
//@CrossOrigin
@Api(tags = "数据字典管理")
@RestController
@RequestMapping("admin/core/dict")
public class AdminDictController {
    @Autowired
    private DictService dictService;

    @ApiOperation(value = "excel批量导入数据")
    @PostMapping("import")
    public R importFromExcel(
            @ApiParam(value = "excel文件", required = true)
            @RequestParam("file") MultipartFile file
            ){
        try {
            InputStream inputStream = file.getInputStream();
            this.dictService.saveDataFromExcel(inputStream);
            return R.success().msg("批量导入成功！");
        } catch (IOException e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR, e);
        }

    }

    @ApiOperation(value = "导出excel")
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response){
        try {
            // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("mydict", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), ExcelDictDTO.class).sheet("模板").doWrite(this.dictService.getList());
        } catch (IOException e) {
            throw new BusinessException(ResponseEnum.EXPORT_DATA_ERROR, e);
        }
    }

    @ApiOperation(value = "根据parentId获取子节点数据列表")
    @GetMapping("/getListByParentId/{parentId}")
    public R getListByParentId(
            @ApiParam(value = "父节点id", required = true)
            @PathVariable Long parentId){
        List<Dict> dictList = this.dictService.getByParentId(parentId);
        return R.success().data("list", dictList);
    }

}
