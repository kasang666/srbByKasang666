package com.ks.srb.core.controller.admin;


import com.ks.common.result.R;
import com.ks.srb.core.pojo.entity.IntegralGrade;
import com.ks.srb.core.service.IntegralGradeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 积分等级表 前端控制器
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
@Api(tags = "积分等级管理")  // 默认是类名的驼峰变杠admin-integral-grade-controller
@CrossOrigin
@RestController
@RequestMapping("admin/core/integralGrade")
public class AdminIntegralGradeController {

    @Resource
    private IntegralGradeService integralGradeService;

    @ApiOperation(value = "积分等级列表", notes = "获取积分等级列表")  // value是说明， notes详细说明
    @GetMapping("/list")
    public R getList(){
        List<IntegralGrade> list = this.integralGradeService.list();
        return R.success().data("list", list);
    }

    @ApiOperation(value = "根据id查询")
    @GetMapping("/get/{id}")
    public R getById(
            @ApiParam(value = "对象id", required = true)
            @PathVariable Integer id){
        IntegralGrade integralGrade = this.integralGradeService.getById(id);
        return integralGrade != null ? R.success().data("record", integralGrade): R.error();
    }

    @ApiOperation(value = "删除积分等级", notes = "逻辑删除")
    @DeleteMapping("/remove/{id}")
    public R deleteById(
            @ApiParam(value = "积分id", example = "3", required = true)  // 参数说明
            @PathVariable Integer id){
        boolean result = this.integralGradeService.removeById(id);
        return result ? R.success() : R.error();
    }

    @ApiOperation(value = "修改数据", notes = "修改数据")
    @PutMapping("/update")
    public R update(
            @ApiParam(value = "数据对象", required = true)
            @RequestBody IntegralGrade integralGrade){
        boolean result = this.integralGradeService.updateById(integralGrade);
        return result ? R.success() : R.error();
    }

    @ApiOperation(value = "添加数据", notes = "添加数据")
    @PostMapping("/save")
    public R save(
            @ApiParam(value = "数据对象", required = true)
            @RequestBody IntegralGrade integralGrade){
        boolean result = this.integralGradeService.save(integralGrade);
        return result ? R.success() : R.error();
    }


}

