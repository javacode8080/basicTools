package com.example.exceltosql.Util;

import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.origin.OriginTrackedValue;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.InputStreamResource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author :sunjian23
 * @date : 2023/2/10 18:22
 */
public class PropertiesUtil {

    public static Map<String, String> propertiesToMap(String filePath){

        PropertiesPropertySourceLoader loader = new PropertiesPropertySourceLoader();
        Map<String, String> map = new HashMap<>();
        try {
            PropertySource<?> propertySource = loader.load("publicConfiguration", new InputStreamResource(Files.newInputStream(Paths.get(filePath)))).get(0);
            Map<String, OriginTrackedValue> source = (Map<String, OriginTrackedValue>)propertySource.getSource();
            Iterator iterator = source.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, OriginTrackedValue> next = (Map.Entry<String, OriginTrackedValue>)iterator.next();
                OriginTrackedValue value = next.getValue();
                String oldCharsetString = value.toString();
                String newCharsetString = changeCharset(oldCharsetString, "ISO_8859_1", "UTF-8");
                map.put(next.getKey(),newCharsetString);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }


    /**
     * 字符串编码转换的实现方法
     *
     * @param str
     *            待转换的字符串
     * @param oldCharset
     *            源字符集
     * @param newCharset
     *            目标字符集
     */
    public static String changeCharset(String str, String oldCharset, String newCharset)
            throws UnsupportedEncodingException {
        if (str != null) {
            // 用源字符编码解码字符串
            byte[] bs = str.getBytes(oldCharset);
            return new String(bs, newCharset);
        }
        return null;
    }

}
