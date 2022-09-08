package com.ks.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.common.exception.Assert;
import com.ks.common.result.ResponseEnum;
import com.ks.srb.core.mapper.BorrowInfoMapper;
import com.ks.srb.core.pojo.entity.*;
import com.ks.srb.core.pojo.enums.BorrowAuthEnum;
import com.ks.srb.core.pojo.enums.BorrowInfoStatusEnum;
import com.ks.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.ks.srb.core.pojo.vo.BorrowerDetailVO;
import com.ks.srb.core.service.*;
import com.ks.srb.core.utils.LendNoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 借款信息表 服务实现类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
@Service
public class BorrowInfoServiceImpl extends ServiceImpl<BorrowInfoMapper, BorrowInfo> implements BorrowInfoService {
    @Autowired
    private LendService lendService;

    @Autowired
    private DictService dictService;

    @Autowired
    private IntegralGradeService integralGradeService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private BorrowerService borrowerService;

    @Override
    public BigDecimal getBorrowAmount(Long userId) {
        UserInfo userInfo = this.userInfoService.getById(userId);
        // 获取用户积分，然后根据积分获取借款额度
        Integer integral = userInfo.getIntegral();
        LambdaQueryWrapper<IntegralGrade> lqw = new LambdaQueryWrapper<>();
        lqw
                .le(IntegralGrade::getIntegralStart, integral)
                .ge(IntegralGrade::getIntegralEnd, integral);
        IntegralGrade integralGrade = this.integralGradeService.getOne(lqw);
        if (integralGrade == null) {
            return new BigDecimal("0");
        }
        return integralGrade.getBorrowAmount();
    }

    @Override
    public void saveBorrowInfo(BorrowInfo borrowInfo, Long userId) {
        // 判断用户是否已经提交了借款申请
        LambdaQueryWrapper<BorrowInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BorrowInfo::getUserId, userId);
        BorrowInfo one = this.getOne(lqw);
        Assert.isNull(one, ResponseEnum.FORM_SUBMIT);
        // 获取用户信息
        UserInfo userInfo = this.userInfoService.getById(userId);
        String bindCode = userInfo.getBindCode();
        // bindCode如果为空，证明用户未完成身份认证
        Assert.notNull(bindCode, ResponseEnum.USER_NO_BIND_ERROR);
        // 获取用户的额度申请信息是否完成审批
        Integer borrowAuthStatus = userInfo.getBorrowAuthStatus();
        // 用户信息未审核
        Assert.equals(borrowAuthStatus, BorrowAuthEnum.AUTH_OK.getStatus(), ResponseEnum.USER_NO_AMOUNT_ERROR);
        // 判断借款额度是否足够
        Assert.isTrue(this.getBorrowAmount(userId).doubleValue() >= borrowInfo.getAmount().doubleValue(), ResponseEnum.USER_AMOUNT_LESS_ERROR);
        // 保存数据
        borrowInfo.setUserId(userId);
        //百分比转成小数
        borrowInfo.setBorrowYearRate( borrowInfo.getBorrowYearRate().divide(new BigDecimal(100)));
        borrowInfo.setStatus(BorrowInfoStatusEnum.CHECK_RUN.getStatus());
        this.save(borrowInfo);
    }

    @Override
    public Integer getBorrowInfoStatus(Long userId) {
        LambdaQueryWrapper<BorrowInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BorrowInfo::getUserId, userId);
        BorrowInfo borrowInfo = this.getOne(lqw);
        // 如果borrowInfo == null， 证明用户还没有提交借款申请
        if (borrowInfo == null){
            return new Integer(0);
        }
        return borrowInfo.getStatus();
    }

    @Override
    public List<BorrowInfo> getBorrowInfoList() {
        List<BorrowInfo> borrowInfoList = this.baseMapper.getBorrowInfoList();
        borrowInfoList.stream().forEach(item -> {
            exchangeBorrowInfo(item);
        });
        return borrowInfoList;
    }

    /**
     * 转换borrowInfo里面的moneyUse, returnMethod等信息
     * @param item
     */
    private void exchangeBorrowInfo(BorrowInfo item) {
        String moneyUse = this.dictService.getNameByParentDictCodeAndValue("moneyUse", item.getMoneyUse());
        String returnMethod = this.dictService.getNameByParentDictCodeAndValue("returnMethod", item.getReturnMethod());
        String status = BorrowInfoStatusEnum.getMsgByStatus(item.getStatus());
        item.getParam().put("moneyUse", moneyUse);
        item.getParam().put("returnMethod", returnMethod);
        item.getParam().put("status", status);
    }

    @Override
    public Map<String, Object> getBorrowInfoDetail(Long id) {
        BorrowInfo borrowInfo = this.getById(id);
        // 获取borrowInfo且不为空
        Assert.notNull(borrowInfo, ResponseEnum.ERROR);
        exchangeBorrowInfo(borrowInfo);
        // 获取borrower的id
        Long userId = borrowInfo.getUserId();
        LambdaQueryWrapper<Borrower> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Borrower::getUserId, userId);
        Borrower borrower = this.borrowerService.getOne(lqw);
        BorrowerDetailVO borrowerDetailVO = this.borrowerService.showInfo(borrower.getId());
        HashMap<String, Object> map = new HashMap<>();
        map.put("borrowInfo", borrowInfo);
        map.put("borrower", borrowerDetailVO);
        return map;
    }

    /**
     * 保存审核信息，更改借款申请的状态
     * @param borrowInfoApprovalVO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveApprovalInfo(BorrowInfoApprovalVO borrowInfoApprovalVO) {
        Long id = borrowInfoApprovalVO.getId();
        BorrowInfo borrowInfo = this.getById(id);
        Integer status = borrowInfo.getStatus();
        // 如果审核状态不是待审核，证明已经审核过了
        Assert.isTrue(Objects.equals(status, BorrowInfoStatusEnum.CHECK_RUN.getStatus()), ResponseEnum.FORM_SUBMIT);
        borrowInfo.setStatus(borrowInfoApprovalVO.getStatus());
        this.updateById(borrowInfo);
        // 如果审核通过，根据BorrowInfoApprovalVO的信息创建一个新的标的对象，并保存到数据库
        if (Objects.equals(borrowInfoApprovalVO.getStatus(), BorrowInfoStatusEnum.CHECK_OK.getStatus())){
            // 创建新的标的
            Lend lend = new Lend();
            lend.setUserId(borrowInfo.getUserId());
            lend.setBorrowInfoId(borrowInfo.getId());
            lend.setLendNo(LendNoUtils.getLendNo());//生成编号
            lend.setTitle(borrowInfoApprovalVO.getTitle());
            lend.setAmount(borrowInfo.getAmount());
            lend.setPeriod(borrowInfo.getPeriod());
            lend.setLendYearRate(borrowInfoApprovalVO.getLendYearRate().divide(new BigDecimal(100)));//从审批对象中获取
            lend.setServiceRate(borrowInfoApprovalVO.getServiceRate().divide(new BigDecimal(100)));//从审批对象中获取
            lend.setReturnMethod(borrowInfo.getReturnMethod());
            lend.setLowestAmount(new BigDecimal(100));  // 标的最小投资金额
            lend.setInvestAmount(new BigDecimal(0));    // 当前投资金额
            lend.setInvestNum(0);
            lend.setPublishDate(LocalDateTime.now());       // 设置发布日期
            // 起息日期
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyy-MM-dd");
            LocalDate lendStartDate = LocalDate.parse(borrowInfoApprovalVO.getLendStartDate(), dtf);
            lend.setLendStartDate(lendStartDate);
            //结束日期
            LocalDate lendEndDate = lendStartDate.plusMonths(borrowInfo.getPeriod());
            lend.setLendEndDate(lendEndDate);
            lend.setLendInfo(borrowInfoApprovalVO.getLendInfo());//描述
            // 平台预估收益
            // 月化
            BigDecimal monthRate = lend.getServiceRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);// 保留8位小数，向下取整
            // 预估收益
            BigDecimal expectAmount = lend.getAmount().multiply(monthRate).multiply(new BigDecimal(borrowInfo.getPeriod()));
            lend.setExpectAmount(expectAmount);
            // 实际收益
            lend.setRealAmount(new BigDecimal(0));
            // 审核时间
            lend.setCheckTime(LocalDateTime.now());
            // 审核人
            lend.setCheckAdminId(1l);
            // 保存
            this.lendService.save(lend);
        }
    }
}
