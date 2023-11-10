package com.example.exceltosql;

import com.example.exceltosql.Util.ExcelUtil;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author :sunjian23
 * @date : 2023/8/18 4:35
 */
public class yamlTest {

    public static void yamlTranslationMatch(String yamlFilePath, String excelFilePath, String outputFilePath, String regex) {
        try {
            // 加载yaml文件
            InputStream input = new FileInputStream(yamlFilePath);
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(input);

            // 创建Excel工作簿和工作表
            Map<String, List<LinkedHashMap<String, Object>>> map = ExcelUtil.ReadExcelByRC(excelFilePath, 1, -1, true);
            Map<String, Object> translationMap = map.get("Translation").stream().collect(Collectors.toMap(
                    linkedHashMap -> (String) linkedHashMap.get("key"),
                    linkedHashMap -> null == linkedHashMap.get("en_US") ? "" : linkedHashMap.get("en_US")
            ));

            //记录翻译缺失的key+中文
            Map<String, Object> missingTranslation = new HashMap<>();
            // 遍历yaml数据并写入Excel文件
            processYamlData(data, translationMap, null, missingTranslation, regex);
            //输出替换后的yaml文件
            Writer writer = new FileWriter(outputFilePath);
            DumperOptions options = new DumperOptions();
            options.setWidth(32);
            options.setPrettyFlow(true);
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml1 = new Yaml(options);
            yaml1.dump(data, writer);
//            //将yaml格式化成json
//            String jsonString = JSONObject.toJSONString(data);
//            //转化为json
//            JsonNode jsonNodeTree = new ObjectMapper().readTree(jsonString);
//            //jaosn转化为yaml
//            String jsonAsYaml = new YAMLMapper().writeValueAsString(jsonNodeTree);
//            writer.write(jsonAsYaml);
            System.out.println("成功创建了新的yaml文件。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static Object processYamlData(Object data, Map<String, Object> translationMap, String parentKey, Map<String, Object> missingTranslation, String regex) {
        if (data instanceof Map) {
            processMapData((Map<String, Object>) data, translationMap, parentKey, missingTranslation, regex);
        } else if (data instanceof List) {
            processListData((List<Object>) data, translationMap, parentKey, missingTranslation, regex);
        } else {
            String value = "";
            if (data != null) {
                value = data.toString();
//                //针对包含转义字符的情况，不进行转义，保留转义字符本身
//                value = StringEscapeUtils.escapeJava(value);
//                //将被转义的unicode字符转回原有状态
//                value = new UnicodeUnescaper().translate(value);
            }
            // 添加正则表达式判断，
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(value);
            if (matcher.matches()) {
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

    private static void processMapData(Map<String, Object> data, Map<String, Object> translationMap, String parentKey, Map<String, Object> missingTranslation, String regex) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (parentKey != null) {
                key = parentKey + "." + key;
            }

            Object o = processYamlData(value, translationMap, key, missingTranslation, regex);
            if (null != o) {
                entry.setValue(o);
            }
        }

    }

    private static void processListData(List<Object> data, Map<String, Object> translationMap, String parentKey, Map<String, Object> missingTranslation, String regex) {
        for (int i = 0; i < data.size(); i++) {
            String key = parentKey + "[" + i + "]";
            Object value = data.get(i);

            processYamlData(value, translationMap, key, missingTranslation, regex);
        }

    }

    public static void main(String[] args) {
        //字符串中是否包含中文
        String regex = "[\\u4E00-\\u9FA5]";
        String yamlFilePath = "C:\\Users\\sunjian23\\Desktop\\artemis_resource_apidoc_HCE_C_3.6.3.20230217150818\\HikCentral Enterprise-Commercial_OpenAPI - 副本.yaml";
        String outputFilePath = "C:\\Users\\sunjian23\\Desktop\\artemis_resource_apidoc_HCE_C_3.6.3.20230217150818\\HikCentral Enterprise-Commercial_OpenAPI_output - 副本.yaml";
        String excelFilePath = "C:\\Users\\sunjian23\\Desktop\\artemis_resource_apidoc_HCE_C_3.6.3.20230217150818\\HikCentral Enterprise-Commercial_OpenAPI - 副本.xlsx";
        yamlTranslationMatch(yamlFilePath, excelFilePath, outputFilePath, regex);
    }


}
