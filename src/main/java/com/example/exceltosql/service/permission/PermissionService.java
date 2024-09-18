package com.example.exceltosql.service.permission;

import com.example.exceltosql.entity.permission.PermissionData;

import java.util.List;

/**
 * @author :sunjian23
 * @date : 2024/1/8 14:04
 */
public interface PermissionService {
    public List<PermissionData> getPermissions();
}

