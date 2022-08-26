package com.ks.srb.core.mapper;

import com.ks.srb.core.pojo.dto.ExcelDictDTO;
import com.ks.srb.core.pojo.entity.Dict;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 数据字典 Mapper 接口
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
public interface DictMapper extends BaseMapper<Dict> {

    void saveBatch(List<ExcelDictDTO> list);
}
