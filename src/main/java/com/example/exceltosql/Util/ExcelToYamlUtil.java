package com.example.exceltosql.Util;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author :sunjian23
 * @date : 2023/8/20 22:54
 */
public class ExcelToYamlUtil {

    private static final String rootDir = System.getProperties().getProperty("user.dir") + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "ExcelToYaml";


    /**
     * @param yamlFilePath: yaml文件的上传路径
     * @param excelFilePath: Excel文件的上传路径
     * @param regex: 正则表达式
     * @return String：返回生成的文件路径
     * @author sunjian23
     * @description TODO
     * @date 2023/8/21 1:44
     */
    public static String yamlTranslationMatch(String yamlFilePath, String excelFilePath, String regex) throws IOException {
        // 加载yaml文件
        InputStream input = new FileInputStream(yamlFilePath);
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(input);
        input.close();

        // 创建Excel工作簿和工作表
        Map<String, List<LinkedHashMap<String, Object>>> map = ExcelUtil.ReadExcelByRC(excelFilePath, 1, -1, true);
        Map<String, Object> translationMap = map.get("Translation").stream().collect(Collectors.toMap(
                linkedHashMap -> null == linkedHashMap.get("key") ? "" : (String) linkedHashMap.get("key"),
                linkedHashMap -> null == linkedHashMap.get("en_US") ? "" : linkedHashMap.get("en_US")
        ));
        //记录翻译缺失的key+中文
        Map<String, Object> missingTranslation = new HashMap<>();
        // 遍历yaml数据并写入Excel文件
        processYamlData(data, translationMap, null, missingTranslation, regex);
        String zipDirPath = rootDir + File.separator + "temp" + UUID.randomUUID().toString();
        String yamlFileName = "TranslateYaml.yaml";
        File zipDir = new File(zipDirPath);
        //判断保存文件所对应路径是否存在
        if (!zipDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            zipDir.mkdirs();
        }
        //todo：此处保证了yaml格式与原格式保持一致，注意使用的是jackson-dataformat-yaml包
        //将yaml格式化成json
        String jsonString = JSONObject.toJSONString(data);
        //转化为json
        JsonNode jsonNodeTree = new ObjectMapper().readTree(jsonString);
        //jaosn转化为yaml
        String jsonAsYaml = new YAMLMapper().writeValueAsString(jsonNodeTree);
        //todo：输出时尽量使用输出流形式，这样不会丢失字节，直接采用FileWriter()可能会丢失数据
        FileOutputStream outputStream = null;
        outputStream = new FileOutputStream(zipDirPath + File.separator + yamlFileName, false);//注意这里的第二个参数true是指写入的时候不覆盖原有的内容
        outputStream.write(jsonAsYaml.getBytes());
        outputStream.close();
        if (!missingTranslation.isEmpty()) {
            String missingTranslationExcelName = "missingTranslation.xlsx";
            String zipFileName = "translate.zip";
            // 创建Excel工作簿和工作表
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Translation");
            int rowNum = 0;
            //创建标题行
            Row row = sheet.createRow(rowNum++);
            Cell keyCell = row.createCell(0);
            keyCell.setCellValue("key");
            Cell zh_CNCell = row.createCell(1);
            zh_CNCell.setCellValue("zh_CN");
            Cell en_USCell = row.createCell(2);
            en_USCell.setCellValue("en_US");
            //遍历missingTranslation
            Iterator<Map.Entry<String, Object>> iterator = missingTranslation.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> next = iterator.next();
                row = sheet.createRow(rowNum++);
                keyCell = row.createCell(0);
                keyCell.setCellValue(next.getKey());
                zh_CNCell = row.createCell(1);
                zh_CNCell.setCellValue(next.getValue().toString());
            }
            // 保存Excel文件
            OutputStream output = new FileOutputStream(zipDirPath + File.separator + missingTranslationExcelName);
            workbook.write(output);
            workbook.close();
            output.close();
            ZipUtil.zipFolderContents(zipDirPath, rootDir + File.separator + zipFileName);
            return rootDir + File.separator + zipFileName;
        } else {
            return zipDirPath + File.separator + yamlFileName;
        }
    }


    /**
     * @param data: yaml文件的层级数据(递归)
     * @param translationMap: 翻译映射map
     * @param parentKey: 层级路径
     * @param missingTranslation:缺失的翻译
     * @param regex: 正则表达式
     * @return Object：返回需要修改的value值翻译，返回null则不需要修改/未到最终层级，不是String类型
     * @author sunjian23
     * @description TODO：不确定层级的yaml递归
     * @date 2023/8/21 1:45
     */
    private static Object processYamlData(Object data, Map<String, Object> translationMap, String parentKey, Map<String, Object> missingTranslation, String regex) {
        if (data instanceof Map) {
            processMapData((Map<String, Object>) data, translationMap, parentKey, missingTranslation, regex);
        } else if (data instanceof List) {
            processListData((List<Object>) data, translationMap, parentKey, missingTranslation, regex);
        } else {
            String value = "";
            if (data != null) {
                value = data.toString();
            }
            // 添加正则表达式判断，
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                //获取翻译
                String translation = (String) translationMap.get(parentKey);
                if (StringUtils.isNotEmpty(translation)) {
                    data = translation;
                    return data;
                } else {
                    //记录缺失的中文翻译
                    missingTranslation.put(parentKey, value);
                }
            }
        }
        return null;
    }

    /**
     * @param data: yaml文件的层级数据(递归)
     * @param translationMap: 翻译映射map
     * @param parentKey: 层级路径
     * @param missingTranslation: 缺失的翻译
     * @param regex: 正则表达式
     * @return void
     * @author sunjian23
     * @description TODO：对于map类型的yaml递归，同时对于翻译存在的则更新map的翻译
     * @date 2023/8/21 1:47
     */
    private static void processMapData(Map<String, Object> data, Map<String, Object> translationMap, String parentKey, Map<String, Object> missingTranslation, String regex) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (parentKey != null) {
                key = parentKey + "." + key;
            }

            //返回值是待更新的翻译
            Object o = processYamlData(value, translationMap, key, missingTranslation, regex);
            if (null != o) {
                entry.setValue(o);
            }
        }

    }

    /**
     * @param data: yaml文件的层级数据(递归)
     * @param translationMap: 翻译映射map
     * @param parentKey: 层级路径
     * @param missingTranslation:缺失的翻译
     * @param regex: 正则表达式
     * @return void
     * @author sunjian23
     * @description TODO:对于list类型的yaml递归
     * @date 2023/8/21 1:48
     */
    private static void processListData(List<Object> data, Map<String, Object> translationMap, String parentKey, Map<String, Object> missingTranslation, String regex) {
        for (int i = 0; i < data.size(); i++) {
            String key = parentKey + "[" + i + "]";
            Object value = data.get(i);

            processYamlData(value, translationMap, key, missingTranslation, regex);
        }

    }
}
