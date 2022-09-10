package com.ks.srb.core.service;

import com.ks.srb.core.pojo.entity.BorrowInfo;
import com.ks.srb.core.pojo.entity.Lend;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ks.srb.core.pojo.vo.BorrowInfoApprovalVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 服务类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
public interface LendService extends IService<Lend> {

    void createLend(BorrowInfo borrowInfo, BorrowInfoApprovalVO borrowInfoApprovalVO);

    List<Lend> getList();

    Map<String, Object> getDetailById(Long id);

    BigDecimal getInterestCount(BigDecimal invest, BigDecimal yearRate, Integer totalMonth, Integer returnMethod);
}
