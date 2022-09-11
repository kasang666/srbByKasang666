package com.ks.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.common.exception.Assert;
import com.ks.common.result.ResponseEnum;
import com.ks.srb.base.DTO.SmsDTO;
import com.ks.srb.core.hfb.FormHelper;
import com.ks.srb.core.hfb.HfbConst;
import com.ks.srb.core.hfb.RequestHelper;
import com.ks.srb.core.mapper.UserAccountMapper;
import com.ks.srb.core.pojo.entity.TransFlow;
import com.ks.srb.core.pojo.entity.UserAccount;
import com.ks.srb.core.pojo.entity.UserInfo;
import com.ks.srb.core.pojo.enums.TransTypeEnum;
import com.ks.srb.core.service.TransFlowService;
import com.ks.srb.core.service.UserAccountService;
import com.ks.srb.core.service.UserBindService;
import com.ks.srb.core.service.UserInfoService;
import com.ks.srb.core.utils.LendNoUtils;
import com.ks.srb.mq.service.MQService;
import com.ks.srb.mq.utils.MQConst;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户账户 服务实现类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
@Slf4j
@Service
public class UserAccountServiceImpl extends ServiceImpl<UserAccountMapper, UserAccount> implements UserAccountService {

    @Autowired
    private MQService mqService;

    @Autowired
    private UserBindService userBindService;

    @Autowired
    private TransFlowService transFlowService;

    @Autowired
    private UserInfoService userInfoService;

    @Override
    public String getFormStr(BigDecimal chargeAmt, Long userId) {
        UserInfo userInfo = this.userInfoService.getById(userId);
        // 如果用户不存在或者被锁定
        Assert.notNull(userInfo, ResponseEnum.LOGIN_MOBILE_ERROR);
        // 如果用户未绑定
        Assert.notNull(userInfo.getBindCode(), ResponseEnum.USER_NO_BIND_ERROR);
        // 构建表单参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getNo());
        paramMap.put("bindCode", userInfo.getBindCode());
        paramMap.put("chargeAmt", chargeAmt);
        paramMap.put("feeAmt", new BigDecimal("0"));
        paramMap.put("notifyUrl", HfbConst.RECHARGE_NOTIFY_URL);//检查常量是否正确
        paramMap.put("returnUrl", HfbConst.RECHARGE_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);
        // 获取formStr
        String formStr = FormHelper.buildForm(HfbConst.RECHARGE_URL, paramMap);
        return formStr;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notifyForHfb(Map<String, Object> paramMap) {
        String bindCode = (String) paramMap.get("bindCode");
        BigDecimal chargeAmt = new BigDecimal((String) paramMap.get("chargeAmt"));
        String agentBillNo = (String) paramMap.get("agentBillNo");
        // 冻结金额，现在先硬编码为0
        BigDecimal freezeAmount = new BigDecimal(0);
        // 获取流水单号，查看当前流水是否已经存在
        if(this.transFlowExists(agentBillNo)){
            return ;
        }
        // 流水号不存在，新增一个流水号
        LambdaQueryWrapper<UserInfo> userInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        userInfoLambdaQueryWrapper.eq(UserInfo::getBindCode,bindCode);
        UserInfo userInfo = this.userInfoService.getOne(userInfoLambdaQueryWrapper);
        // 创建流水对象
        this.transFlowService.saveTransFlow(agentBillNo, userInfo, chargeAmt, TransTypeEnum.RECHARGE);
        // 会员账户金额进行相应的操作
        this.baseMapper.updateAccount(bindCode, chargeAmt, freezeAmount);
        //发消息
        log.info("发消息");
        String mobile = userInfoService.getMobileByBindCode(bindCode);
        SmsDTO smsDTO = new SmsDTO();
        smsDTO.setMobile(mobile);
        smsDTO.setMessage("充值成功");
        mqService.sendMessage(MQConst.EXCHANGE_TOPIC_SMS, MQConst.ROUTING_SMS_ITEM, smsDTO);
    }

    @Override
    public BigDecimal getAccount(Long userId) {
        LambdaQueryWrapper<UserAccount> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserAccount::getUserId, userId);
        UserAccount userAccount = this.getOne(lqw);
        Assert.notNull(userAccount, ResponseEnum.LOGIN_AUTH_ERROR);
        return userAccount.getAmount();
    }

    @Override
    public void updateAccount(String bindCode, BigDecimal bigDecimal, BigDecimal bigDecimal1) {
        this.baseMapper.updateAccount(bindCode, bigDecimal, bigDecimal1);
    }

    @Override
    public String commitWithdraw(BigDecimal fetchAmt, Long userId) {
        //账户可用余额充足：当前用户的余额 >= 当前用户的提现金额
        BigDecimal amount = this.getAccount(userId);//获取当前用户的账户余额
        Assert.isTrue(amount.doubleValue() >= fetchAmt.doubleValue(),
                ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);
        String bindCode = this.userBindService.getBindCodeByUserId(userId);
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentBillNo", LendNoUtils.getWithdrawNo());
        paramMap.put("bindCode", bindCode);
        paramMap.put("fetchAmt", fetchAmt);
        paramMap.put("feeAmt", new BigDecimal(0));
        paramMap.put("notifyUrl", HfbConst.WITHDRAW_NOTIFY_URL);
        paramMap.put("returnUrl", HfbConst.WITHDRAW_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);
        //构建自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.WITHDRAW_URL, paramMap);
        return formStr;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notifyWithdraw(Map<String, Object> paramMap) {
        log.info("提现成功");
        String agentBillNo = (String)paramMap.get("agentBillNo");
        boolean result = transFlowService.transFlowExists(agentBillNo);
        if(result){
            log.warn("幂等性返回");
            return;
        }
        String bindCode = (String)paramMap.get("bindCode");
        String fetchAmt = (String)paramMap.get("fetchAmt");
        //根据用户账户修改账户金额
        baseMapper.updateAccount(bindCode, new BigDecimal("-" + fetchAmt), new BigDecimal(0));
        //增加交易流水
        LambdaQueryWrapper<UserInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserInfo::getBindCode, bindCode);
        UserInfo userInfo = this.userInfoService.getOne(lqw);
        transFlowService.saveTransFlow(agentBillNo, userInfo, new BigDecimal(fetchAmt), TransTypeEnum.WITHDRAW);
    }

    /**
     * 判断流水号是否存在
     * @param transFlowId
     * @return
     */
    public Boolean transFlowExists(String transFlowId){
        LambdaQueryWrapper<TransFlow> transFlowLambdaQueryWrapper = new LambdaQueryWrapper<>();
        transFlowLambdaQueryWrapper.eq(TransFlow::getTransNo, transFlowId);
        int count = this.transFlowService.count(transFlowLambdaQueryWrapper);
        // 如果存在证明已经进行了接口调用，结束调用
        return count > 0;
    }

}
