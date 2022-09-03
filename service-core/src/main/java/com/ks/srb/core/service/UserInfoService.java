package com.ks.srb.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ks.srb.core.pojo.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ks.srb.core.pojo.query.UserInfoQuery;
import com.ks.srb.core.pojo.vo.LoginVO;
import com.ks.srb.core.pojo.vo.RegisterVO;
import com.ks.srb.core.pojo.vo.UserInfoVO;

/**
 * <p>
 * 用户基本信息 服务类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */

public interface UserInfoService extends IService<UserInfo> {

    void register(RegisterVO registerVO);

    UserInfoVO login(LoginVO loginVO, String ip);

    Page<UserInfo> getPageList(UserInfoQuery userInfoQuery, Integer page, Integer limit);

    void lock(Long id, Integer status);

    boolean checkMobile(String mobile);
}
