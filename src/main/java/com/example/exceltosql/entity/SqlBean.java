package com.example.exceltosql.entity;


import com.example.exceltosql.interfaces.ExcelResource;

/**
 * @author :sunjian23
 * @date : 2022/8/23 17:00
 */
public class SqlBean {

    //key
    @ExcelResource(value = "Key")
    private String key;

    //中文
    @ExcelResource(value = "现中文")
    private String zh_CN;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getZh_CN() {
        return zh_CN;
    }

    public void setZh_CN(String zh_CN) {
        this.zh_CN = zh_CN;
    }
}
