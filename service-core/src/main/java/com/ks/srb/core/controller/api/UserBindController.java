package com.ks.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.ks.common.result.R;
import com.ks.srb.base.util.JwtUtils;
import com.ks.srb.core.hfb.RequestHelper;
import com.ks.srb.core.pojo.vo.UserBindVO;
import com.ks.srb.core.service.UserBindService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 前端控制器
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */

@Api(tags = "会员账号绑定管理")
@Slf4j
@RestController
@RequestMapping("/api/core/userBind")
public class UserBindController {
    @Autowired
    private UserBindService userBindService;

    @ApiOperation("获取跳转到hfb的链接")
    @PostMapping("/auth/bind")
    public R authBind(@ApiParam(value = "用户绑定对象", required = true)
                           @RequestBody UserBindVO userBindVO,
                           @ApiParam(value = "请求头对象", required = true)
                           HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = this.userBindService.getFormStr(userBindVO, userId);
        return R.success().data("formStr", formStr);
    }

    @ApiOperation("账户绑定异步回调")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request){
        Map<String, String[]> parameterMap = request.getParameterMap();
        Map<String, Object> paramMap = RequestHelper.switchMap(parameterMap);
        log.info("用户账号绑定异步回调：" + JSON.toJSONString(paramMap));
        if (!RequestHelper.isSignEquals(paramMap)){
            return "failed";
        }
        this.userBindService.myNotify(paramMap);
        return "success";
    }


}

