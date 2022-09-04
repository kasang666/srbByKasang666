package com.ks.srb.oss.controller;
// -*-coding:utf-8 -*-

/*
 * File       : OSSController.java
 * Time       ：2022/8/29 21:32
 * Author     ：hhs
 * version    ：java8
 * Description：
 */


import com.ks.common.result.R;
import com.ks.srb.oss.service.OSSService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@Api(tags = "文件存储服务")
//@CrossOrigin
@RequestMapping("/api/oss/file")
@RestController
public class OSSController {
    @Autowired
    private OSSService ossService;

    @ApiOperation(value = "文件上传")
    @PostMapping("/upload")
    public R uploadFile(@ApiParam(value = "文件对象", required = true)
                        @RequestParam("file") MultipartFile file,

                        @ApiParam(value = "模块名", required = true)
                        @RequestParam("module") String module){

        String url = this.ossService.uploadFile(file, module);
        return R.success().data("url", url);
    }

    @ApiOperation(value = "文件删除")
    @DeleteMapping("/remove")
    public R removeFile(@ApiParam(value = "文件名称", required = true)
                        @RequestParam("url") String url){
        this.ossService.removeFile(url);
        return R.success().msg("删除成功！");
    }

}
