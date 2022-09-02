package com.ks.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ks.srb.core.pojo.entity.UserLoginRecord;
import com.ks.srb.core.mapper.UserLoginRecordMapper;
import com.ks.srb.core.service.UserLoginRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Override
    public List<UserLoginRecord> getUserLoginRecordTop50(Long id) {
        LambdaQueryWrapper<UserLoginRecord> lqw = new LambdaQueryWrapper<>();
        lqw.eq(id != null, UserLoginRecord::getUserId, id);
        lqw.last("limit 50");
        return this.list(lqw);
    }
}
