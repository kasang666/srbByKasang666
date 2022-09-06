package com.ks.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.srb.core.mapper.BorrowerAttachMapper;
import com.ks.srb.core.pojo.entity.BorrowerAttach;
import com.ks.srb.core.pojo.vo.BorrowerAttachVO;
import com.ks.srb.core.service.BorrowerAttachService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 借款人上传资源表 服务实现类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
@Service
public class BorrowerAttachServiceImpl extends ServiceImpl<BorrowerAttachMapper, BorrowerAttach> implements BorrowerAttachService {

    @Override
    public List<BorrowerAttachVO> getBorrowerAttachVOList(Long id) {
        LambdaQueryWrapper<BorrowerAttach> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BorrowerAttach::getBorrowerId, id);
        List<BorrowerAttach> borrowerAttachList = this.list(lqw);
        List<BorrowerAttachVO> borrowerAttachVOList = new ArrayList<>(borrowerAttachList.size());
        borrowerAttachList.stream().forEach(item -> {
            BorrowerAttachVO borrowerAttachVO = new BorrowerAttachVO();
            borrowerAttachVO.setImageType(item.getImageType());
            borrowerAttachVO.setImageUrl(item.getImageUrl());
            borrowerAttachVOList.add(borrowerAttachVO);
        });
        return borrowerAttachVOList;
    }
}
