package com.example.exceltosql.entity.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author :sunjian23
 * @date : 2024/1/8 15:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionBean {
    private String id;
    private String name;
    private String parentId;
    private String type;
    private String url;
    private String icon;
    private Boolean hidden = true;
    private String buttonList;
}
