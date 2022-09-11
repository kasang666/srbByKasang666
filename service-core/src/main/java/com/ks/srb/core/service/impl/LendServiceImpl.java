package com.ks.srb.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.common.exception.BusinessException;
import com.ks.srb.core.hfb.HfbConst;
import com.ks.srb.core.hfb.RequestHelper;
import com.ks.srb.core.mapper.LendMapper;
import com.ks.srb.core.pojo.entity.*;
import com.ks.srb.core.pojo.enums.LendStatusEnum;
import com.ks.srb.core.pojo.enums.ReturnMethodEnum;
import com.ks.srb.core.pojo.enums.TransTypeEnum;
import com.ks.srb.core.pojo.vo.BorrowInfoApprovalVO;
import com.ks.srb.core.pojo.vo.BorrowerDetailVO;
import com.ks.srb.core.service.*;
import com.ks.srb.core.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 标的准备表 服务实现类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */

@Slf4j
@Service
public class LendServiceImpl extends ServiceImpl<LendMapper, Lend> implements LendService {

    @Autowired
    private LendItemReturnService lendItemReturnService;

    @Autowired
    private LendReturnService lendReturnService;

    @Autowired
    private TransFlowService transFlowService;

    @Autowired
    private LendItemService lendItemService;

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserInfoService userInfoService;

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

//    @Cacheable(value = "srb:core:lend:list")
    @Override
    public List<Lend> getList() {
        List<Lend> list = this.list();
        list.stream().forEach(lend -> {
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
        if (returnMethod.intValue() == ReturnMethodEnum.ONE.getMethod()) {
            interestCount = Amount1Helper.getInterestCount(invest, yearRate, totalMonth);
        } else if (returnMethod.intValue() == ReturnMethodEnum.TWO.getMethod()) {
            interestCount = Amount2Helper.getInterestCount(invest, yearRate, totalMonth);
        } else if (returnMethod.intValue() == ReturnMethodEnum.THREE.getMethod()) {
            interestCount = Amount3Helper.getInterestCount(invest, yearRate, totalMonth);
        } else {
            interestCount = Amount4Helper.getInterestCount(invest, yearRate, totalMonth);
        }
        return interestCount;
    }

    /**
     * 满标放款
     * @param id
     */
    @Override
    public void makeLoan(Long id) {
        Lend lend = this.getById(id);
        // 放款接口调用
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentProjectCode", lend.getLendNo());//标的编号
        String agentBillNo = LendNoUtils.getLoanNo();//放款编号
        paramMap.put("agentBillNo", agentBillNo);
        //平台收益，放款扣除，借款人借款实际金额=借款金额-平台收益
        //月年化
        BigDecimal monthRate = lend.getLendYearRate().divide(new BigDecimal(12), 8, BigDecimal.ROUND_DOWN);
        //平台实际收益 = 已投金额 * 月年化 * 标的期数
        BigDecimal realAmount = lend.getInvestAmount().multiply(monthRate).multiply(new BigDecimal(lend.getPeriod()));
        paramMap.put("mchFee", realAmount); //商户手续费(平台实际收益)
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        String sign = RequestHelper.getSign(paramMap);
        paramMap.put("sign", sign);
        // 记录放款日志
        log.info("放款参数：", JSON.toJSONString(paramMap));
        JSONObject response = RequestHelper.sendRequest(paramMap, HfbConst.MAKE_LOAD_URL);
        log.info("放款响应：", response.toJSONString());
        String resultCode = response.getString("resultCode");
        if(!"0000".equals(resultCode)){
            throw new BusinessException(response.getString("resultMsg"));
        }
        // 更新标的信息
        lend.setRealAmount(realAmount);
        lend.setPublishDate(LocalDateTime.now());
        lend.setStatus(LendStatusEnum.PAY_RUN.getStatus());
        this.updateById(lend);
        // 获取借款人信息
        Long userId = lend.getUserId();
        UserInfo userInfo = this.userInfoService.getById(userId);
        String bindCode = userInfo.getBindCode();
        // 给借款人转账
        BigDecimal total = new BigDecimal(response.getString("voteAmt"));
        this.userAccountService.updateAccount(bindCode, total, new BigDecimal(0));
        // 保存转账流水
        this.transFlowService.saveTransFlow(agentBillNo, userInfo, total, TransTypeEnum.BORROW_BACK);
        // 获取投资人列表
        LambdaQueryWrapper<LendItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(LendItem::getLendId, id).eq(LendItem::getStatus, 1);
        List<LendItem> lendItemList = this.lendItemService.list(lqw);
        // 遍历投资人列表，将冻结金额清空
        lendItemList.stream().forEach(lendItem -> {
            Long investUserId = lendItem.getInvestUserId();
            UserInfo investUserInfo = this.userInfoService.getById(investUserId);
            // 投资金额
            BigDecimal investAmount = lendItem.getInvestAmount();
            String investBindCode = investUserInfo.getBindCode();
            // 将冻结金额清空，冻结金额==投资金额
            this.userAccountService.updateAccount(investBindCode, new BigDecimal(0), investAmount.negate());
            // 新增交易流水
            this.transFlowService.saveTransFlow(LendNoUtils.getTransNo(),investUserInfo, investAmount, TransTypeEnum.INVEST_UNLOCK);
        });
        // 生成还款计划和回款计划
        this.repaymentPlan(lend);
    }

    /**
     * 生成还款计划
     * @param lend
     */
    private void repaymentPlan(Lend lend){
        //还款计划列表
        List<LendReturn> lendReturnList = new ArrayList<>();
        //按还款时间生成还款计划
        int len = lend.getPeriod().intValue();
        for (int i = 1; i <= len; i++) {
            //创建还款计划对象
            LendReturn lendReturn = new LendReturn();
            lendReturn.setReturnNo(LendNoUtils.getReturnNo());  // 还款对象流水
            lendReturn.setLendId(lend.getId());   // 标的id
            lendReturn.setBorrowInfoId(lend.getBorrowInfoId());  // 设置用户借款id
            lendReturn.setUserId(lend.getUserId());   // 用户id
            lendReturn.setAmount(lend.getAmount());   // 还款金额
            lendReturn.setBaseAmount(lend.getInvestAmount());   // 投资金额
            lendReturn.setLendYearRate(lend.getLendYearRate());  // 年化利率
            lendReturn.setCurrentPeriod(i);//当前期数
            lendReturn.setReturnMethod(lend.getReturnMethod());   // 还款方式
            //说明：还款计划中的这三项 = 回款计划中对应的这三项和：因此需要先生成对应的回款计划
//            lendReturn.setPrincipal();
//            lendReturn.setInterest();
//            lendReturn.setTotal();
            lendReturn.setFee(new BigDecimal(0));
            lendReturn.setReturnDate(lend.getLendStartDate().plusMonths(i)); //第二个月开始还款
            lendReturn.setOverdue(false);
            if (i == len) { //最后一个月
                //标识为最后一次还款
                lendReturn.setLast(true);
            } else {
                lendReturn.setLast(false);
            }
            lendReturn.setStatus(0);
            lendReturnList.add(lendReturn);
        }
        //批量保存
        this.lendReturnService.saveBatch(lendReturnList);
        //获取lendReturnList中还款期数与还款计划id对应map
        Map<Integer, Long> lendReturnMap = lendReturnList.stream().collect(
                Collectors.toMap(LendReturn::getCurrentPeriod, LendReturn::getId)
        );
        //======================================================
        //=============获取所有投资者，生成回款计划===================
        //======================================================
        //回款计划列表
        List<LendItemReturn> lendItemReturnAllList = new ArrayList<>();
        //获取投资成功的投资记录
        LambdaQueryWrapper<LendItem> lqw = new LambdaQueryWrapper<>();
        lqw.eq(LendItem::getLendId, lend.getId()).eq(LendItem::getStatus, 1);
        List<LendItem> lendItemList = this.lendItemService.list(lqw);
        for (LendItem lendItem : lendItemList) {
            //创建回款计划列表
            List<LendItemReturn> lendItemReturnList = this.returnInvest(lendItem.getId(), lendReturnMap, lend);
            lendItemReturnAllList.addAll(lendItemReturnList);
        }
        //更新还款计划中的相关金额数据
        for (LendReturn lendReturn : lendReturnList) {

            BigDecimal sumPrincipal = lendItemReturnAllList.stream()
                    //过滤条件：当回款计划中的还款计划id == 当前还款计划id的时候
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    //将所有回款计划中计算的每月应收本金相加
                    .map(LendItemReturn::getPrincipal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumInterest = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getInterest)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal sumTotal = lendItemReturnAllList.stream()
                    .filter(item -> item.getLendReturnId().longValue() == lendReturn.getId().longValue())
                    .map(LendItemReturn::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            lendReturn.setPrincipal(sumPrincipal); //每期还款本金
            lendReturn.setInterest(sumInterest); //每期还款利息
            lendReturn.setTotal(sumTotal); //每期还款本息
            lendReturnService.updateBatchById(lendReturnList);
        }
    }

    /**
     * 回款计划
     *
     * @param lendItemId
     * @param lendReturnMap 还款期数与还款计划id对应map
     * @param lend
     * @return
     */
    public List<LendItemReturn> returnInvest(Long lendItemId, Map<Integer, Long> lendReturnMap, Lend lend) {
        //投标信息
        LendItem lendItem = lendItemService.getById(lendItemId);
        //投资金额
        BigDecimal amount = lendItem.getInvestAmount();
        //年化利率
        BigDecimal yearRate = lendItem.getLendYearRate();
        //投资期数
        int totalMonth = lend.getPeriod();
        Map<Integer, BigDecimal> mapInterest = null;  //还款期数 -> 利息
        Map<Integer, BigDecimal> mapPrincipal = null; //还款期数 -> 本金
        //根据还款方式计算本金和利息
        if (lend.getReturnMethod().intValue() == ReturnMethodEnum.ONE.getMethod()) {
            //利息
            mapInterest = Amount1Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            //本金
            mapPrincipal = Amount1Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.TWO.getMethod()) {
            mapInterest = Amount2Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount2Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else if (lend.getReturnMethod().intValue() == ReturnMethodEnum.THREE.getMethod()) {
            mapInterest = Amount3Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount3Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        } else {
            mapInterest = Amount4Helper.getPerMonthInterest(amount, yearRate, totalMonth);
            mapPrincipal = Amount4Helper.getPerMonthPrincipal(amount, yearRate, totalMonth);
        }
        //创建回款计划列表
        List<LendItemReturn> lendItemReturnList = new ArrayList<>();
        for (Map.Entry<Integer, BigDecimal> entry : mapInterest.entrySet()) {
            Integer currentPeriod = entry.getKey();
            //根据还款期数获取还款计划的id
            Long lendReturnId = lendReturnMap.get(currentPeriod);
            LendItemReturn lendItemReturn = new LendItemReturn();
            lendItemReturn.setLendReturnId(lendReturnId);
            lendItemReturn.setLendItemId(lendItemId);
            lendItemReturn.setInvestUserId(lendItem.getInvestUserId());
            lendItemReturn.setLendId(lendItem.getLendId());
            lendItemReturn.setInvestAmount(lendItem.getInvestAmount());
            lendItemReturn.setLendYearRate(lend.getLendYearRate());
            lendItemReturn.setCurrentPeriod(currentPeriod);
            lendItemReturn.setReturnMethod(lend.getReturnMethod());
            //最后一次本金计算
            if (lendItemReturnList.size() > 0 && currentPeriod.intValue() == lend.getPeriod().intValue()) {
                //最后一期本金 = 本金 - 前几次之和
                BigDecimal sumPrincipal = lendItemReturnList.stream()
                        .map(LendItemReturn::getPrincipal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                //最后一期应还本金 = 用当前投资人的总投资金额 - 除了最后一期前面期数计算出来的所有的应还本金
                BigDecimal lastPrincipal = lendItem.getInvestAmount().subtract(sumPrincipal);
                lendItemReturn.setPrincipal(lastPrincipal);

                //最后一期利息 = 总利息 - 前几次之和
                BigDecimal sumInterest = lendItemReturnList.stream()
                        .map(LendItemReturn::getInterest)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                //最后一期应还利息 = 用当前投资人的总利息 - 除了最后一期前面期数计算出来的所有的应还利息
                BigDecimal lastInterest = lendItem.getExpectAmount().subtract(sumInterest);
                lendItemReturn.setInterest(lastInterest);
            } else {
                lendItemReturn.setPrincipal(mapPrincipal.get(currentPeriod));
                lendItemReturn.setInterest(mapInterest.get(currentPeriod));
            }
            lendItemReturn.setTotal(lendItemReturn.getPrincipal().add(lendItemReturn.getInterest()));
            lendItemReturn.setFee(new BigDecimal("0"));
            lendItemReturn.setReturnDate(lend.getLendStartDate().plusMonths(currentPeriod));
            //是否逾期，默认未逾期
            lendItemReturn.setOverdue(false);
            lendItemReturn.setStatus(0);
            lendItemReturnList.add(lendItemReturn);
        }
        this.lendItemReturnService.saveBatch(lendItemReturnList);
        return lendItemReturnList;
    }

    /**
     * 转换Lend参数
     *
     * @param lend
     */
    private void exchangeLend(Lend lend) {
        String returnMethod = dictService.getNameByParentDictCodeAndValue("returnMethod", lend.getReturnMethod());
        String status = LendStatusEnum.getMsgByStatus(lend.getStatus());
        lend.getParam().put("returnMethod", returnMethod);
        lend.getParam().put("status", status);
    }
}
