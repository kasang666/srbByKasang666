package com.ks.srb.core.controller.api;


import com.ks.common.result.R;
import com.ks.srb.core.pojo.entity.Lend;
import com.ks.srb.core.service.LendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 前端控制器
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */


@Api(tags = "标的管理")
@Slf4j
@RestController
@RequestMapping("/api/core/lend")
public class LendController {

    @Autowired
    private LendService lendService;

    @ApiOperation("标的列表")
    @GetMapping("/list")
    public R getList(){
        List<Lend> list = this.lendService.getList();
        return R.success().data("lendList", list);
    }

    @ApiOperation("获取标的详情")
    @GetMapping("/show/{lendId}")
    public R getDetail(@ApiParam(value = "标的id", required = true)
                       @PathVariable Long lendId){
        Map<String, Object> lendDetail = this.lendService.getDetailById(lendId);
        return R.success().data("lendDetail", lendDetail);
    }

    @ApiOperation("计算投资收益")
    @GetMapping("/getInterestCount/{invest}/{yearRate}/{totalMonth}/{returnMethod}")
    public R getInterestCount(@ApiParam(value = "投资金额", required = true)
                              @PathVariable("invest") BigDecimal invest,
                              @ApiParam(value = "年华利率", required = true)
                              @PathVariable("yearRate") BigDecimal yearRate,
                              @ApiParam(value = "总月数", required = true)
                              @PathVariable("totalMonth") Integer totalMonth,
                              @ApiParam(value = "还款方式", required = true)
                              @PathVariable("returnMethod") Integer returnMethod){
        BigDecimal interestCount = this.lendService.getInterestCount(invest, yearRate, totalMonth, returnMethod);
        return R.success().data("interestCount", interestCount);
    }
}

