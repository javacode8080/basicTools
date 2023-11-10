package com.example.exceltosql.enums;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author :sunjian23
 * @date : 2023/6/28 12:43
 */
public enum PackageInfoEnum {

    TYPE("包类型", "type", ""),
    ID("id", "id", ""),
    VERSION("版本", "version", ""),
    DISPLAYNAME_ZH("中文描述", "displayName.zh_CN", ""),
    DISPLAYNAME_EN("英文描述", "displayName.en_US", ""),
    COMPONENTID("组件标识", "component.id", ""),
    MINORVERSION("最低适配版本", "component.compatibility.minor.version", ""),
    MINORACTION("最低适配版本异常提醒级别", "component.compatibility.minor.action", "warn"),
    MAJORVERSION("最高适配版本", "component.compatibility.major.version", ""),
    MAJORACTION("最高适配版本异常提醒级别", "component.compatibility.major.action", "warn"),
    LANGUAGES("语言", "languages", ""),
    UNPACKDIR("解压目录", "unpack.dir", "./");


    public static List<Pair<String, String>> parameterMapToPackageInfoEnumList(Map<String, String[]> parameterMap) {
        ArrayList<Pair<String, String>> packageInfoEnums = new ArrayList<>();
        List<String> nameList = getNameList();
        nameList.forEach(name -> {
            if (null != parameterMap.get(name)) {
                String value = parameterMap.get(name)[0];
                PackageInfoEnum enumByName = getEnumByName(name);
                Pair<String, String> pair = null;
                if (StringUtils.isEmpty(value) && !StringUtils.isEmpty(enumByName.getValue())) {
                    pair = new Pair<>(enumByName.getAttribute(), enumByName.getValue());
                } else {
                    pair = new Pair<>(enumByName.getAttribute(), value);
                }
                packageInfoEnums.add(pair);
            }
        });
        return packageInfoEnums;
    }

    public String getName() {
        return name;
    }

    public String getAttribute() {
        return attribute;
    }

    public String getValue() {
        return value;
    }

    private String name;
    private String attribute;
    private String value;

    PackageInfoEnum(String name, String attribute, String value) {
        this.name = name;
        this.attribute = attribute;
        this.value = value;
    }

    public static PackageInfoEnum getEnumByName(String name) {
        for (PackageInfoEnum packageInfoEnum : PackageInfoEnum.values()) {
            if (packageInfoEnum.name.equals(name)) {
                return packageInfoEnum;
            }
        }
        return null;
    }

    public static List<String> getNameList() {
        List<String> list = new ArrayList<String>();
        for (PackageInfoEnum packageInfoEnum : PackageInfoEnum.values()) {
            list.add(packageInfoEnum.getName());
        }
        return list;
    }


}
