package com.example.exceltosql.Util;

import org.apache.commons.codec.binary.Hex;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MD5Util {

    private static final String FILE_CHECKSUM_FILE = "file_checksum.xml";

    /**
     * 生成META-INF下面除了 file_checksum.xml 以外的文件的MD5
     *
     * @param path
     * @param md5MapList
     */
    public static void generateFilesMD5Value(String path, String parentFilePath, List<Map<String, String>> md5MapList) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    generateFilesMD5Value(files[i].getPath(), parentFilePath + files[i].getName() + File.separator, md5MapList);
                } else {
                    String filePath = files[i].getPath();
                    if (!filePath.contains(FILE_CHECKSUM_FILE)) {
                        File singleFile = new File(filePath);
                        String fileSize = singleFile.length() + "";
                        String md5 = MD5Util.getMD5(singleFile);
                        // 组装信息
                        Map<String, String> md5Map = buildMd5Map(parentFilePath + singleFile.getName(), fileSize, md5);
                        md5MapList.add(md5Map);
                    }
                }
            }
        } else {
            if (!path.contains(FILE_CHECKSUM_FILE)) {
                File singleFile = new File(path);
                String fileSize = singleFile.length() + "";
                String md5 = MD5Util.getMD5(singleFile);

                // 组装信息
                Map<String, String> md5Map = buildMd5Map(path, fileSize, md5);
                md5MapList.add(md5Map);
            }

        }
    }



    /**
     * @param path:
     * @param parentFilePath:
     * @param md5Map:
     * @return void
     * @author sunjian23
     * @description TODO:获取所有文件的相对路径+md5码
     * @date 2023/8/11 8:54
     */
    public static void getFilesMD5Value(String path, String parentFilePath,Map<String, String> md5Map) {
        File file = new File(path);
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    getFilesMD5Value(files[i].getPath(), parentFilePath + files[i].getName() + File.separator, md5Map);
                } else {
                    String filePath = files[i].getPath();
                    File singleFile = new File(filePath);
                    String md5 = MD5Util.getMD5(singleFile);
                    // 组装信息
                    md5Map.put(parentFilePath + singleFile.getName(), md5);
                }
            }
        } else {
            if (!path.contains(FILE_CHECKSUM_FILE)) {
                File singleFile = new File(path);
                String md5 = MD5Util.getMD5(singleFile);
                // 组装信息
                md5Map.put(parentFilePath + singleFile.getName(), md5);
            }

        }
    }

    /**
     * 组装文件的MD5
     *
     * @param filePath
     * @param fileSize
     * @param md5
     * @return
     */
    private static Map<String, String> buildMd5Map(String filePath, String fileSize, String md5) {
        Map<String, String> map = new HashMap<>();
        map.put("filePath", filePath);
        map.put("fileSize", fileSize);
        map.put("md5", md5);
        return map;
    }


    /**
     * 获取一个文件的md5值(可处理大文件)
     *
     * @return md5 value
     */
    public static String getMD5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");

            byte[] buffer = new byte[8192];
            int length;
            while ((length = fileInputStream.read(buffer)) != -1) {
                MD5.update(buffer, 0, length);
            }
            return new String(Hex.encodeHex(MD5.digest()));
        } catch (Exception e) {
            return null;
        }
    }
}
