package com.example.exceltosql.entity;

import com.example.exceltosql.interfaces.ExcelResource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author :sunjian23
 * @date : 2022/11/4 15:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DuplicateKeyBean {
    //key
    @ExcelResource(value = "原有key")
    private String oldKey;

    //key
    @ExcelResource(value = "转化后的key")
    private String newKey;

    //中文
    @ExcelResource(value = "中文（简体）")
    private String zh_CN;
}
