package com.ks.srb.core.service.impl;

import com.ks.common.exception.Assert;
import com.ks.common.result.ResponseEnum;
import com.ks.srb.core.pojo.entity.Borrower;
import com.ks.srb.core.mapper.BorrowerMapper;
import com.ks.srb.core.pojo.entity.BorrowerAttach;
import com.ks.srb.core.pojo.entity.UserInfo;
import com.ks.srb.core.pojo.enums.BorrowerStatusEnum;
import com.ks.srb.core.pojo.vo.BorrowerVO;
import com.ks.srb.core.service.BorrowerAttachService;
import com.ks.srb.core.service.BorrowerService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.srb.core.service.UserInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    private UserInfoService userInfoService;

    @Autowired
    private BorrowerAttachService borrowerAttachService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveBorrowerInfo(BorrowerVO borrowerVO, Long userId) {
        UserInfo userInfo = this.userInfoService.getById(userId);
        Integer borrowAuthStatus = userInfo.getBorrowAuthStatus();
        // 如果借款人的borrowAuthStatus已存在，证明借款人已提交信息，这次是重复提交， 然后可以获取到图片附件的链接，将oss里面的图片删除
        Assert.isNull(borrowAuthStatus, ResponseEnum.FORM_SUBMIT);
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
}
