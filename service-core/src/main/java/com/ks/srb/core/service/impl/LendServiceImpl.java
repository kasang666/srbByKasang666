package com.ks.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.srb.core.mapper.LendMapper;
import com.ks.srb.core.pojo.entity.BorrowInfo;
import com.ks.srb.core.pojo.entity.Borrower;
import com.ks.srb.core.pojo.entity.Lend;
import com.ks.srb.core.pojo.enums.LendStatusEnum;
import com.ks.srb.core.pojo.enums.ReturnMethodEnum;
import com.ks.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.ks.srb.core.pojo.vo.BorrowerDetailVO;
import com.ks.srb.core.service.BorrowInfoService;
import com.ks.srb.core.service.BorrowerService;
import com.ks.srb.core.service.DictService;
import com.ks.srb.core.service.LendService;
import com.ks.srb.core.utils.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
@Service
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {

    @Resource
    private BorrowInfoService borrowInfoService;

    @Resource
    private BorrowerService borrowerService;

    @Resource
    private DictService dictService;


    @Override
    public void createLend(BorrowInfo borrowInfo, BorrowInfoApprovalVO borrowInfoApprovalVO) {
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
        //状态
        lend.setStatus(LendStatusEnum.INVEST_RUN.getStatus());
        // 审核时间
        lend.setCheckTime(LocalDateTime.now());
        // 审核人
        lend.setCheckAdminId(1l);
        // 保存
        this.save(lend);
    }

    @Cacheable(value = "srb:core:lend:list")
    @Override
    public List<Lend> getList() {
        List<Lend> list = this.list();
        list.stream().forEach(lend ->{
            exchangeLend(lend);
        });
        return list;
    }

    @Cacheable(value = "srb:core:lend", key = "#id")
    @Override
    public Map<String, Object> getDetailById(Long id) {
        Lend lend = this.getById(id);
        exchangeLend(lend);
        LambdaQueryWrapper<Borrower> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Borrower::getUserId, lend.getUserId());
        Borrower borrower = this.borrowerService.getOne(lqw);
        BorrowerDetailVO borrowerDetailVO = this.borrowerService.showInfo(borrower.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("lend", lend);
        map.put("borrower", borrowerDetailVO);
        return map;
    }

    @Override
    public BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalMonth, Integer returnMethod) {
        BigDecimal interestCount = null;
        if (returnMethod.intValue() == ReturnMethodEnum.ONE.getMethod()){
            interestCount =Amount1Helper.getInterestCount(invest, yearRate, totalMonth);
        }else if(returnMethod.intValue() == ReturnMethodEnum.TWO.getMethod()){
            interestCount =Amount2Helper.getInterestCount(invest, yearRate, totalMonth);
        }else if(returnMethod.intValue() == ReturnMethodEnum.THREE.getMethod()){
            interestCount =Amount3Helper.getInterestCount(invest, yearRate, totalMonth);
        }else{
            interestCount =Amount4Helper.getInterestCount(invest, yearRate, totalMonth);
        }
        return interestCount;
    }

    /**
     * 转换Lend参数
     * @param lend
     */
    private void exchangeLend(Lend lend) {
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
        String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
        lend.getParam().put("returnMethod", returnMethod);
        lend.getParam().put("status", status);
    }
}
