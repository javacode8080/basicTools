package com.example.exceltosql.service.permission.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.exceltosql.dao.permission.PermissionDao;
import com.example.exceltosql.entity.permission.ButtonData;
import com.example.exceltosql.entity.permission.PermissionBean;
import com.example.exceltosql.entity.permission.PermissionData;
import com.example.exceltosql.service.permission.PermissionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :sunjian23
 * @date : 2024/1/8 10:54
 */
@Service
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private PermissionDao permissionDao;

    @Override
    public List<PermissionData> getPermissions() {
        //1.数据库查询
        List<PermissionBean> permissions = permissionDao.getPermissions();
        //2.将数据字典项转化为实体类层级
        List<PermissionData> permissionDataList = dataProcessing(permissions);
        //3.递归树生成
        List<PermissionData> buildByRecursive = buildByRecursive(permissionDataList);
        return buildByRecursive;
    }

    public List<PermissionData> buildByRecursive(List<PermissionData> permissionDataList) {
        List<PermissionData> trees = new ArrayList<>();
        for (PermissionData permissionData : permissionDataList) {
            // 判断当前是否为第一节点
            if (permissionData.getParentId().equals("0")) {
                trees.add(findChildren(permissionData, permissionDataList));
            }
        }
        return trees;
    }

    public PermissionData findChildren(PermissionData permissionData, List<PermissionData> permissionDataList) {
        for (PermissionData permissionData2 : permissionDataList) {
            if (permissionData.getId().equals(permissionData2.getParentId())) {
                if (permissionData.getChildren() == null) {
                    permissionData.setChildren(new ArrayList<PermissionData>());
                }
                //判断是否还有子节点，如果有的话继续向下遍历，否则直接返回
                permissionData.getChildren().add(findChildren(permissionData2, permissionDataList));
            }
        }
        return permissionData;
    }

    public List<PermissionData> dataProcessing(List<PermissionBean> permissions) {
        List<PermissionData> permissionDataList = new ArrayList<>();
        if (permissions != null) {
            for (PermissionBean permission : permissions) {
                PermissionData permissionData = new PermissionData();
                //1.复制基本结构
                BeanUtils.copyProperties(permission, permissionData);
                //2.转换buttonlist
                String buttonList = permission.getButtonList();
                // 将字符串解析为 JSON 数组
                List<ButtonData> buttonDataList = new ArrayList<>();
                if (StringUtils.isNotEmpty(buttonList)) {
                    JSONArray jsonArray = JSONArray.parseArray(buttonList);
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        ButtonData buttonData = JSONObject.parseObject(jsonObject.toJSONString(), ButtonData.class);
                        buttonDataList.add(buttonData);
                    }
                }
                permissionData.setButtonList(buttonDataList);
                permissionDataList.add(permissionData);
            }
        }
        return permissionDataList;
    }


}
