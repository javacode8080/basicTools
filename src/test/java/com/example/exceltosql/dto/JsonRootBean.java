package com.example.exceltosql.dto;

/**
 * @author :sunjian23
 * @date : 2023/12/11 18:19
 */
/**
 * Copyright 2023 json.cn
 */
public class JsonRootBean {

    private Data data;
    private String errorCode;
    private String code;
    public void setData(Data data) {
        this.data = data;
    }
    public Data getData() {
        return data;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    public String getErrorCode() {
        return errorCode;
    }

    public void setCode(String code) {
        this.code = code;
    }
    public String getCode() {
        return code;
    }

}
