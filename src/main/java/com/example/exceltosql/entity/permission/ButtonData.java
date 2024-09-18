package com.example.exceltosql.entity.permission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author :sunjian23
 * @date : 2024/1/8 10:27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ButtonData {

    private String id;
    private String name;
    private String parentId;
    private String type;
    private String url;
}
