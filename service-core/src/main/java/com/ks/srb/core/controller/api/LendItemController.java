package com.ks.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.ks.common.result.R;
import com.ks.srb.base.util.JwtUtils;
import com.ks.srb.core.hfb.RequestHelper;
import com.ks.srb.core.pojo.entity.LendItem;
import com.ks.srb.core.pojo.vo.InvestVO;
import com.ks.srb.core.service.LendItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的出借记录表 前端控制器
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */

@Api(tags = "会员投资管理")
@Slf4j
@RestController
@RequestMapping("/api/core/lendItem")
public class LendItemController {

    @Autowired
    private LendItemService lendItemService;

    @ApiOperation("提交投资请求，返回表单")
    @PostMapping("/auth/commitInvest")
    public R commitInvest(@ApiParam(value = "投资参数对象", required = true)
                          @RequestBody InvestVO investVO,
                          HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = this.lendItemService.getFormStr(investVO, userId);
        return R.success().data("formStr", formStr);
    }

    @ApiOperation("投资接口回调")
    @PostMapping("/notify")
    public String notifyForHfb(HttpServletRequest request){
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        if(!RequestHelper.isSignEquals(paramMap)){
            return "failed";
        }
        if("0001".equals(paramMap.get("resultCode"))) {
            // 参数校验成功
            this.lendItemService.notifyForHfb(paramMap);
        } else {
            log.info("用户投资异步回调失败：" + JSON.toJSONString(paramMap));
        }
        return "success";
    }

    @ApiOperation("获取列表")
    @GetMapping("/list/{lendId}")
    public R list(
            @ApiParam(value = "标的id", required = true)
            @PathVariable Long lendId) {
        List<LendItem> list = lendItemService.selectByLendId(lendId);
        return R.success().data("list", list);
    }

}

