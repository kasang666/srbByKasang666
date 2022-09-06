package com.ks.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.common.exception.Assert;
import com.ks.common.result.ResponseEnum;
import com.ks.srb.core.mapper.BorrowerMapper;
import com.ks.srb.core.pojo.entity.Borrower;
import com.ks.srb.core.pojo.entity.BorrowerAttach;
import com.ks.srb.core.pojo.entity.UserInfo;
import com.ks.srb.core.pojo.entity.UserIntegral;
import com.ks.srb.core.pojo.enums.BorrowerStatusEnum;
import com.ks.srb.core.pojo.enums.IntegralEnum;
import com.ks.srb.core.pojo.vo.BorrowerApprovalVO;
import com.ks.srb.core.pojo.vo.BorrowerAttachVO;
import com.ks.srb.core.pojo.vo.BorrowerDetailVO;
import com.ks.srb.core.pojo.vo.BorrowerVO;
import com.ks.srb.core.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款人 服务实现类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
@Service
public class BorrowerServiceImpl extends ServiceImpl<BorrowerMapper, Borrower> implements BorrowerService {

    @Autowired
    private UserIntegralService userIntegralService;

    @Autowired
    private DictService dictService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private BorrowerAttachService borrowerAttachService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBorrowerInfo(BorrowerVO borrowerVO, Long userId) {
        UserInfo userInfo = this.userInfoService.getById(userId);
        String bindCode = userInfo.getBindCode();
        // 用户信息未绑定或者审核还未通过
        Assert.notNull(bindCode, ResponseEnum.USER_NO_BIND_ERROR);
        Integer borrowAuthStatus = userInfo.getBorrowAuthStatus();
        // 如果借款人的borrowAuthStatus已存在，证明借款人已提交额度申请信息，这次是重复提交， 然后可以获取到图片附件的链接，将oss里面的图片删除
        Assert.equals(borrowAuthStatus, 0, ResponseEnum.FORM_SUBMIT);
        // 保存借款人信息
        Borrower borrower = new Borrower();
        BeanUtils.copyProperties(borrowerVO, borrower);
        borrower.setUserId(userId);
        borrower.setName(userInfo.getName());
        borrower.setMobile(userInfo.getMobile());
        borrower.setIdCard(userInfo.getIdCard());
        borrower.setStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        this.save(borrower);
        // 保存图片附件
        List<BorrowerAttach> borrowerAttachList = borrowerVO.getBorrowerAttachList();
        borrowerAttachList.stream().forEach(item -> {
            item.setBorrowerId(borrower.getId());
            this.borrowerAttachService.save(item);
        });
        // 更新会员状态为认证中
        userInfo.setBorrowAuthStatus(BorrowerStatusEnum.AUTH_RUN.getStatus());
        this.userInfoService.updateById(userInfo);
    }

    @Override
    public Integer getBorrowerStatus(Long userId) {
        LambdaQueryWrapper<Borrower> lqw = new LambdaQueryWrapper<>();
        lqw.select(Borrower::getStatus).eq(Borrower::getUserId, userId);
        Map<String, Object> map = this.getMap(lqw);
        if (map == null){
            return BorrowerStatusEnum.NO_AUTH.getStatus();
        }
        Integer status = (Integer)map.get("status");
        return status;
    }

    @Override
    public Page<Borrower> pageList(Integer page, Integer limit, String keyword) {
        Page<Borrower> borrowerPage = new Page<>();
        if (StringUtils.isBlank(keyword)){
            this.page(borrowerPage);
            return borrowerPage;
        }
        LambdaQueryWrapper<Borrower> lqw = new LambdaQueryWrapper<>();
        lqw
                .like(Borrower::getMobile, keyword)
                .or()
                .like(Borrower::getName, keyword)
                .or()
                .like(Borrower::getIdCard, keyword)
                .orderByAsc(Borrower::getId);
        this.page(borrowerPage, lqw);
        return borrowerPage;
    }

    @Cacheable(value = "srb:core:borrower", key = "#id")
    @Override
    public BorrowerDetailVO showInfo(Long id) {
        Borrower borrower = this.getById(id);
        BorrowerDetailVO borrowerDetailVO = new BorrowerDetailVO();
        BeanUtils.copyProperties(borrower, borrowerDetailVO);
        // 设置性别
        borrowerDetailVO.setSex(borrower.getSex() == 1? "男": "女");
        // 设置婚否
        borrowerDetailVO.setMarry(borrower.getMarry()? "是": "否");
        //计算下拉列表选中内容
        String education = dictService.getNameByParentDictCodeAndValue("education", borrower.getEducation());
        String industry = dictService.getNameByParentDictCodeAndValue("industry", borrower.getIndustry());
        String income = dictService.getNameByParentDictCodeAndValue("income", borrower.getIncome());
        String returnSource = dictService.getNameByParentDictCodeAndValue("returnSource", borrower.getReturnSource());
        String contactsRelation = dictService.getNameByParentDictCodeAndValue("relation", borrower.getContactsRelation());

        //设置下拉列表选中内容
        borrowerDetailVO.setEducation(education);
        borrowerDetailVO.setIndustry(industry);
        borrowerDetailVO.setIncome(income);
        borrowerDetailVO.setReturnSource(returnSource);
        borrowerDetailVO.setContactsRelation(contactsRelation);

        //审批状态
        String status = BorrowerStatusEnum.getMsgByStatus(borrower.getStatus());
        borrowerDetailVO.setStatus(status);

        //获取附件VO列表
        List<BorrowerAttachVO> borrowerAttachVOList =  this.borrowerAttachService.getBorrowerAttachVOList(id);
        borrowerDetailVO.setBorrowerAttachVOList(borrowerAttachVOList);

        return borrowerDetailVO;

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approvalBorrowerApprovalVO(BorrowerApprovalVO borrowerApprovalVO) {
        // 获取用户审核对象信息
        Borrower borrower = this.getById(borrowerApprovalVO.getBorrowerId());
        // 更改审核状态
        borrower.setStatus(borrowerApprovalVO.getStatus());
        // 提交状态修改
        this.updateById(borrower);
        // 根据userid获取userinfo对象，然后修改积分数据
        UserInfo userInfo = this.userInfoService.getById(borrower.getUserId());
        // 然后更改审核状态
        userInfo.setBorrowAuthStatus(borrowerApprovalVO.getStatus());
        // 提交状态修改
        this.userInfoService.updateById(userInfo);
        // 如果是未通过。后面的操作都不执行
        if(borrowerApprovalVO.getStatus() == -1){
            return ;
        }
        // 获取当前积分
        Integer currentIntegral = borrowerApprovalVO.getInfoIntegral();
        //保存基础积分
        UserIntegral userIntegral = new UserIntegral();
        userIntegral.setUserId(userInfo.getId());
        userIntegral.setIntegral(currentIntegral);
        userIntegral.setContent("借款人基本积分信息");
        this.userIntegralService.save(userIntegral);
        // 分别对每项积分都进行存储，然后加和
        if (borrowerApprovalVO.getIsCarOk()){
            currentIntegral += IntegralEnum.BORROWER_CAR.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userInfo.getId());
            userIntegral.setIntegral(IntegralEnum.BORROWER_CAR.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_CAR.getMsg());
            this.userIntegralService.save(userIntegral);
        }
        if (borrowerApprovalVO.getIsHouseOk()){
            currentIntegral += IntegralEnum.BORROWER_HOUSE.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userInfo.getId());
            userIntegral.setIntegral(IntegralEnum.BORROWER_HOUSE.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_HOUSE.getMsg());
            this.userIntegralService.save(userIntegral);
        }
        if (borrowerApprovalVO.getIsIdCardOk()){
            currentIntegral += IntegralEnum.BORROWER_IDCARD.getIntegral();
            userIntegral = new UserIntegral();
            userIntegral.setUserId(userInfo.getId());
            userIntegral.setIntegral(IntegralEnum.BORROWER_IDCARD.getIntegral());
            userIntegral.setContent(IntegralEnum.BORROWER_IDCARD.getMsg());
            this.userIntegralService.save(userIntegral);
        }
        // 用户信息对象更改积分
        userInfo.setIntegral(currentIntegral);
        // 保存积分修改
        this.userInfoService.updateById(userInfo);
    }


}
