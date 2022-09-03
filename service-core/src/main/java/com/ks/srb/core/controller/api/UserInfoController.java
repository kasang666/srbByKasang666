package com.ks.srb.core.controller.api;


import com.ks.common.result.R;
import com.ks.srb.base.util.JwtUtils;
import com.ks.srb.core.pojo.vo.LoginVO;
import com.ks.srb.core.pojo.vo.RegisterVO;
import com.ks.srb.core.pojo.vo.UserInfoVO;
import com.ks.srb.core.service.UserInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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
//@CrossOrigin
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
        // 校验用户类型是否合法
        Integer userType = registerVO.getUserType();
        if (!(userType == 1 || userType == 2)){
            return R.error().msg("用户类型错误！");
        }
        this.userInfoService.register(registerVO);
        return R.success();
    }

    @ApiOperation(value = "会员登录")
    @PostMapping("/login")
    public R login(
            @ApiParam(value = "用户登录对象", required = true)
            @RequestBody LoginVO loginVO,
            @ApiParam(value = "请求头对象", required = true)
            HttpServletRequest request
            ){
        String mobile = loginVO.getMobile();
        if (!mobile.matches("^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$")){
            return R.error().msg("用户名或密码错误！");
        }
        String password = loginVO.getPassword();
        if (password == null || "".equals(password)){
            return R.error().msg("用户名或密码错误！！");
        }
        Integer userType = loginVO.getUserType();
        if (!(userType == 1 || userType == 2)){
            return R.error().msg("用户类型错误！");
        }
        // 获取登录ip
        String ip = request.getRemoteAddr();
        UserInfoVO userInfoVO = this.userInfoService.login(loginVO, ip);
        return R.success().data("userInfo", userInfoVO);
    }

    @ApiOperation("校验令牌")
    @GetMapping("/checkToken")
    public R checkToken(HttpServletRequest request){
        String token = request.getHeader("token");
        boolean res = JwtUtils.checkToken(token);
        return res? R.success(): R.error();
    }

    @ApiOperation("校验手机号是否注册")
    @GetMapping("/checkMobile/{mobile}")
    public boolean checkMobile(@ApiParam(value = "需要校验的手机号", required = true) @PathVariable String mobile){
        return this.userInfoService.checkMobile(mobile);
    }


}

