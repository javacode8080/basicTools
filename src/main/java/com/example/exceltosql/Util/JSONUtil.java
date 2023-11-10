package com.example.exceltosql.Util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


/**
 * @author :sunjian23
 * @date : 2022/8/23 18:19
 */
public class JSONUtil {


    /**
     * 将key+中文转化为JSON
     *
     * @param excelClassPath
     * @param startRow
     * @param endRow
     * @param FinalFileClassPath
     * @param clazz
     * @throws Exception
     */
    public static <T> void toJSON(String excelClassPath, Integer startRow, Integer endRow, String FinalFileClassPath, Class<T> clazz) throws Exception {
        File json = new File(FinalFileClassPath);
        if (!json.exists()) {
            json.createNewFile();
        }
        //读取Excel中的内容
        List<Object> list = ExcelUtil.ReadExcelByPOJO(excelClassPath, startRow, endRow, true, clazz);
        if (null == list) {
            throw new RuntimeException("上传失败：读取内容为空");
        }
        if (list.size() == 0) {
            throw new RuntimeException("上传失败:未读取到规定字段的数据(key/中文（简体）)，请检查文件格式是否正确");
        }
        Map<String, String> map = new LinkedHashMap<>();
        list.stream().forEach(obj -> {
            T bean = (T) obj;
            try {
                Method getKey = clazz.getMethod("getKey");//这里get方法名是不变的，所以可以写死
                Method getZh_CN = clazz.getMethod("getZh_CN");//这里get方法名是不变的，所以可以写死
                String key = (String) getKey.invoke(bean);
                String value = (String) getZh_CN.invoke(bean);
                if (null == key || null == value || "".equals(key) || "".equals(value)) {
                    throw new RuntimeException("上传失败:存在(key/中文（简体）)单元格内容缺失的情况，请检查文件是否正确");
                }
                if (null != key && !"".equals(key)) {
                    //map去重
//                    if(null!=map.get(key)){
//                        System.out.println(1);
//                    }
                    int firstIndex = key.indexOf('\\');
                    if (firstIndex != -1) {//index不是-1的时候说明该key含有反斜杠，进行裁切存入
                        String replaceKey = key.substring(firstIndex + 1).replace('\\', '.');
//                        String replaceKey = key.substring(0).replace('\\', '.');
                        // replace.replace('\\', '.');//才切掉第一个反斜杠前面的东西再替换反斜杠为点
                        map.put(replaceKey, value);
                    } else {
                        map.put(key, value);//不是特殊的key就直接存进去就行
                    }
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        //toJsonString()方法可以自定义一些格式化JSON字符串的选项https://blog.csdn.net/zhanqq2012/article/details/104921655/
        String s = JSON.toJSONString(map, SerializerFeature.PrettyFormat);
        System.out.println(s);
        FileOutputStream outputStream = null;
        outputStream = new FileOutputStream(json.getAbsoluteFile(), false);//注意这里的第二个参数true是指写入的时候不覆盖原有的内容
        outputStream.write(s.getBytes());
        outputStream.close();


    }




    /**
     * 将key+中文转化为JSON
     *
     * @param excelClassPath
     * @param startRow
     * @param endRow
     * @param FinalFileClassPath
     * @param clazz
     * @throws Exception
     */
    public static <T> void toJSONNotRemoveDuplicates(String excelClassPath, Integer startRow, Integer endRow, String FinalFileClassPath, Class<T> clazz) throws Exception {
        File json = new File(FinalFileClassPath);
        if (!json.exists()) {
            json.createNewFile();
        }
        //读取Excel中的内容
        List<Object> list = ExcelUtil.ReadExcelByPOJO(excelClassPath, startRow, endRow, true, clazz);
        if (null == list) {
            throw new RuntimeException("上传失败：读取内容为空");
        }
        if (list.size() == 0) {
            throw new RuntimeException("上传失败:未读取到规定字段的数据(key/中文（简体）)，请检查文件格式是否正确");
        }
        Map<String, String> map = new LinkedHashMap<>();
        List<String> lists=new ArrayList<>();
        list.stream().forEach(obj -> {
            T bean = (T) obj;
            try {
                Method getKey = clazz.getMethod("getKey");//这里get方法名是不变的，所以可以写死
                Method getZh_CN = clazz.getMethod("getZh_CN");//这里get方法名是不变的，所以可以写死
                String key = (String) getKey.invoke(bean);
                String value = (String) getZh_CN.invoke(bean);
                if (null == key || null == value || "".equals(key) || "".equals(value)) {
                    throw new RuntimeException("上传失败:存在(key/中文（简体）)单元格内容缺失的情况，请检查文件是否正确");
                }
                if (null != key && !"".equals(key)) {
                    //map去重
//                    if(null!=map.get(key)){
//                        System.out.println(1);
//                    }
                    int firstIndex = key.indexOf('\\');
                    if (firstIndex != -1) {//index不是-1的时候说明该key含有反斜杠，进行裁切存入
                        String replaceKey = key.substring(firstIndex + 1).replace('\\', '.');
//                        String replaceKey = key.substring(0).replace('\\', '.');
                        // replace.replace('\\', '.');//才切掉第一个反斜杠前面的东西再替换反斜杠为点
                        map.put(replaceKey, value);
                        lists.add(replaceKey);
                    } else {
                        map.put(key, value);//不是特殊的key就直接存进去就行
                        lists.add(key);
                    }
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        //toJsonString()方法可以自定义一些格式化JSON字符串的选项https://blog.csdn.net/zhanqq2012/article/details/104921655/
//        String s = JSON.toJSONString(map, SerializerFeature.PrettyFormat);
//        System.out.println(s);
        String s = JSON.toJSONString(lists, SerializerFeature.PrettyFormat);
        System.out.println(s);
        FileOutputStream outputStream = null;
        outputStream = new FileOutputStream(json.getAbsoluteFile(), false);//注意这里的第二个参数true是指写入的时候不覆盖原有的内容
        outputStream.write(s.getBytes());
        outputStream.close();


    }

//    /**
//     * @param jsonObject:
//     * @param keyProperty:当作key的哪个属性
//     * @param valueProperty:当作value的哪个属性
//     * @return List<Map<String,String>>
//     * @author sunjian23
//     * @description TODO:将json中指定的属性以key,value的形式保存为map，对于数组形式的话同样按照数组存储
//     * @date 2023/3/9 12:35
//     */
//
//    public static List<Map<String,String>> jsonToList(JSONObject jsonObject, String keyProperty, String valueProperty){
//        return null;
//
//    }


//    /**
//     * @param originStr: 待格式化的字符串
//     * @return String：格式化后的字符串
//     * @author sunjian23
//     * @description TODO：格式化字符串
//     * JSON格式化原则：仅存在{},[]两种情况，{}，[]不可能存在于""当中，value/key中可能存在{},[],"等特殊符号，如何去区分是难点
//     * @date 2022/10/29 16:25
//     */
//
//    public static String formatJson(String originStr) {
//        if (originStr == null) {
//            return null;
//        }
//        char[] charArray = originStr.toCharArray();
//        StringBuilder sb = new StringBuilder();
//
//        int tabCount = 0;
//        // 可以不用stack,在满足 '}' 的if语句内更换 int temp = tabCount-1即可
//        Stack<Integer> stack = new Stack<>();
//        char c;
//        for (int i = 0; i < charArray.length; i++) {
//            c = charArray[i];
//            // 遇到{换行
//            if (c == '{') {
//                //首先判断
//                enterAndTabs(sb, tabCount);
//                stack.push(tabCount++);
//                sb.append(c);
//                continue;
//            }
//            // 换行
//            if (c == '"'
//                    && (charArray[i - 1] == '{' || charArray[i - 1] == ',')) {
//                enterAndTabs(sb, tabCount);
//                sb.append(c);
//                continue;
//            }
//            // 换行
//            if (c == '}') {
//                enterAndTabs(sb, stack.pop());
//                sb.append(c);
//                tabCount--;
//                continue;
//            }
//            sb.append(c);
//        }
//
//
//        return sb.toString();
//    }


//    /**
//     * @param originStr: 待格式化的字符串
//     * @return String：格式化后的字符串
//     * @author sunjian23
//     * @description TODO：格式化字符串
//     * JSON格式化原则：仅存在{},[]两种情况，{}，[]不可能存在于""当中，value/key中可能存在{},[],"等特殊符号，如何去区分是难点
//     * @date 2022/10/29 16:25
//     */
//
//    public static String formatJson(String originStr) {
//        if (originStr == null) {
//            return null;
//        }
//        char[] charArray = originStr.toCharArray();
//        StringBuilder sb = new StringBuilder();
//
//        int tabCount = 0;
//        // 可以不用stack,在满足 '}' 的if语句内更换 int temp = tabCount-1即可
//        Stack<Integer> stack = new Stack<>();
//        char c;
//        for (int i = 0; i < charArray.length; i++) {
//            c = charArray[i];
//            // 遇到{换行
//            if (c == '{') {
//                enterAndTabs(sb, tabCount);
//                stack.push(tabCount++);
//                sb.append(c);
//                continue;
//            }
//            // 换行
//            if (c == '"'
//                    && (charArray[i - 1] == '{' || charArray[i - 1] == ',')) {
//                enterAndTabs(sb, tabCount);
//                sb.append(c);
//                continue;
//            }
//            // 换行
//            if (c == '}') {
//                enterAndTabs(sb, stack.pop());
//                sb.append(c);
//                tabCount--;
//                continue;
//            }
//            sb.append(c);
//        }
//
//
//        return sb.toString();
//    }
//    /**
//     * 回车与制表符
//     *
//     * @param sb StringBuilder
//     * @param tabCount 多少个制表符
//     */
//    private static void enterAndTabs(StringBuilder sb, int tabCount) {
//        sb.append("\n");
//        while (tabCount-- > 0) {
//            sb.append("    ");
//        }
//    }

}
