package com.example.exceltosql.Util;

import java.io.IOException;
import java.util.*;

/**
 * @author :sunjian23
 * @date : 2022/10/29 18:44
 */
public class TranslationMatchUtil {


    public static void translationMatch(String filePath, int i, Integer maxcolumnnumber, String uploadFilePath) throws IOException, IllegalAccessException {
        Map<String, List<LinkedHashMap<String, Object>>> map = ExcelUtil.ReadExcelByRC(filePath, i, maxcolumnnumber, true);
        List<LinkedHashMap<String, Object>> translation = map.get("译文记录");
        List<LinkedHashMap<String, Object>> hirule = map.get("Hirule文件未翻译字段");
        List<LinkedHashMap<String, Object>> mapping = map.get("语种标题映射关系");
        Map<String, String> mappingMaps = new HashMap<>();
        if (null == translation || null == hirule || null == mapping) {
            throw new RuntimeException("上传错误:请检查文件格式是否正确");
        }
        //1.先获取中文+ key的映射关系
        mapping.forEach(mappingMap -> {
            mappingMaps.put((String) mappingMap.get("Hirule未翻译字段语种标题"), (String) mappingMap.get("译文记录语种标题"));
        });
        if (null == mappingMaps || null == mappingMaps.get("key") || null == mappingMaps.get("中文（简体）") || "".equals(mappingMaps.get("key")) || "".equals(mappingMaps.get("中文（简体）"))) {
            throw new RuntimeException("上传错误:key/中文的匹配映射缺失");
        }
        //2.循环遍历进行内容的匹配
        hirule.stream().forEach(hirlueMap -> {
            //2.1获取当前hirlueMap的中文+key
            String hiruleKey = (String) hirlueMap.get("key");
            String hiruleZh = (String) hirlueMap.get("中文（简体）");
            if (null == hiruleKey || "".equals(hiruleKey) || null == hiruleZh || "".equals(hiruleZh)) {
                throw new RuntimeException("文件错误:Hirule文件未翻译sheet页中存在key/中文缺失的数据行");
            }
            translation.stream().forEach(translationMap -> {
                //2.1找到中文+key匹配的translationMap
                String translationkey = (String) translationMap.get(mappingMaps.get("key"));
                String translationZh = (String) translationMap.get(mappingMaps.get("中文（简体）"));
                if (hiruleKey.equals(translationkey) && hiruleZh.equals(translationZh)) {
                    //2.2对其他所有的字段进行匹配
                    Set<String> hiruleKeySet = hirlueMap.keySet();
                    Iterator<String> hiruleKeyIterator = hiruleKeySet.iterator();
                    while (hiruleKeyIterator.hasNext()) {
                        String hKey = hiruleKeyIterator.next();
                        if (!"key".equals(hKey) && !"中文（简体）".equals(hKey)) {
                            String tKey = mappingMaps.get(hKey);
                            if (null != tKey && !"".equals(tKey)) {
                                //进行匹配
                                String tValue = (String) translationMap.get(tKey);
                                if (null != tValue && !"".equals(tValue)) {
                                    hirlueMap.put(hKey, tValue);
                                }
                            }
                        }

                    }
                }
            });

        });

        //写出文件到Excel中
        Map<String, List<LinkedHashMap<String, Object>>> excelMap = new HashMap();
        List<String> sheetList = new ArrayList<>();
        excelMap.put("未翻译字段", hirule);
        excelMap.put("确认翻译修改", null);
        excelMap.put("术语管理", null);
        excelMap.put("术语相关图片", null);
        sheetList.add("未翻译字段");
        sheetList.add("确认翻译修改");
        sheetList.add("术语管理");
        sheetList.add("术语相关图片");
        //写出文件
        ExcelUtil.writeExcel(uploadFilePath, excelMap, sheetList);


    }
}
