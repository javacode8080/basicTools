package com.example.exceltosql;

import com.example.exceltosql.Util.ExcelUtil;
import com.example.exceltosql.Util.METAINFUtil;
import com.example.exceltosql.Util.PropertiesUtil;
import com.example.exceltosql.Util.XmlUtils;
import com.example.exceltosql.service.Impl.FileToSqlServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author :sunjian23
 * @date : 2023/2/10 17:31
 */
@SpringBootTest(classes = ExceltosqlApplication.class)
public class test {

    public static void mai1n(String[] args) {
        String text = "我 的 i i i 是 珅 哦哦哦 o oo 好";

        // 使用正则表达式匹配中英文字符串
        Pattern pattern = Pattern.compile("(\\S+\\s*\\S+)");
        Matcher matcher = pattern.matcher(text);

        // 输出拆分后的字符串
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }


    public static void main(String[] args) {
//        String input = "我love你.我love123你!@#";
        String input = "انتهت مهلة الوصول بواسطة الوجه ورمز PIN INC INCA وبصمة الإصبع";
        Pattern pattern = Pattern.compile("(\\S+\\s*\\S+)");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }

    @Test
    public void test() throws IOException, IllegalAccessException {

        String s = "انتهت مهلة الوصول بواسطة الوجه ورمز PIN وبصمة الإصبع";


//        HashSet<String> strings = new HashSet<>(null);
//        System.out.println(strings);

//        String s = "C:\\Users\\sunjian23\\Desktop\\sdmc_vis_translate.properties";
//        FileToSqlServiceImpl service = new FileToSqlServiceImpl();
//        Map<String, String> stringStringMap = PropertiesUtil.propertiesToMap(s);
//        String s1 = service.propertiesWriteInExcel(s);
//        System.out.println(s1);
    }


    @Test
    public void test2() throws IOException, IllegalAccessException {

        String sourcePath = "C:\\Users\\sunjian23\\Desktop\\1s\\boothclient_zh.ts";
        String toPath = "C:\\Users\\sunjian23\\Desktop\\1s\\boothclient_去除空白标签_zh.ts";
        XmlUtils.removeBlankTags(sourcePath, toPath);
        System.out.println("sfcs");
    }

    @Test
    public void test3() throws Exception {
        String s = "72yD9wbnkQRM8wP51CRXoTE=";
        byte[] decode = Base64.getDecoder().decode(s);

        String value = String.valueOf(decode);
        System.out.println(value);
    }


    @Test
    public void test5() throws Exception {
//        translationMatch("C:\\Users\\sunjian23\\Desktop\\test\\test\\artemisTranslateFile-91290197-255c-4482-8378-19eb2d025639.xlsx", "C:\\Users\\sunjian23\\Desktop\\test\\test\\HCE-C V2.0.0 OpenAPI翻译-王余轩.xlsx", 1, Integer.MAX_VALUE, "C:\\Users\\sunjian23\\Desktop\\test\\test\\matchExcel.xlsx");
        chineseChangeMatch(
                "C:\\Users\\sunjian23\\Desktop\\test\\web-translate_HikCentral+Enterprise_2.0.1.xlsx",
                "C:\\Users\\sunjian23\\Desktop\\test\\字符串搜索信息_20230829153028.xlsx", 1, Integer.MAX_VALUE,
                "C:\\Users\\sunjian23\\Desktop\\test\\web-matchExcel.xlsx");

    }


    public static void chineseChangeMatch(String filePath, String filePath2, int i, Integer maxcolumnnumber, String uploadFilePath) throws IOException, IllegalAccessException {
        Map<String, List<LinkedHashMap<String, Object>>> map = ExcelUtil.ReadExcelByRC(filePath, i, maxcolumnnumber, true);
        Map<String, List<LinkedHashMap<String, Object>>> map2 = ExcelUtil.ReadExcelByRC(filePath2, i, maxcolumnnumber, true);
        List<LinkedHashMap<String, Object>> list = new ArrayList<>();


        List<LinkedHashMap<String, Object>> translation = map.get("英语翻译");
        List<LinkedHashMap<String, Object>> translation2 = map2.get("字符串搜索信息1");
        for (LinkedHashMap<String, Object> translationMap : translation) {
            String key = translationMap.get("Key (关键信息)").toString();
            List<LinkedHashMap<String, Object>> collect = translation2.stream().filter(translation2Map -> {
                return translation2Map.get("key").equals(key);
            }).collect(Collectors.toList());
            if (!collect.isEmpty()) {
                if (collect.size() > 1) {
                    Map<Object, List<LinkedHashMap<String, Object>>> collect1 = collect.stream().collect(Collectors.groupingBy(mapw -> mapw.get("中文（简体）")));
                    if (collect1.size() > 1) {
                        System.out.println("key值重复但中文不同"+key);
                    }
                }else{
                    String hirule_Zh_CN = collect.get(0).get("中文（简体）").toString();
                    String hido_Zh_CN = translationMap.get("简体中文").toString();
                    if (!hirule_Zh_CN.equals(hido_Zh_CN)) {
                        LinkedHashMap<String, Object> stringObjectLinkedHashMap = new LinkedHashMap<>();
                        stringObjectLinkedHashMap.put("key", key);
                        stringObjectLinkedHashMap.put("hirule", hirule_Zh_CN);
                        stringObjectLinkedHashMap.put("hido", hido_Zh_CN);
                        list.add(stringObjectLinkedHashMap);

                    }
                }

            }
        }

        //写出文件到Excel中
        Map<String, List<LinkedHashMap<String, Object>>> excelMap = new HashMap();
        List<String> sheetList = new ArrayList<>();
        excelMap.put("对比结果", list);
        sheetList.add("对比结果");
        //写出文件
        ExcelUtil.writeExcel(uploadFilePath, excelMap, sheetList);


    }

    /**
     * @param filePath:                待匹配文件
     * @param filePath2:               翻译文件
     * @param i:                       从第几行读取，起始为1
     * @param maxcolumnnumber:读取到第几行结束
     * @param uploadFilePath:          输出匹配文件路径
     * @return void
     * @author sunjian23
     * @description TODO：根据中文匹配英文(artemis)，多个中文以第一个获取的英文为准
     * @date 2023/8/24 16:38
     */
    public static void translationMatch(String filePath, String filePath2, int i, Integer maxcolumnnumber, String uploadFilePath) throws IOException, IllegalAccessException {
        Map<String, List<LinkedHashMap<String, Object>>> map = ExcelUtil.ReadExcelByRC(filePath, i, maxcolumnnumber, true);
        Map<String, List<LinkedHashMap<String, Object>>> map2 = ExcelUtil.ReadExcelByRC(filePath2, i, maxcolumnnumber, true);

        List<LinkedHashMap<String, Object>> translation = map.get("Translation");
        List<LinkedHashMap<String, Object>> translation2 = map2.get("Translation");
        for (LinkedHashMap<String, Object> translationMap : translation) {
            String zh_cn = translationMap.get("zh_CN").toString();
            Optional<LinkedHashMap<String, Object>> zh_cn1 = translation2.stream().filter(translation2Map -> {
                return translation2Map.get("zh_CN").equals(zh_cn);
            }).findFirst();
            if (zh_cn1.isPresent()) {

                translationMap.put("en_US", zh_cn1.get().get("en_US"));
            }
        }

        //写出文件到Excel中
        Map<String, List<LinkedHashMap<String, Object>>> excelMap = new HashMap();
        List<String> sheetList = new ArrayList<>();
        excelMap.put("Translation", translation);
        sheetList.add("Translation");
        //写出文件
        ExcelUtil.writeExcel(uploadFilePath, excelMap, sheetList);


    }

    @Test
    public void test6() throws Exception {
        HashMap<String, Map<String, Double>> hashMap = new HashMap<>();
        for (int i=0;i<5;i++){
            Random rand = new Random();
            HashMap<String, Double> hashMap1 = new HashMap<>();
            if(i == 2){
                hashMap1.put("statusRate",null);
            }else{
                hashMap1.put("statusRate",rand.nextDouble());
            }
            hashMap.put("status"+i,hashMap1);
        }
        hashMap = hashMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue((map1, map2) -> {
                    Double value1 = map1.get("statusRate");
                    Double value2 = map2.get("statusRate");
                    if(value1 == null || value2 == null){
                        return 1;
                    }else{
                        return -value1.compareTo(value2);
                    }
//                    if (value1 == null && value2 == null) {
//                        return 0;
//                    } else if (value1 == null) {
//                        return 1;
//                    } else if (value2 == null) {
//                        return -1;
//                    } else {
//                        return -value1.compareTo(value2);
//                    }
                })).collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new));
        System.out.println(hashMap);

    }


    @Test
    public void test7() throws Exception {
        Date date = new Date();
        System.out.println(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_WEEK, -1); // 添加一天
        Date datePlusOneDay = calendar.getTime(); // 获取新的日期
        System.out.println(datePlusOneDay);
    }
}
