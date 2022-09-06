package com.ks.srb.core.controller.admin;
// -*-coding:utf-8 -*-

/*
 * File       : AdminBorrowerController.java
 * Time       ：2022/9/5 22:19
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ks.common.result.R;
import com.ks.srb.core.pojo.entity.Borrower;
import com.ks.srb.core.pojo.vo.BorrowerApprovalVO;
import com.ks.srb.core.pojo.vo.BorrowerDetailVO;
import com.ks.srb.core.service.BorrowerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@Api(tags = "会员信息管理")
@Slf4j
@RestController
@RequestMapping("/admin/core/borrower")
public class AdminBorrowerController {

    @Autowired
    private BorrowerService borrowerService;

    @ApiOperation("获取会员列表")
    @GetMapping("/list/{page}/{limit}")
    public R pageList(@ApiParam(value = "当前页码", required = true)
                      @PathVariable Integer page,
                      @ApiParam(value = "每页数量", required = true)
                      @PathVariable Integer limit,
                      @ApiParam(value = "查询参数" , required = true)
                      @RequestParam("keyword") String keyword) {
        Page<Borrower> pageModel = this.borrowerService.pageList(page, limit, keyword);
        return R.success().data("pageModel", pageModel);
    }

    @ApiOperation("显示会员申请额度信息")
    @GetMapping("/show/{id}")
    public R showBorrowerInfo(@ApiParam(value = "会员id", required = true)
                              @PathVariable Long id){
        BorrowerDetailVO borrowerDetailVO = this.borrowerService.showInfo(id);
        return R.success().data("borrowerDetailVO", borrowerDetailVO);

    }

    @ApiOperation("提交审核结果")
    @PostMapping("/approval")
    public R approvalBorrowerApprovalVO(@ApiParam(value = "额度审核结果对象", required = true)
                                      @RequestBody BorrowerApprovalVO borrowerApprovalVO){
        this.borrowerService.approvalBorrowerApprovalVO(borrowerApprovalVO);
        return R.success().msg("审核成功！");
    }
}
