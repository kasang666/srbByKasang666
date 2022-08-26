package com.ks.srb.core.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ks.srb.core.listener.ExcelDictDTOListener;
import com.ks.srb.core.mapper.DictMapper;
import com.ks.srb.core.pojo.dto.ExcelDictDTO;
import com.ks.srb.core.pojo.entity.Dict;
import com.ks.srb.core.service.DictService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 数据字典 服务实现类
 * </p>
 *
 * @author kasang
 * @since 2022-08-20
 */
@Slf4j
@Service
public class DictServiceImpl extends ServiceImpl<DictMapper, Dict> implements DictService {

    @Transactional(rollbackFor = {Exception.class})  // 一旦有任意的异常被捕获就回滚
    @Override
    public void saveDataFromExcel(InputStream inputStream) {
        EasyExcel.read(inputStream, ExcelDictDTO.class, new ExcelDictDTOListener(this.baseMapper)).sheet().doRead();
    }

    @Override
    public List<ExcelDictDTO> getList() {
        List<Dict> dictList = this.baseMapper.selectList(null);
        List<ExcelDictDTO> excelDictDTOList = new ArrayList<>(dictList.size());   // 创建指定大小的list
        dictList.stream().forEach(item -> {
            ExcelDictDTO excelDictDTO = new ExcelDictDTO();
            BeanUtils.copyProperties(item, excelDictDTO);
            excelDictDTOList.add(excelDictDTO);
        });
        return excelDictDTOList;
    }

    /**
     * @CacheEvict 通常需要手动的删除时使用，比如删除，更新
     * @CachePut 通常需要手动添加时使用
     * value: 表示某一类
     * key: 表示具体的内用，value和key合起来才可以确定一个数据
     * condition: 条件成立才进行缓存， 但是无法获取到result属性
     * unless: 条件不成立才进行缓存
     * @param parentId
     * @return
     */
    @Cacheable(value = "srb:core:dict", key = "#parentId", unless = "#result == null")
    @Override
    public List<Dict> getByParentId(Long parentId) {
        LambdaQueryWrapper<Dict> lqw = new LambdaQueryWrapper<>();
        lqw.eq(parentId != null, Dict::getParentId, parentId);
        List<Dict> dictList = this.baseMapper.selectList(lqw);
        dictList.stream().forEach(item->{
            Boolean hasChildren = this.hasChildren(item.getId());
            item.setHasChildren(hasChildren);
        });
        return dictList;
    }

    /**
     * 辅助方法，判断当前字典数据是否有孩子节点
     * @param id
     * @return
     */
    private Boolean hasChildren(Long id){
        LambdaQueryWrapper<Dict> lqw = new LambdaQueryWrapper<>();
        lqw.eq(id != null, Dict::getParentId, id);
        List<Dict> dictList = this.baseMapper.selectList(lqw);
        return dictList.size() > 0;
    }
}
