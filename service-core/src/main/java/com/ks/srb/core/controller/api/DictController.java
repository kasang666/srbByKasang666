package com.ks.srb.core.controller.api;


import com.ks.common.result.R;
import com.ks.srb.core.pojo.entity.Dict;
import com.ks.srb.core.service.DictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 数据字典 前端控制器
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */

@Api(tags = "字典数据")
@Slf4j
@RestController
@RequestMapping("/api/core/dict")
public class DictController {

    @Autowired
    private DictService dictService;

    @ApiOperation("字典数据获取")
    @GetMapping("/findByDictCode/{dictCode}")
    public R findByDictCode(@ApiParam(value = "需要查找的dictCode", required = true)
                            @PathVariable String dictCode){
        List<Dict> list = this.dictService.findByDictCode(dictCode);
        return R.success().data("dictList", list);
    }

}

