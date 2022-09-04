package com.ks.srb.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.common.exception.BusinessException;
import com.ks.common.result.ResponseEnum;
import com.ks.srb.core.hfb.FormHelper;
import com.ks.srb.core.hfb.HfbConst;
import com.ks.srb.core.hfb.RequestHelper;
import com.ks.srb.core.mapper.UserBindMapper;
import com.ks.srb.core.mapper.UserInfoMapper;
import com.ks.srb.core.pojo.entity.UserBind;
import com.ks.srb.core.pojo.entity.UserInfo;
import com.ks.srb.core.pojo.enums.UserBindEnum;
import com.ks.srb.core.pojo.vo.UserBindVO;
import com.ks.srb.core.service.UserBindService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户绑定表 服务实现类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
@Service
public class UserBindServiceImpl extends ServiceImpl<UserBindMapper, UserBind> implements UserBindService {

    @Resource
    private UserInfoMapper userInfoMapper;

    @Transactional(rollbackFor = {Exception.class})
    @Override
    public String getFormStr(UserBindVO userBindVO, Long userId) {
        // 校验参数是否完整
        String name = userBindVO.getName();
        String mobile = userBindVO.getMobile();
        String idCard = userBindVO.getIdCard();
        String bankNo = userBindVO.getBankNo();
        String bankType = userBindVO.getBankType();
        // 校验参数是否合法
//        if (StringUtils.isBlank(mobile) || !idCard.matches("(^\\d{15}$)|(^\\d{18}$)|(^\\d{17}(\\d|X|x)$)")){
//            throw new BusinessException(ResponseEnum.WEIXIN_FETCH_USERINFO_ERROR);
//        }

        // 身份证是否被注册
        LambdaQueryWrapper<UserBind> _lqw = new LambdaQueryWrapper<>();
        _lqw.eq(UserBind::getIdCard, idCard).ne(UserBind::getUserId, userId);
        UserBind _userBind = this.getOne(_lqw);
        if(_userBind != null){
            // 身份证已被注册！
            throw new BusinessException(ResponseEnum.USER_BIND_IDCARD_EXIST_ERROR);
        }
        // 手机号是否已被绑定？

        // 用户是否填写过表单
        LambdaQueryWrapper<UserBind> lqw = new LambdaQueryWrapper<>();
        lqw.eq(UserBind::getUserId, userId);
        UserBind userBind = this.getOne(lqw);
        if (userBind != null) {
            // 如果取出的bingCode有值，证明当前用户已完成注册, 用户已存在
            if(userBind.getBindCode() != null){
                throw new BusinessException(ResponseEnum.USER_BIND_EXIST_ERROR);
            }
            // 如果查询到的数据不为空，证明用户之前填写过表单，只是没有提交到第三方平台
            BeanUtils.copyProperties(userBindVO, userBind);
            this.updateById(userBind);
        } else {
            //为空，则创建对象，赋值，存储
            userBind = new UserBind();
            BeanUtils.copyProperties(userBindVO, userBind);
            userBind.setUserId(userId);
            this.save(userBind);
        }
        // 生成返回的表单字符串
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("agentId", HfbConst.AGENT_ID);
        paramMap.put("agentUserId", userId);
        paramMap.put("idCard", userBindVO.getIdCard());
        paramMap.put("personalName", userBindVO.getName());
        paramMap.put("bankType", userBindVO.getBankType());
        paramMap.put("bankNo", userBindVO.getBankNo());
        paramMap.put("mobile", userBindVO.getMobile());
        paramMap.put("returnUrl", HfbConst.USERBIND_RETURN_URL);
        paramMap.put("notifyUrl", HfbConst.USERBIND_NOTIFY_URL);
        paramMap.put("timestamp", RequestHelper.getTimestamp());
        paramMap.put("sign", RequestHelper.getSign(paramMap));
        //构建充值自动提交表单
        String formStr = FormHelper.buildForm(HfbConst.USERBIND_URL, paramMap);
        return formStr;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void myNotify(Map<String, Object> paramMap) {
        String bindCode = (String)paramMap.get("bindCode");
        //会员id
        String agentUserId = (String)paramMap.get("agentUserId");
        //根据user_id查询user_bind记录
        QueryWrapper<UserBind> userBindQueryWrapper = new QueryWrapper<>();
        userBindQueryWrapper.eq("user_id", agentUserId);

        //更新用户绑定表
        UserBind userBind = baseMapper.selectOne(userBindQueryWrapper);
        userBind.setBindCode(bindCode);
        userBind.setStatus(UserBindEnum.BIND_OK.getStatus());
        baseMapper.updateById(userBind);

        //更新用户表
        UserInfo userInfo = userInfoMapper.selectById(agentUserId);
        userInfo.setBindCode(bindCode);
        userInfo.setName(userBind.getName());
        userInfo.setIdCard(userBind.getIdCard());
        userInfo.setBindStatus(UserBindEnum.BIND_OK.getStatus());
        userInfoMapper.updateById(userInfo);
    }


}
