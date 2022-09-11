package com.ks.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.srb.core.mapper.TransFlowMapper;
import com.ks.srb.core.pojo.entity.TransFlow;
import com.ks.srb.core.pojo.entity.UserInfo;
import com.ks.srb.core.pojo.enums.TransTypeEnum;
import com.ks.srb.core.service.TransFlowService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 交易流水表 服务实现类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
@Service
public class TransFlowServiceImpl extends ServiceImpl<TransFlowMapper, TransFlow> implements TransFlowService {

    @Override
    public boolean transFlowExists(String agentBillNo) {
        LambdaQueryWrapper<TransFlow> lqw = new LambdaQueryWrapper<>();
        lqw.eq(TransFlow::getTransNo, agentBillNo);
        int count = this.count(lqw);
        return count > 0;
    }

    public void saveTransFlow(String agentBillNo, UserInfo userInfo, BigDecimal chargeAmt, TransTypeEnum transTypeEnum) {
        TransFlow transFlow = new TransFlow();
        transFlow.setUserId(userInfo.getId());
        transFlow.setUserName(userInfo.getName());
        transFlow.setTransNo(agentBillNo);
        transFlow.setTransType(transTypeEnum.getTransType());  // 当前接口为充值接口
        transFlow.setTransTypeName(transTypeEnum.getTransTypeName());   // 充值
        transFlow.setTransAmount(chargeAmt);
        transFlow.setMemo(transTypeEnum.getTransTypeName());
        this.save(transFlow);
    }

    @Override
    public List<TransFlow> selectByUserId(Long userId) {
        QueryWrapper<TransFlow> queryWrapper = new QueryWrapper<>();
        queryWrapper
                .eq("user_id", userId)
                .orderByDesc("id");
        return baseMapper.selectList(queryWrapper);
    }
}
