package com.ks.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.common.exception.Assert;
import com.ks.common.exception.BusinessException;
import com.ks.common.result.ResponseEnum;
import com.ks.srb.base.util.JwtUtils;
import com.ks.srb.core.mapper.UserInfoMapper;
import com.ks.srb.core.pojo.entity.UserAccount;
import com.ks.srb.core.pojo.entity.UserInfo;
import com.ks.srb.core.pojo.entity.UserLoginRecord;
import com.ks.srb.core.pojo.query.UserInfoQuery;
import com.ks.srb.core.pojo.vo.LoginVO;
import com.ks.srb.core.pojo.vo.RegisterVO;
import com.ks.srb.core.pojo.vo.UserInfoVO;
import com.ks.srb.core.service.UserAccountService;
import com.ks.srb.core.service.UserInfoService;
import com.ks.srb.core.service.UserLoginRecordService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * <p>
 * 用户基本信息 服务实现类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {

    @Autowired
    private UserAccountService accountService;

    @Autowired
    private UserLoginRecordService userLoginRecordService;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public void register(RegisterVO registerVO) {
        String mobile = registerVO.getMobile();
        LambdaQueryWrapper<UserInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserInfo::getMobile, mobile);
        Integer count = this.baseMapper.selectCount(lqw);
        if(count != 0){
            // 手机号已被注册
            throw new BusinessException(ResponseEnum.MOBILE_EXIST_ERROR);
        }
        // 对密码进行md5加密
        String password = registerVO.getPassword();
        String passwordByMD5 = DigestUtils.md5DigestAsHex(password.getBytes(StandardCharsets.UTF_8));


        // 初始化用户的信息
        // https://srb-file-by-ks.oss-cn-hangzhou.aliyuncs.com/avator/0a3b3288-3446-4420-bbff-f263d0c02d8e.jpg
        UserInfo userInfo = new UserInfo();
        userInfo.setName(registerVO.getMobile());
        userInfo.setNickName(registerVO.getMobile());
        userInfo.setMobile(registerVO.getMobile());
        userInfo.setPassword(passwordByMD5);
        userInfo.setUserType(registerVO.getUserType());
        userInfo.setStatus(UserInfo.STATIC_NORMAL);
        userInfo.setHeadImg(UserInfo.HEAD_IMG);

        // 保存
        this.baseMapper.insert(userInfo);

        // 创建UserAccount账户
        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userInfo.getId());
        // 保存账户信息
        accountService.save(userAccount);
    }

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public UserInfoVO login(LoginVO loginVO, String ip) {
        Integer userType = loginVO.getUserType();
        String mobile = loginVO.getMobile();
        String password = loginVO.getPassword();
        LambdaQueryWrapper<UserInfo> lqw = new LambdaQueryWrapper<>();
        lqw
                .eq(mobile != null, UserInfo::getMobile, mobile)
                .eq(password != null, UserInfo::getPassword, password)
                .eq(userType!=null, UserInfo::getUserType, userType);
        UserInfo one = this.getOne(lqw);
        // 用户不存在
        Assert.notNull(one, ResponseEnum.USERNAME_OR_PASSWORD_ERROR);
        // 用户被锁定
        Integer status = one.getStatus();
        Assert.equals(status, UserInfo.STATIC_NORMAL, ResponseEnum.LOGIN_LOKED_ERROR);
        // 记录登录日志
        UserLoginRecord userLoginRecord = new UserLoginRecord();
        userLoginRecord.setUserId(one.getId());
        userLoginRecord.setIp(ip);
        this.userLoginRecordService.save(userLoginRecord);
        // 生成token
        String token = JwtUtils.createToken(one.getId(), one.getNickName());
        // 创建返回的userInfo对象, 同时设置token等信息
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(loginVO, userInfoVO, "password");
        userInfoVO.setName(one.getName());
        userInfoVO.setNickName(one.getNickName());
        userInfoVO.setToken(token);
        userInfoVO.setHeadImg(one.getHeadImg());

        return userInfoVO;

    }

    @Override
    public Page<UserInfo> getPageList(UserInfoQuery userInfoQuery, Integer page, Integer limit) {
        Page<UserInfo> userInfoPage = new Page<>(page, limit);
        LambdaQueryWrapper<UserInfo> lqw = new LambdaQueryWrapper<>();
        if(userInfoQuery == null){
            // 如果查询对象为空，返回全部数据
            return this.page(userInfoPage);
        }
        String mobile = userInfoQuery.getMobile();
        Integer userType = userInfoQuery.getUserType();
        Integer status = userInfoQuery.getStatus();
        // 组装查询条件
        lqw
                .eq(StringUtils.isNotBlank(mobile), UserInfo::getMobile, mobile)
                .eq(userType != null, UserInfo::getUserType, userType)
                .eq(status != null, UserInfo::getStatus, status);

        return this.page(userInfoPage, lqw);

    }

    @Override
    public void lock(Long id, Integer status) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(id);
        userInfo.setStatus(status);
        this.updateById(userInfo);
    }
}
