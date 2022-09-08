package com.ks.srb.core.controller.api;


import com.alibaba.fastjson.JSON;
import com.ks.common.result.R;
import com.ks.srb.base.util.JwtUtils;
import com.ks.srb.core.hfb.RequestHelper;
import com.ks.srb.core.service.UserAccountService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

/**
 * <p>
 * 用户账户 前端控制器
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */


@Api(tags = "会员账户管理")
@Slf4j
@RestController
@RequestMapping("/api/core/userAccount")
public class UserAccountController {

    @Autowired
    private UserAccountService userAccountService;

    @ApiOperation("会员充值")
    @PostMapping("/auth/commitCharge/{chargeAmt}")
    public R commitCharge(@ApiParam(value = "充值金额", required = true)
                          @PathVariable BigDecimal chargeAmt,
                          HttpServletRequest request){
        String token = request.getHeader("token");
        Long userId = JwtUtils.getUserId(token);
        String formStr = this.userAccountService.getFormStr(chargeAmt, userId);
        return R.success().data("formStr", formStr);
    }

    @ApiOperation("hfb充值成功后的回调接口")
    @PostMapping("/notify")
    public String notifyForHfb(HttpServletRequest request){
        Map<String, Object> paramMap = RequestHelper.switchMap(request.getParameterMap());
        log.info("用户充值异步回调：" + JSON.toJSONString(paramMap));
        if(!RequestHelper.isSignEquals(paramMap)){
            log.info("用户充值异步回调签名错误：" + JSON.toJSONString(paramMap));
            return "failed";
        }
        // 签名正确后判断hfb是否充值成功，不管hfb是充值成功还是失败，都返回success
        if("0001".equals(paramMap.get("resultCode"))) {
            // 成功，修改会员账户月，并记录流水
            this.userAccountService.notifyForHfb(paramMap);
        } else {
            log.info("hfb充值异步回调充值失败：" + JSON.toJSONString(paramMap));
        }
        return "success";
    }

}

