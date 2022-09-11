package com.ks.srb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ks.srb.core.pojo.entity.TransFlow;
import com.ks.srb.core.pojo.entity.UserInfo;
import com.ks.srb.core.pojo.enums.TransTypeEnum;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 交易流水表 服务类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
public interface TransFlowService extends IService<TransFlow> {

    boolean transFlowExists(String agentBillNo);

    void saveTransFlow(String agentBillNo, UserInfo userInfo, BigDecimal chargeAmt, TransTypeEnum transTypeEnum);

    List<TransFlow> selectByUserId(Long userId);
}
