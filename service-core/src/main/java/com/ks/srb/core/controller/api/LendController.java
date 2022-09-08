package com.ks.srb.core.controller.api;


import com.ks.common.result.R;
import com.ks.srb.core.pojo.entity.Lend;
import com.ks.srb.core.service.LendService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

}

