package com.ks.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.common.exception.Assert;
import com.ks.common.result.ResponseEnum;
import com.ks.srb.core.mapper.BorrowInfoMapper;
import com.ks.srb.core.pojo.entity.BorrowInfo;
import com.ks.srb.core.pojo.entity.Borrower;
import com.ks.srb.core.pojo.entity.IntegralGrade;
import com.ks.srb.core.pojo.entity.UserInfo;
import com.ks.srb.core.pojo.enums.BorrowAuthEnum;
import com.ks.srb.core.pojo.enums.BorrowInfoStatusEnum;
import com.ks.srb.core.pojo.vo.BorrowerDetailVO;
import com.ks.srb.core.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
}
