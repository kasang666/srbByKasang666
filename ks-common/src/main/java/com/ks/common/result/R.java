package com.ks.common.result;
// -*-coding:utf-8 -*-

/*
 * File       : R.java
 * Time       ：2022/8/21 9:34
 * Author     ：hhs
 * version    ：java8
 * Description：
 */

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class R {

    private Integer code;
    private String msg;
    private Map<String, Object> data = new HashMap<>();

    /**
     * 构造器私有化，因为不想外界通过构造方法创建对象
     */
    private R(){}

    public static R success(){
        R r = new R();
        r.setCode(ResponseEnum.SUCCESS.getCode());
        r.setMsg(ResponseEnum.SUCCESS.getMsg());
        return r;
    }

    public static R error(){
        R r = new R();
        r.setCode(ResponseEnum.ERROR.getCode());
        r.setMsg(ResponseEnum.ERROR.getMsg());
        return r;
    }

    public static R setResult(ResponseEnum responseEnum){
        R r = new R();
        r.setCode(responseEnum.getCode());
        r.setMsg(responseEnum.getMsg());
        return r;
    }

    public R code(Integer code){
        this.setCode(code);
        return this;
    }

    public R msg(String msg){
        this.setMsg(msg);
        return this;
    }

    public R data(String key, Object val){
        this.data.put(key, val);
        return this;
    }

    public R data(Map<String, Object> data){
        this.setData(data);
        return this;
    }

}
