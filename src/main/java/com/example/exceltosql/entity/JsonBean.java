package com.example.exceltosql.entity;


import com.example.exceltosql.interfaces.ExcelResource;

/**
 * @author :sunjian23
 * @date : 2022/8/23 17:00
 */
public class JsonBean {

    //key
    @ExcelResource(value = "key")
    private String key;


    //中文
    @ExcelResource(value = "中文（简体）")
    private String zh_CN;

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getZh_CN() {
        return this.zh_CN;
    }

    public void setZh_CN(String zh_CN) {
        this.zh_CN = zh_CN;
    }

    public JsonBean(String key, String zh_CN) {
        this.key = key;
        this.zh_CN = zh_CN;
    }

    public JsonBean() {
    }
}
