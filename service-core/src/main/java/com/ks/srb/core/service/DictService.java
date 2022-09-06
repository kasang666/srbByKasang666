package com.ks.srb.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ks.srb.core.pojo.dto.ExcelDictDTO;
import com.ks.srb.core.pojo.entity.Dict;

import java.io.InputStream;
import java.util.List;

/**
 * <p>
 * 数据字典 服务类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
public interface DictService extends IService<Dict> {

    void saveDataFromExcel(InputStream inputStream);

    List<ExcelDictDTO> getList();

    List<Dict> getByParentId(Long parentId);

    List<Dict> findByDictCode(String dictCode);

    String getNameByParentDictCodeAndValue(String dictCode, Integer value);
}
