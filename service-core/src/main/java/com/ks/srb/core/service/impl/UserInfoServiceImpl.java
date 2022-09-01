package com.ks.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.common.exception.BusinessException;
import com.ks.common.result.ResponseEnum;
import com.ks.srb.core.mapper.UserInfoMapper;
import com.ks.srb.core.pojo.entity.UserAccount;
import com.ks.srb.core.pojo.entity.UserInfo;
import com.ks.srb.core.pojo.vo.RegisterVO;
import com.ks.srb.core.service.UserAccountService;
import com.ks.srb.core.service.UserInfoService;
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
}
