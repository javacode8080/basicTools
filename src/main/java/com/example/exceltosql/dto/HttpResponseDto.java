package com.example.exceltosql.dto;

import lombok.Data;

/**
 * @author :sunjian23
 * @date : 2022/10/19 9:52
 */
@Data
public class HttpResponseDto<T> {

    private String message;

    private T data;


    private String code;


    public static <T> HttpResponseDto<T> wapper(T t, String message, String code) {
        HttpResponseDto<T> result = new HttpResponseDto<>();
        result.setCode(code);
        result.setData(t);
        result.setMessage(message);
        return result;
    }

    public static <T> HttpResponseDto<T> success(T t) {
        HttpResponseDto<T> result = new HttpResponseDto<>();
        result.setData(t);
        result.setCode("200");
        result.setMessage("success");
        return result;
    }
}
