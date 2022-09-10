package com.ks.srb.core.service;

import com.ks.srb.core.pojo.entity.UserAccount;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
public interface UserAccountService extends IService<UserAccount> {

    String getFormStr(BigDecimal chargeAmt, Long userId);

    void notifyForHfb(Map<String, Object> paramMap);

    BigDecimal getAccount(Long userId);
}
