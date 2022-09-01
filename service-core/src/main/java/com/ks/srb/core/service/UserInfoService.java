package com.ks.srb.core.service;

import com.ks.srb.core.pojo.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ks.srb.core.pojo.vo.RegisterVO;

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
}
