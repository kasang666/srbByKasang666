package com.ks.srb.core.service.impl;

import com.ks.srb.core.pojo.entity.UserAccount;
import com.ks.srb.core.mapper.UserAccountMapper;
import com.ks.srb.core.service.UserAccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

}
