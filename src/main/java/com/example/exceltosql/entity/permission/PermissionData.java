package com.example.exceltosql.entity.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author :sunjian23
 * @date : 2024/1/8 10:23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionData {

    private String id;
    private String name;
    private String parentId;
    private String type;
    private String url;
    private String icon;
    private Boolean hidden = true;
    private List<ButtonData> buttonList;
    private List<PermissionData> children;

}
