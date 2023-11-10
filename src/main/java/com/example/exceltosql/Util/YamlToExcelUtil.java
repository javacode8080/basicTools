package com.example.exceltosql.Util;

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
 * @date : 2023/8/20 22:52
 */
public class YamlToExcelUtil {

    /**
     * @param yamlFilePath:  yaml文件的路径
     * @param excelFilePath: excel的生成路径
     * @param regex:         正则表达式，满足正则表达式的value值才会写入到excel中，[例如".*"-->标识全匹配]
     * @return void
     * @author sunjian23
     * @description TODO
     * @date 2023/8/18 19:28
     */
    public static void convertYamlToExcel(String yamlFilePath, String excelFilePath, String regex) throws IOException {
        // 加载yaml文件
        InputStream input = new FileInputStream(yamlFilePath);
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(input);
        input.close();
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
        // 遍历yaml数据并写入Excel文件
        int excelRow = processYamlData(data, sheet, null, rowNum, regex);

        // 保存Excel文件
        OutputStream output = new FileOutputStream(excelFilePath);
        workbook.write(output);
        workbook.close();
        output.close();

        System.out.println("转换完成！生成" + (excelRow - 1) + "行数据");
    }


    /**
     * @param data:      yaml文件的层级数据(递归)
     * @param sheet:     excel的sheet页对象
     * @param parentKey: 层级路径
     * @param rowNum:    excel行数记录
     * @param regex:     正则表达式
     * @return int：返回Excel的下一行数
     * @author sunjian23
     * @description TODO：不确定类型的yaml递归
     * @date 2023/8/21 1:50
     */
    public static int processYamlData(Object data, Sheet sheet, String parentKey, int rowNum, String regex) {
        if (data instanceof Map) {
            rowNum = processMapData((Map<String, Object>) data, sheet, parentKey, rowNum, regex);
        } else if (data instanceof List) {
            rowNum = processListData((List<Object>) data, sheet, parentKey, rowNum, regex);
        } else {
            String value = "";
            if (data != null) {
                value = data.toString();
            }
            // 添加正则表达式判断，只有满足正则条件的值才写入到Excel中
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                Row row = sheet.createRow(rowNum++);
                Cell keyCell = row.createCell(0);
                keyCell.setCellValue(parentKey);
                Cell valueCell = row.createCell(1);
                valueCell.setCellValue(value);
            }
        }

        return rowNum;
    }

    /**
     * @param data:      yaml文件的层级数据(递归)
     * @param sheet:     excel的sheet页对象
     * @param parentKey: 层级路径
     * @param rowNum:    excel行数记录
     * @param regex:     正则表达式
     * @return int：返回Excel的下一行数
     * @author sunjian23
     * @description TODO：map类型的yaml递归
     * @date 2023/8/21 1:51
     */
    public static int processMapData(Map<String, Object> data, Sheet sheet, String parentKey, int rowNum, String regex) {
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

    /**
     * @param data:      yaml文件的层级数据(递归)
     * @param sheet:     excel的sheet页对象
     * @param parentKey: 层级路径
     * @param rowNum:    excel行数记录
     * @param regex:     正则表达式
     * @return int：返回Excel的下一行数
     * @author sunjian23
     * @description TODO:list类型的yaml递归
     * @date 2023/8/21 1:52
     */
    public static int processListData(List<Object> data, Sheet sheet, String parentKey, int rowNum, String regex) {
        for (int i = 0; i < data.size(); i++) {
            String key = parentKey + "[" + i + "]";
            Object value = data.get(i);

            rowNum = processYamlData(value, sheet, key, rowNum, regex);
        }

        return rowNum;
    }
}
