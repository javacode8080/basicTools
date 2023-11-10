package com.example.exceltosql;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.translate.UnicodeUnescaper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author :sunjian23
 * @date : 2023/8/18 4:35
 */
public class yamlTest2 {
    /**
     * @param yamlFilePath:  yaml文件的路径
     * @param excelFilePath: excel的生成路径
     * @param regex:         正则表达式，满足正则表达式的value值才会写入到excel中，[例如".*"-->标识全匹配]
     * @return void
     * @author sunjian23
     * @description TODO
     * @date 2023/8/18 19:28
     */
    public static void convertYamlToExcel(String yamlFilePath, String excelFilePath, String regex) {
        try {
            // 加载yaml文件
            InputStream input = new FileInputStream(yamlFilePath);
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(input);

            // 创建Excel工作簿和工作表
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Translation");
            int rowNum = 0;

            // 遍历yaml数据并写入Excel文件
            int excelRow = processYamlData(data, sheet, null, rowNum, regex);

            // 保存Excel文件
            OutputStream output = new FileOutputStream(excelFilePath);
            workbook.write(output);

            System.out.println("转换完成！生成" + excelRow + "行数据");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static int processYamlData(Object data, Sheet sheet, String parentKey, int rowNum, String regex) {
        if (data instanceof Map) {
            rowNum = processMapData((Map<String, Object>) data, sheet, parentKey, rowNum, regex);
        } else if (data instanceof List) {
            rowNum = processListData((List<Object>) data, sheet, parentKey, rowNum, regex);
        } else {
            String value = "";
            if (data != null) {
                value = data.toString();
                //针对包含转义字符的情况，不进行转义，保留转义字符本身
                value = StringEscapeUtils.escapeJava(value);
                //将被转义的unicode字符转回原有状态
                value = new UnicodeUnescaper().translate(value);
            }
            // 添加正则表达式判断，只有满足正则条件的值才写入到Excel中
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
                Row row = sheet.createRow(rowNum++);
                Cell keyCell = row.createCell(0);
                keyCell.setCellValue(parentKey);
                Cell valueCell = row.createCell(1);
                valueCell.setCellValue(value);
            }
        }

        return rowNum;
    }

    private static int processMapData(Map<String, Object> data, Sheet sheet, String parentKey, int rowNum, String regex) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (parentKey != null) {
                key = parentKey + "." + key;
            }

            rowNum = processYamlData(value, sheet, key, rowNum, regex);
        }

        return rowNum;
    }

    private static int processListData(List<Object> data, Sheet sheet, String parentKey, int rowNum, String regex) {
        for (int i = 0; i < data.size(); i++) {
            String key = parentKey + "[" + i + "]";
            Object value = data.get(i);

            rowNum = processYamlData(value, sheet, key, rowNum, regex);
        }

        return rowNum;
    }

    public static void main(String[] args) throws IOException {
        //字符串中是否包含中文
        String regex = ".*[\\u4E00-\\u9FA5]+.*";
        String yamlFilePath = "C:\\Users\\sunjian23\\Desktop\\artemis_resource_apidoc_HCE_C_3.6.3.20230217150818\\HikCentral Enterprise-Commercial_OpenAPI - 副本.yaml";
        String outputFilePath = "C:\\Users\\sunjian23\\Desktop\\artemis_resource_apidoc_HCE_C_3.6.3.20230217150818\\HikCentral Enterprise-Commercial_OpenAPI_output - 副本.yaml";
        String excelFilePath = "C:\\Users\\sunjian23\\Desktop\\artemis_resource_apidoc_HCE_C_3.6.3.20230217150818\\HikCentral Enterprise-Commercial_OpenAPI - 副本.xlsx";

//        // 加载yaml文件
//        InputStream input = new FileInputStream(yamlFilePath);
//        Yaml yaml = new Yaml();
//        Map<String, Object> data = yaml.load(input);
//        // 保持内容不变地写入新文件
//        FileWriter writer = new FileWriter(outputFilePath);
//        yaml.dump(data, writer);
//
//        writer.close();
//        System.out.println("新文件写入成功");
        convertYamlToExcel(yamlFilePath, excelFilePath, regex);
    }

}
