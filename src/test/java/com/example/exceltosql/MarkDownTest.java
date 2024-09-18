//package com.example.exceltosql;
//
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.io.*;
//import java.nio.charset.Charset;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * @author :sunjian23
// * @date : 2024/4/8 9:12
// */
//@SpringBootTest
//public class MarkDownTest {
//
//    @Test
//    public void test() throws Exception {
//
//    }
//
//
//    private static List<Map<String, String>> parsePropertiesFile(String filePath) throws IOException {
//        List<Map<String, String>> errorCodeGroups = new ArrayList<>();
//        Map<String, String> currentGroup = null;
//
//        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                if (line.startsWith("#")) {
//                    // 新的错误码类别开始
//                    currentGroup = new HashMap<>();
//                    errorCodeGroups.add(currentGroup);
//                    currentGroup.put("category", line.substring(1).trim());
//                    continue;
//                }
//
//                String[] parts = line.split("=");
//                if (parts.length != 2) {
//                    System.err.println("Invalid line: " + line);
//                    continue;
//                }
//
//                String key = parts[0].trim();
//                String value = parts[1].trim();
//
//                String errorCode = key.substring("errorCode.".length(), key.indexOf('.'));
//                String field = key.substring(key.indexOf('.') + 1);
//
//                if (currentGroup == null) {
//                    throw new IllegalStateException("Missing category before error code: " + line);
//                }
//
//                if (!currentGroup.containsKey(errorCode)) {
//                    currentGroup.put(errorCode, new HashMap<>());
//                }
//
//                currentGroup.get(errorCode).put(field, value);
//            }
//        }
//
//        return errorCodeGroups;
//    }
//
//    private static void writeMarkdownTables(String filePath, List<Map<String, String>> errorCodeGroups) throws IOException {
//        StringBuilder markdownContent = new StringBuilder();
//
//        for (Map<String, String> errorCodeGroup : errorCodeGroups) {
//            String category = errorCodeGroup.remove("category");
//
//            markdownContent.append("## ").append(category).append("\n\n");
//            markdownContent.append("| 错误码      | 错误分类及错误原因 | 消除建议             |\n");
//            markdownContent.append("| ----------- | ------------------ | -------------------- |\n");
//
//            for (Map.Entry<String, Map<String, String>> entry : errorCodeGroup.entrySet()) {
//                String errorCode = entry.getKey();
//                Map<String, String> errorDetails = entry.getValue();
//
//                markdownContent.append("| ").append(errorCode).append(" | ")
//                        .append(errorDetails.get("description")).append(" | ")
//                        .append(errorDetails.get("suggestion")).append(" |\n");
//            }
//
//            markdownContent.append("\n");
//        }
//
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
//            writer.write(markdownContent.toString());
//        }
//    }
//
//
//}
