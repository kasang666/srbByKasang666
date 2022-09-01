package com.ks.srb.oss.service.impl;
// -*-coding:utf-8 -*-

/*
 * File       : OSSServiceImpl.java
 * Time       ：2022/8/29 21:32
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.CannedAccessControlList;
import com.ks.common.exception.BusinessException;
import com.ks.common.result.ResponseEnum;
import com.ks.srb.oss.service.OSSService;
import com.ks.srb.oss.util.OSSPropertiesUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class OSSServiceImpl implements OSSService {
    @Override
    public String uploadFile(MultipartFile file, String module) {

        String filePath = null;
        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            throw new BusinessException(ResponseEnum.UPLOAD_ERROR);
        }
        String name = file.getOriginalFilename();
        // 获取文件后缀
        String fileSuffix = name.substring(name.lastIndexOf("."));
        // 文件根路径  2022/08/29
        String rootPath = LocalDate.now().toString().replace("-", "/");
        // 自定义文件名
        UUID fileName = UUID.randomUUID();
        // 文件路径
        filePath = module + "/" +rootPath + "/" + fileName + fileSuffix;
        this.uploadFileApi(filePath, inputStream);
        return "https://" + OSSPropertiesUtil.BUCKET_NAME + "." + OSSPropertiesUtil.ENDPOINT + "/" +filePath;
    }

    @Override
    public void removeFile(String fileName) {
        // 去除https://等前缀
        try {
            fileName = fileName.split(OSSPropertiesUtil.ENDPOINT + "/")[1];
        } catch (Exception e) {
            throw new BusinessException("文件删除失败！", 500, e);
        }
        this.removeFileApi(fileName);
    }

    private void uploadFileApi(String filePath, InputStream inputStream){
        // 创建OSSClient实例。
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(OSSPropertiesUtil.ENDPOINT, OSSPropertiesUtil.ACCESS_KEY_ID, OSSPropertiesUtil.ACCESS_KEY_SECRET);
            if (!ossClient.doesBucketExist(OSSPropertiesUtil.BUCKET_NAME)){
                // 如果不存在bucket就创建, 同时设置权限
                ossClient.createBucket(OSSPropertiesUtil.BUCKET_NAME);
                ossClient.setBucketAcl(OSSPropertiesUtil.BUCKET_NAME, CannedAccessControlList.PublicRead);
            }
            // 文件上传
            ossClient.putObject(OSSPropertiesUtil.BUCKET_NAME, filePath, inputStream);
            // 返回文件访问url

        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    public void removeFileApi(String fileName){
        // 创建OSSClient实例。
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(OSSPropertiesUtil.ENDPOINT, OSSPropertiesUtil.ACCESS_KEY_ID, OSSPropertiesUtil.ACCESS_KEY_SECRET);
            // 判断文件是否存在
            boolean res = ossClient.doesObjectExist(OSSPropertiesUtil.BUCKET_NAME, fileName);
            if (res){
                // 文件删除
                ossClient.deleteObject(OSSPropertiesUtil.BUCKET_NAME, fileName);
            }else{
                throw new BusinessException("需要删除的文件不存在！", 500);
            }
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            throw new BusinessException("文件删除失败！", 500, oe);
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
            throw new BusinessException("文件删除失败！", 500, ce);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }


}
