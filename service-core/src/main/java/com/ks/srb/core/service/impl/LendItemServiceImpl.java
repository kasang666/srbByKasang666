package com.ks.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.common.exception.Assert;
import com.ks.common.result.ResponseEnum;
import com.ks.srb.core.hfb.FormHelper;
import com.ks.srb.core.hfb.HfbConst;
import com.ks.srb.core.hfb.RequestHelper;
import com.ks.srb.core.mapper.LendItemMapper;
import com.ks.srb.core.pojo.entity.Lend;
import com.ks.srb.core.pojo.entity.LendItem;
import com.ks.srb.core.pojo.entity.UserAccount;
import com.ks.srb.core.pojo.entity.UserInfo;
import com.ks.srb.core.pojo.enums.LendStatusEnum;
import com.ks.srb.core.pojo.enums.TransTypeEnum;
import com.ks.srb.core.pojo.vo.InvestVO;
import com.ks.srb.core.service.*;
import com.ks.srb.core.utils.LendNoUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 标的出借记录表 服务实现类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */

@Slf4j
@Service
public class LendItemServiceImpl extends ServiceImpl<LendItemMapper, LendItem> implements LendItemService {

    @Autowired
    private TransFlowService transFlowService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private UserBindService userBindService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private LendService lendService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String getFormStr(InvestVO investVO, Long userId) {
        // 参数校验
        Long lendId = investVO.getLendId();
        // 获取用户信息， 用户必须是投资人
        UserInfo userInfo = this.userInfoService.getById(userId);
        Integer userType = userInfo.getUserType();
        Assert.isTrue(userType == 1, ResponseEnum.USER_TYPE_ERROR);
        // 获取标的信息
        Lend lend = this.lendService.getById(lendId);
        // 标的的状态必须是募资中
        Assert.isTrue(Objects.equals(LendStatusEnum.INVEST_RUN.getStatus(), lend.getStatus()), ResponseEnum.LEND_INVEST_ERROR);
        //标的不能超卖：(已投金额 + 本次投资金额 )>=标的金额（超卖）
        BigDecimal sum = lend.getInvestAmount().add(new BigDecimal(investVO.getInvestAmount()));
        Assert.isTrue(sum.doubleValue() <= lend.getAmount().doubleValue(), ResponseEnum.LEND_FULL_SCALE_ERROR);
        // 投资金额不能少于最低投资金额，并且必须是100的整数倍
        Assert.isTrue(Double.parseDouble(investVO.getInvestAmount()) >= lend.getLowestAmount().doubleValue(), ResponseEnum.LOWEST_AMOUNT_ERROR);
        // 账户余额充足
        LambdaQueryWrapper<UserAccount> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserAccount::getUserId, userId);
        UserAccount userAccount = this.userAccountService.getOne(lqw);
        Assert.isTrue(userAccount.getAmount().doubleValue()>Double.parseDouble(investVO.getInvestAmount()), ResponseEnum.NOT_SUFFICIENT_FUNDS_ERROR);
        // 标的下的投资信息-----------------------------------------------------
        LendItem lendItem = new LendItem();
        lendItem.setInvestUserId(userId);//投资人id
        lendItem.setInvestName(investVO.getInvestName());//投资人名字
        String lendItemNo = LendNoUtils.getLendItemNo();
        lendItem.setLendItemNo(lendItemNo); //投资条目编号（一个Lend对应一个或多个LendItem）
        lendItem.setLendId(investVO.getLendId());//对应的标的id
        lendItem.setInvestAmount(new BigDecimal(investVO.getInvestAmount())); //此笔投资金额
        lendItem.setLendYearRate(lend.getLendYearRate());//年化
        lendItem.setInvestTime(LocalDateTime.now()); //投资时间
        lendItem.setLendStartDate(lend.getLendStartDate()); //开始时间
        lendItem.setLendEndDate(lend.getLendEndDate()); //结束时间
        // 预期收益
        BigDecimal interestCount = this.lendService.getInterestCount(new BigDecimal(investVO.getInvestAmount()), lend.getLendYearRate(), lend.getPeriod(), lend.getReturnMethod());
        lendItem.setExpectAmount(interestCount);
        // 实际收益
        lendItem.setRealAmount(new BigDecimal(0));
        // lendItem状态
        lendItem.setStatus(0);
        // save
        this.save(lendItem);
        //组装投资相关的参数，提交到汇付宝资金托管平台==========================================
        //在托管平台同步用户的投资信息，修改用户的账户资金信息==========================================
        //获取投资人的绑定协议号
        String investUserBindCode = this.userBindService.getBindCodeByUserId(userId);
        //获取借款人的绑定协议号
        String benefitUserBindCode = this.userBindService.getBindCodeByUserId(lend.getUserId());
        //封装提交至汇付宝的参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("voteBindCode", investUserBindCode);
        paramMap.put("benefitBindCode",benefitUserBindCode);
        paramMap.put("agentProjectCode", lend.getLendNo());//项目标号
        paramMap.put("agentProjectName", lend.getTitle());
        //在资金托管平台上的投资订单的唯一编号，要和lendItemNo保持一致。
        paramMap.put("agentBillNo", lendItemNo);//订单编号
        paramMap.put("voteAmt", investVO.getInvestAmount());
        paramMap.put("votePrizeAmt", "0");
        paramMap.put("voteFeeAmt", "0");
        paramMap.put("projectAmt", lend.getAmount()); //标的总金额
        paramMap.put("note", "");
        paramMap.put("notifyUrl", HfbConst.INVEST_NOTIFY_URL); //检查常量是否正确
        paramMap.put("returnUrl", HfbConst.INVEST_RETURN_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);
        //构建充值自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.INVEST_URL, paramMap);
        return formStr;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void notifyForHfb(Map<String, Object> paramMap) {
        log.info("用户投资异步回调");
        //获取投资编号
        String agentBillNo = (String)paramMap.get("agentBillNo");
        if(this.transFlowService.transFlowExists(agentBillNo)){
            log.warn("幂等性返回");
            return;
        }
        //获取用户的绑定协议号
        String bindCode = (String)paramMap.get("voteBindCode");
        String voteAmt = (String)paramMap.get("voteAmt");
        //修改商户系统中的用户账户金额：余额、冻结金额
        this.userAccountService.updateAccount(bindCode, new BigDecimal("-" + voteAmt), new BigDecimal(voteAmt));
        //修改投资记录的投资状态改为已支付
        LendItem lendItem = this.getByLendItemNo(agentBillNo);
        lendItem.setStatus(1);//已支付
        baseMapper.updateById(lendItem);
        //修改标的信息：投资人数、已投金额
        Long lendId = lendItem.getLendId();
        Lend lend = this.lendService.getById(lendId);
        lend.setInvestNum(lend.getInvestNum() + 1);
        lend.setInvestAmount(lend.getInvestAmount().add(lendItem.getInvestAmount()));
        this.lendService.updateById(lend);
        //新增交易流水
        LambdaQueryWrapper<UserInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserInfo::getBindCode, bindCode);
        UserInfo userInfo = this.userInfoService.getOne(lqw);
        this.transFlowService.saveTransFlow(agentBillNo, userInfo, lendItem.getInvestAmount(), TransTypeEnum.INVEST_LOCK);
    }

    @Override
    public LendItem getByLendItemNo(String agentBillNo) {
        LambdaQueryWrapper<LendItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(LendItem::getLendItemNo, agentBillNo);
        LendItem lendItem = this.getOne(lqw);
        return lendItem;
    }

    @Override
    public List<LendItem> selectByLendId(Long lendId) {
        QueryWrapper<LendItem> queryWrapper = new QueryWrapper();
        queryWrapper.eq("lend_id", lendId);
        List<LendItem> lendItemList = baseMapper.selectList(queryWrapper);
        return lendItemList;
    }
}
