package com.ks.srb.core.service;

import com.ks.srb.core.pojo.entity.LendItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ks.srb.core.pojo.vo.InvestVO;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 服务类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
public interface LendItemService extends IService<LendItem> {

    String getFormStr(InvestVO investVO, Long userId);

    void notifyForHfb(Map<String, Object> paramMap);

    LendItem getByLendItemNo(String agentBillNo);

    List<LendItem> selectByLendId(Long lendId);
}
