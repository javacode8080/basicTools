package com.example.exceltosql.entity.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author :sunjian23
 * @date : 2024/1/8 10:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionDto {
    private String errorCode = "0";
    private String errorMessage = "success";
    private List<PermissionData> data;

}
