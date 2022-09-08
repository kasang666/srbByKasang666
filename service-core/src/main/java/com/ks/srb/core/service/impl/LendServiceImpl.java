package com.ks.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.srb.core.mapper.LendMapper;
import com.ks.srb.core.pojo.entity.Borrower;
import com.ks.srb.core.pojo.entity.Lend;
import com.ks.srb.core.pojo.enums.LendStatusEnum;
import com.ks.srb.core.pojo.vo.BorrowerDetailVO;
import com.ks.srb.core.service.BorrowInfoService;
import com.ks.srb.core.service.BorrowerService;
import com.ks.srb.core.service.DictService;
import com.ks.srb.core.service.LendService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
