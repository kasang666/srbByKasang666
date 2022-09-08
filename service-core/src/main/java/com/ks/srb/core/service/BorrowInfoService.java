package com.ks.srb.core.service;

import com.ks.srb.core.pojo.entity.BorrowInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ks.srb.core.pojo.vo.BorrowInfoApprovalVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 借款信息表 服务类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
public interface BorrowInfoService extends IService<BorrowInfo> {

    BigDecimal getBorrowAmount(Long userId);

    void saveBorrowInfo(BorrowInfo borrowInfo, Long userId);

    Integer getBorrowInfoStatus(Long userId);

    List<BorrowInfo> getBorrowInfoList();

    Map<String, Object> getBorrowInfoDetail(Long id);

    void saveApprovalInfo(BorrowInfoApprovalVO borrowInfoApprovalVO);
}
