package com.example.exceltosql.dao.permission;

import com.example.exceltosql.entity.permission.PermissionBean;
import lombok.Data;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author :sunjian23
 * @date : 2024/1/8 15:58
 */
@Mapper
public interface PermissionDao {
    public List<PermissionBean> getPermissions();
}
