package com.ks.srb.core.service.impl;

import com.ks.srb.core.pojo.entity.UserLoginRecord;
import com.ks.srb.core.mapper.UserLoginRecordMapper;
import com.ks.srb.core.service.UserLoginRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户登录记录表 服务实现类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
@Service
public class UserLoginRecordServiceImpl extends ServiceImpl<UserLoginRecordMapper, UserLoginRecord> implements UserLoginRecordService {

}
