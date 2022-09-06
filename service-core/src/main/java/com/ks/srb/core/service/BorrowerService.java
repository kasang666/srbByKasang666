package com.ks.srb.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ks.srb.core.pojo.entity.Borrower;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ks.srb.core.pojo.vo.BorrowerApprovalVO;
import com.ks.srb.core.pojo.vo.BorrowerDetailVO;
import com.ks.srb.core.pojo.vo.BorrowerVO;

/**
 * <p>
 * 借款人 服务类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
public interface BorrowerService extends IService<Borrower> {

    void saveBorrowerInfo(BorrowerVO borrowerVO, Long userId);

    Integer getBorrowerStatus(Long userId);

    Page<Borrower> pageList(Integer page, Integer limit, String keyword);

    BorrowerDetailVO showInfo(Long id);

    void approvalBorrowerApprovalVO(BorrowerApprovalVO borrowerApprovalVO);
}
