package com.ks.srb.core.controller.api;


import com.ks.common.result.R;
import com.ks.srb.core.pojo.vo.RegisterVO;
import com.ks.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 用户基本信息 前端控制器
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */

@Slf4j
@Api(tags = "会员接口")
@CrossOrigin
@RestController
@RequestMapping("api/core/userInfo")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @ApiOperation(value = "用户注册")
    @PostMapping("/register")
    public R register(
            @ApiParam(value = "用户对象", required = true)
            @RequestBody RegisterVO registerVO){
        // 校验手机号正确性
        String mobile = registerVO.getMobile();
        if (mobile == null || "".equals(mobile) || !mobile.matches("^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$")){
            return R.error().msg("手机号码错误");
        }
        // 校验验证码
        ValueOperations forValue = this.redisTemplate.opsForValue();
        String key = "srb:sms:code:" + mobile;
        Object trueCode = (String)forValue.get(key);
        String code = registerVO.getCode();
        if (trueCode == null && !trueCode.equals(code)){
            return R.error().msg("验证码错误！");
        }
        // 校验密码是否合法
        String password = registerVO.getPassword();
        if (!password.matches("^[a-zA-Z]\\w{5,17}$")){
            return R.error().msg("密码不合法！");
        }
        this.userInfoService.register(registerVO);
        return R.success();
    }

}

