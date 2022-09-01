package com.ks.srb.oss.service;
// -*-coding:utf-8 -*-

/*
 * File       : OSSService.java
 * Time       ：2022/8/29 21:32
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import org.springframework.web.multipart.MultipartFile;

public interface OSSService {
    String uploadFile(MultipartFile file, String module);
    void removeFile(String fileName);
}
