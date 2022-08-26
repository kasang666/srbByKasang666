package com.ks.srb.core.listener;
// -*-coding:utf-8 -*-

/*
 * File       : ExcelDictDTOListener.java
 * Time       ：2022/8/25 9:33
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.ks.srb.core.mapper.DictMapper;
import com.ks.srb.core.pojo.dto.ExcelDictDTO;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@NoArgsConstructor
public class ExcelDictDTOListener extends AnalysisEventListener<ExcelDictDTO> {

    // 创建一个列表，防止多次访问数据库或者最后一次性访问数据库数据量过大
    /**
     * 每隔5条存储数据库，实际使用中可以3000条，然后清理list ，方便内存回收
     */
    private List<ExcelDictDTO> list = new ArrayList<>();
    // 每次达到这个数量的数据，就像数据库提交数据
    private final Integer MAX_BATCH_COUNT = 5;

    private DictMapper dictMapper;

    // 由于ExcelDictDTOListener没有交给spring管理，所以需要使用这种方式去实现Mapper的注入
    public ExcelDictDTOListener(DictMapper dictMapper){
        this.dictMapper = dictMapper;
    }

    @Override
    public void invoke(ExcelDictDTO excelDictDTO, AnalysisContext analysisContext) {
        this.list.add(excelDictDTO);
        if (this.list.size() >= this.MAX_BATCH_COUNT){
            this.saveData();
            log.info("数据存入完成！");
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        this.saveData();
    }

    public void saveData(){
        this.dictMapper.saveBatch(this.list);
        log.info("{}条数据存储到数据库", this.list.size());
        this.list.clear();
    }

}
