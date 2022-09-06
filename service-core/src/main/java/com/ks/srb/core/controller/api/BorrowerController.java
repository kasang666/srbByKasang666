package com.ks.srb.core.controller.api;


import com.ks.common.result.R;
import com.ks.srb.base.util.JwtUtils;
import com.ks.srb.core.pojo.vo.BorrowerVO;
import com.ks.srb.core.service.BorrowerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>
 * 借款人 前端控制器
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */

@Api(tags = "借款人提交信息路由")
@Slf4j
@RestController
@RequestMapping("/api/core/borrower")
public class BorrowerController {

    @Autowired
    private BorrowerService borrowerService;

    @ApiOperation("保存借款人申请额度信息")
    @PostMapping("/auth/save")
    public R saveBorrowerInfo(
            @ApiParam(value = "借款人信息", required = true)
            @RequestBody BorrowerVO borrowerVO,
            HttpServletRequest request){
        // 获取当前用户id
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        this.borrowerService.saveBorrowerInfo(borrowerVO, userId);
        return R.success().msg("信息提交成功！");
    }

    @ApiOperation("获取借款人申请额度结果")
    @GetMapping("/auth/getBorrowerStatus")
    public R getBorrowerStatus(HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        Integer borrowerStatus = this.borrowerService.getBorrowerStatus(userId);
        return R.success().data("borrowerStatus", borrowerStatus);
    }
}

