package com.ks.srb.sms.service.impl;
// -*-coding:utf-8 -*-

/*
 * File       : SmsServiceImpl.java
 * Time       ：2022/8/27 10:40
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.google.gson.Gson;
import com.ks.common.exception.Assert;
import com.ks.common.exception.BusinessException;
import com.ks.common.result.ResponseEnum;
import com.ks.srb.sms.service.SmsService;
import com.ks.srb.sms.util.RandomUtils;
import com.ks.srb.sms.util.SmsPropertiesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void sendSms(String mobile) {
        Pattern pattern = Pattern.compile("^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$");
        // 手机号码不正确， 触发断言
        Assert.isTrue(pattern.matcher(mobile).matches(), ResponseEnum.MOBILE_ERROR);
        // 判断改手机号是否在一分钟内发送过短信
        String mobileKey = "srb:sms:mobile:" + mobile;
        ValueOperations forValue = null;
        Object o = null;
        try {
            forValue = this.redisTemplate.opsForValue();
            o = forValue.get(mobileKey);

        } catch (Exception e) {
            SmsServiceImpl.log.error("redis连接异常：", e.getMessage());
            throw new BusinessException("redis连接异常", 555);
        }
        // 不为空，抛出异常
        Assert.isNull(o, ResponseEnum.ALIYUN_SMS_LIMIT_CONTROL_ERROR);
        com.aliyun.dysmsapi20170525.Client client = null;
        try {
            client = SmsServiceImpl.createClient(SmsPropertiesUtil.ACCESS_KEY_ID, SmsPropertiesUtil.ACCESS_KEY_SECRET);
        } catch (Exception e) {
            SmsServiceImpl.log.error("阿里云连接失败:", e.getMessage());
            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR);
        }
        String fourBitRandom = RandomUtils.getFourBitRandom();
        Map<String, Object> map = new HashMap<>();
        map.put("code", fourBitRandom);
        SendSmsRequest sendSmsRequest = new SendSmsRequest()
                .setSignName(SmsPropertiesUtil.SIGN_NAME)
                .setTemplateCode(SmsPropertiesUtil.TEMPLATE_CODE)
                .setPhoneNumbers(mobile)
                .setTemplateParam(new Gson().toJson(map));
        RuntimeOptions runtime = new RuntimeOptions();
        try {
            // 复制代码运行请自行打印 API 的返回值
            client.sendSmsWithOptions(sendSmsRequest, runtime);
            // 将验证码存入redis里面
            String smsCodeKey = "srb:sms:code:" + fourBitRandom;
            try {
                forValue.set(smsCodeKey, fourBitRandom, 5, TimeUnit.MINUTES);
                // 同时将手机号码标价，用来限流
                forValue.set(mobileKey, 1, 1, TimeUnit.MINUTES);
            } catch (Exception e) {
                SmsServiceImpl.log.error("redis连接异常：", e.getMessage());
                throw new BusinessException("redis连接异常", 555);
            }
        } catch (TeaException error) {
            SmsServiceImpl.log.error("短信发送失败:", error.getMessage());
            // 如有需要，请打印 error
            com.aliyun.teautil.Common.assertAsString(error.message);
            // 短信发送失败异常
            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR);
        } catch (Exception _error) {
            TeaException error = new TeaException(_error.getMessage(), _error);
            SmsServiceImpl.log.error("短信发送失败:", error.getMessage());
            // 如有需要，请打印 error
            com.aliyun.teautil.Common.assertAsString(error.message);
            // 短信发送失败异常
            throw new BusinessException(ResponseEnum.ALIYUN_SMS_ERROR);
        }
    }

    public static com.aliyun.dysmsapi20170525.Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
        Config config = new Config()
                // 您的 AccessKey ID
                .setAccessKeyId(accessKeyId)
                // 您的 AccessKey Secret
                .setAccessKeySecret(accessKeySecret);
        // 访问的域名
        config.endpoint = "dysmsapi.aliyuncs.com";
        return new com.aliyun.dysmsapi20170525.Client(config);
    }

}
