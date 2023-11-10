package com.example.exceltosql.Util;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author :sunjian23
 * @date : 2023/8/10 18:08
 */
public class FileCompareUtil {


    public static void unzip(String filePath, String unzipPath) throws IOException {
        //0.解压文件到指定目录
        File basePathDir = new File(unzipPath);
        if (!basePathDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            basePathDir.mkdirs();
        }
        ZipUtil.unzip(filePath, unzipPath + File.separator);
    }


    public static Map<String, String> getFilesMap(String rootPath, String dirName) {
        Map<String, String> resultMap = new HashMap<>();
        MD5Util.getFilesMD5Value(rootPath + File.separator + dirName, File.separator, resultMap);
        return resultMap;
    }

    public static Map<String, List<String>> compareFiles(Map<String, String> masterFileMap, Map<String, String> slaveFileMap) {

        List<String> lostFiles = new ArrayList<String>();
        List<String> addFiles = new ArrayList<String>();
        List<String> changeFiles = new ArrayList<String>();
        Iterator<Map.Entry<String, String>> iterator = masterFileMap.entrySet().iterator();
        //1.遍历文件
        while (iterator.hasNext()) {
            Map.Entry<String, String> next = iterator.next();
            String key = next.getKey();
            //判断从文件列表是否存在该文件，不存在则说明该文件为新增文件
            if (null != slaveFileMap.get(key)) {
                String masterMD5 = next.getValue();
                String slaveMD5 = slaveFileMap.get(key);
                if (!masterMD5.equals(slaveMD5)) {
                    //md5不同说明文件被修改
                    changeFiles.add(key);
                }
                slaveFileMap.remove(key);
            } else {
                addFiles.add(key);
            }
        }
        //2.剩余的为主文件不存在但是从文件列表存在的
        slaveFileMap.keySet().forEach(key -> lostFiles.add(key));
        //2-2.给每个list内容排序，方便阅读
        lostFiles.sort(String::compareTo);
        addFiles.sort(String::compareTo);
        changeFiles.sort(String::compareTo);
        //3.创建输出集
        Map<String, List<String>> resultMap = new HashMap<>();
        resultMap.put("修改的文件{"+changeFiles.size()+"}个", changeFiles);
        resultMap.put("新增的文件{"+addFiles.size()+"}个", addFiles);
        resultMap.put("缺失的文件{"+lostFiles.size()+"}个", lostFiles);
        return resultMap;
    }

}
