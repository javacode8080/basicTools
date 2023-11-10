package com.example.exceltosql.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author :sunjian23
 * @date : 2022/10/28 13:43
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum BusinessTypeEnum {
    EXCELTODATABASECREATEANDDATAINSERT("1", "1.根据Excel生成数据库文件"),
    TEMPNEWTABLEINSERTSQL("2", "2.刷库：temp_newtable数据库更新"),
    EXCELTOJSON("3", "3.根据Excel文件生成JSON进行受控"),
    LANGUAGETRANSLATIONMATCH("4", "4.匹配小语种翻译"),
    CONVERTKEYSANDFILTERDUPLICATES("5", "5.转化key并过滤重复项"),
    PROPERTIESWRITEINEXCEL("6", "6.properties文件按规则写入Excel(主要用于Hirule平台生成的产品描述包转化为Hido上可以上传的Excel文件)"),
    TSTOEXCEL("7", "7.ts文件转为Excel文件供翻译组翻译(英文)"),
    EXCELTOTS("8", "8.Excel文件英文翻译替换ts文件中文"),
    REMOVEBLANKTAGS("9", "9.去除Ts文件中的空白<translation>标签"),
    GENERATEMETAINFFILE("10","10.压缩包校验文件生成"),
    COMPAREFILE("11", "11.对比压缩包文件异同"),
    YAMLEXCELINTERCHANGE("12", "12.YAML/EXCEL互转(用于artemis资源包翻译)"),
    ARTEMISREMOVEINTERFACEBYSERVICENAMEORGROUPNAME("13", "13.根据提供方服务名称/服务分组去除artemis相关接口"),
    GENERATELANGUAGEPACKSBASEDONEXCEL("14", "14.根据Excel生成语言包");
    private String businessType;
    private String BusinessName;

}
