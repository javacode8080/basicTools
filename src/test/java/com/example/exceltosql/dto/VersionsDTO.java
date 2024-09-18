package com.example.exceltosql.dto;

import lombok.Data;

import java.util.List;

/**
 * @author :sunjian23
 * @date : 2023/12/11 16:10
 */
@Data
public class VersionsDTO {
    String code;
    List<String> data;
    String errorCode;
}
