package com.ks.srb.core.service;

import com.ks.srb.core.pojo.entity.UserBind;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ks.srb.core.pojo.vo.UserBindVO;

import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */


public interface UserBindService extends IService<UserBind> {

    String getFormStr(UserBindVO userBindVO, Long userId);


    void myNotify(Map<String, Object> paramMap);

    String getBindCodeByUserId(Long userId);
}
