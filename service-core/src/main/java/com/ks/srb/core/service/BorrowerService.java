package com.ks.srb.core.service;

import com.ks.srb.core.pojo.entity.Borrower;
import com.baomidou.mybatisplus.extension.service.IService;
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
}
