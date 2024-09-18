package com.example.exceltosql.controller.api;

import com.example.exceltosql.entity.permission.PermissionData;
import com.example.exceltosql.entity.permission.PermissionDto;
import com.example.exceltosql.service.permission.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author :sunjian23
 * @date : 2024/1/8 10:06
 */
@Controller
@RequestMapping("/Index")
public class IndexController {

    @Autowired
    private PermissionService permissionService;

    @PostMapping("/getPermission")
    @ResponseBody
    public PermissionDto getPermission() {
        PermissionDto permissionDto = new PermissionDto();
        try {
            List<PermissionData> permissions = permissionService.getPermissions();
            permissionDto.setData(permissions);
        } catch (Exception e) {
            e.printStackTrace();
            permissionDto.setErrorCode("0x00000001");
            permissionDto.setErrorMessage("获取许可资源失败");
        }
        return permissionDto;
    }
}
