package com.ks.srb.core.service;

import com.ks.srb.core.pojo.entity.Lend;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标的准备表 服务类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
public interface LendService extends IService<Lend> {

    List<Lend> getList();

    Map<String, Object> getDetailById(Long id);
}
