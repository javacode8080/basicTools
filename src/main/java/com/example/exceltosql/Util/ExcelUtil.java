package com.example.exceltosql.Util;


import com.example.exceltosql.entity.DuplicateKeyBean;
import com.example.exceltosql.interfaces.ExcelResource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author :sunjian23
 * @date : 2022/8/23 17:16
 */
public class ExcelUtil {
    //日志输出
    private static Logger logger = LoggerFactory.getLogger(ExcelUtil.class);
    //定义excel类型
    private static final String XLS = "xls";
    private static final String XLSX = "xlsx";

    /**
     * 根据文件后缀名类型获取对应的工作簿对象
     *
     * @param inputStream 读取文件的输入流
     * @param fileType    文件后缀名类型（xls或xlsx）
     * @return 包含文件数据的工作簿对象
     */
    private static Workbook getWorkbook(InputStream inputStream, String fileType) throws IOException {
        //用自带的方法新建工作薄
        Workbook workbook = WorkbookFactory.create(inputStream);
        //后缀判断有版本转换问题
        //Workbook workbook = null;
        //if (fileType.equalsIgnoreCase(XLS)) {
        // workbook = new HSSFWorkbook(inputStream);
        //} else if (fileType.equalsIgnoreCase(XLSX)) {
        // workbook = new XSSFWorkbook(inputStream);
        //}
        return workbook;
    }

    /**
     * 将单元格内容转换为字符串
     *
     * @param cell
     * @return
     */
    private static String convertCellValueToString(Cell cell) {
        if (cell == null) {
            return null;
        }
        String returnValue = null;
        switch (cell.getCellType()) {
            case NUMERIC: //数字
                Double doubleValue = cell.getNumericCellValue();
                // 格式化科学计数法，取一位整数，如取小数，值如0.0,取小数点后几位就写几个0
                DecimalFormat df = new DecimalFormat("0");
                returnValue = df.format(doubleValue);
                break;
            case STRING: //字符串
                returnValue = cell.getStringCellValue();
                break;
            case BOOLEAN: //布尔
                Boolean booleanValue = cell.getBooleanCellValue();
                returnValue = booleanValue.toString();
                break;
            case BLANK: // 空值
                break;
            case FORMULA: // 公式
                returnValue = cell.getCellFormula();
                break;
            case ERROR: // 故障
                break;
            default:
                break;
        }
        return returnValue;
    }

    /**
     * 处理Excel内容转为Map<String,List<Map<String, Object>>>输出
     * workbook：已连接的工作薄
     * StatrRow：读取的开始行数（默认填0，0开始,传过来是EXcel的行数值默认从1开始，这里已处理减1）
     * EndRow：读取的结束行数（填-1为全部）
     * ExistTop:是否存在头部（如存在则读取数据时会把头部拼接到对应数据，若无则为当前列数）
     */
    private static Map<String, List<LinkedHashMap<String, Object>>> HandleData(Workbook workbook, int StatrRow, int EndRow, boolean ExistTop) {
        //声明返回结果集result
        Map<String, List<LinkedHashMap<String, Object>>> result = new HashMap<>();

        //解析sheet（sheet是Excel脚页）
        /**
         *此处会读取所有脚页的行数据，若只想读取指定页，不要for循环，直接给sheetNum赋值，脚页从0开始（通常情况Excel都只有一页，所以此处未进行进一步处理）
         */
        for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
            //声明一个Excel头部函数
            ArrayList<String> top = new ArrayList<>();
            //声明每个sheet的结果集result
            List<LinkedHashMap<String, Object>> sheetList = new ArrayList<>();
            Sheet sheet = workbook.getSheetAt(sheetNum);
            // 校验sheet是否合法
            if (sheet == null) {
                continue;
            }
            //如存在头部，处理头部数据
            if (ExistTop) {
                int firstRowNum = sheet.getFirstRowNum();
                Row firstRow = sheet.getRow(firstRowNum);
                if (null == firstRow) {
                    logger.warn("解析Excel失败，在第一行没有读取到任何数据！");
                    result.put(workbook.getSheetName(sheetNum), null);
                    break;
                }
                for (int i = 0; i < firstRow.getLastCellNum(); i++) {
                    top.add(convertCellValueToString(firstRow.getCell(i)));
                }
            }
            //处理Excel数据内容
            int endRowNum;
            //获取结束行数
            if (EndRow == -1) {
                endRowNum = sheet.getPhysicalNumberOfRows();
            } else {
                endRowNum = EndRow <= sheet.getPhysicalNumberOfRows() ? EndRow : sheet.getPhysicalNumberOfRows();
            }
            //遍历行数
            for (int i = StatrRow - 1; i < endRowNum; i++) {
                Row row = sheet.getRow(i);
                if (null == row) {
                    continue;
                }
                LinkedHashMap<String, Object> map = new LinkedHashMap<>();
                //获取所有列数据
                for (int y = 0; y < row.getLastCellNum(); y++) {
                    if (top.size() > 0) {
                        if (top.size() >= y) {
                            map.put(top.get(y), convertCellValueToString(row.getCell(y)));
                        } else {
                            map.put(String.valueOf(y + 1), convertCellValueToString(row.getCell(y)));
                        }
                    } else {
                        map.put(String.valueOf(y + 1), convertCellValueToString(row.getCell(y)));
                    }
                }
                sheetList.add(map);
            }
            result.put(workbook.getSheetName(sheetNum), sheetList);
        }
        return result;
    }

    /**
     * 方法一
     * 根据行数和列数读取Excel
     * fileName:Excel文件路径
     * StatrRow：读取的开始行数（默认填0）
     * EndRow：读取的结束行数（填-1为全部）
     * ExistTop:是否存在头部（如存在则读取数据时会把头部拼接到对应数据，若无则为当前列数）[true则以文件头作为map的key，否则以数字(所在列数)作为map的key]
     * 返回一个List<Map<String,Object>>
     */
    public static Map<String, List<LinkedHashMap<String, Object>>> ReadExcelByRC(String fileName, int StatrRow, int EndRow, boolean ExistTop) {
        //判断输入的开始值是否少于等于结束值
        if (StatrRow > EndRow && EndRow != -1) {
            logger.warn("输入的开始行值比结束行值大，请重新输入正确的行数");
            Map<String, List<LinkedHashMap<String, Object>>> error = null;
            return error;
        }
        //声明返回的结果集
        Map<String, List<LinkedHashMap<String, Object>>> result = new HashMap<>();
        //声明一个工作薄
        Workbook workbook = null;
        //声明一个文件输入流
        FileInputStream inputStream = null;
        try {
            // 获取Excel后缀名，判断文件类型
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
            // 获取Excel文件
            File excelFile = new File(fileName);
            if (!excelFile.exists()) {
                logger.warn("指定的Excel文件不存在！");
                return null;
            }
            // 获取Excel工作簿
            inputStream = new FileInputStream(excelFile);
            workbook = getWorkbook(inputStream, fileType);
            //处理Excel内容
            result = HandleData(workbook, StatrRow, EndRow, ExistTop);
        } catch (Exception e) {
            logger.warn("解析Excel失败，文件名：" + fileName + " 错误信息：" + e.getMessage());
        } finally {
            try {
                if (null != workbook) {
                    workbook.close();
                }
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (Exception e) {
                logger.warn("关闭数据流出错！错误信息：" + e.getMessage());
                return null;
            }
        }
        return result;
    }


//    /**
//     * 方法一
//     * 根据行数和列数读取Excel
//     * fileName:Excel文件路径
//     * StatrRow：读取的开始行数（默认填0）
//     * EndRow：读取的结束行数（填-1为全部）
//     * ExistTop:是否存在头部（如存在则读取数据时会把头部拼接到对应数据，若无则为当前列数）
//     * 返回一个List<Map<String,Object>>
//     */
//    public static Map<String,List<Map<String, Object>>> ReadExcelByRC(String fileName, int StatrRow, int EndRow, boolean ExistTop) {
//        //判断输入的开始值是否少于等于结束值
//        if (StatrRow > EndRow && EndRow != -1) {
//            logger.warn("输入的开始行值比结束行值大，请重新输入正确的行数");
//            Map<String,List<Map<String, Object>>> error = null;
//            return error;
//        }
//        //声明返回的结果集
//        Map<String,List<Map<String, Object>>> result = new HashMap<>();
//        //声明一个工作薄
//        Workbook workbook = null;
//        //声明一个文件输入流
//        FileInputStream inputStream = null;
//        try {
//            // 获取Excel后缀名，判断文件类型
//            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
//            // 获取Excel文件
//            File excelFile = new File(fileName);
//            if (!excelFile.exists()) {
//                logger.warn("指定的Excel文件不存在！");
//                return null;
//            }
//            // 获取Excel工作簿
//            inputStream = new FileInputStream(excelFile);
//            workbook = getWorkbook(inputStream, fileType);
//            //处理Excel内容
//            result = HandleData(workbook, StatrRow, EndRow, ExistTop);
//        } catch (Exception e) {
//            logger.warn("解析Excel失败，文件名：" + fileName + " 错误信息：" + e.getMessage());
//        } finally {
//            try {
//                if (null != workbook) {
//                    workbook.close();
//                }
//                if (null != inputStream) {
//                    inputStream.close();
//                }
//            } catch (Exception e) {
//                logger.warn("关闭数据流出错！错误信息：" + e.getMessage());
//                return null;
//            }
//        }
//        return result;
//    }
/**==============================================================================================================================**/

    /**
     * 方法二
     * 根据给定的实体类中赋值的注解值读取Excel
     * fileName:Excel文件路径
     * StatrRow：读取的开始行数（默认填0）
     * EndRow：读取的结束行数（填-1为全部）
     * Class<T>：传过来的实体类类型
     * 返回一个List<T>:T为实体类
     */
    public static List<Object> ReadExcelByPOJO(String fileName, int StatrRow, int EndRow, boolean onlyReadFirstSheet, Class t) throws InvocationTargetException, IntrospectionException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        //判断输入的开始值是否少于等于结束值
        if (StatrRow > EndRow && EndRow != -1) {
            logger.warn("输入的开始行值比结束行值大，请重新输入正确的行数");
            List<Object> error = null;
            return error;
        }
        //声明返回的结果集
        List<Object> result = new ArrayList<>();
        //声明一个工作薄
        Workbook workbook = null;
        //声明一个文件输入流
        FileInputStream inputStream = null;
        try {
            // 获取Excel后缀名，判断文件类型
            String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
            // 获取Excel文件
            File excelFile = new File(fileName);
            if (!excelFile.exists()) {
                logger.warn("指定的Excel文件不存在！");
                return null;
            }
            // 获取Excel工作簿
            inputStream = new FileInputStream(excelFile);
            workbook = getWorkbook(inputStream, fileType);
            //处理Excel内容
            result = HandleDataPOJO(workbook, StatrRow, EndRow, t, onlyReadFirstSheet);
        } catch (Exception e) {
            logger.warn("解析Excel失败，文件名：" + fileName + " 错误信息：" + e.getMessage());
        } finally {
            try {
                if (null != workbook) {
                    workbook.close();
                }
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (Exception e) {
                logger.warn("关闭数据流出错！错误信息：" + e.getMessage());
                return null;
            }
        }
        return result;
    }

    /**
     * 处理Excel内容转为List<T>输出
     * workbook：已连接的工作薄
     * StatrRow：读取的开始行数（默认填0，0开始,传过来是EXcel的行数值默认从1开始，这里已处理减1）
     * EndRow：读取的结束行数（填-1为全部）
     * Class<T>：所映射的实体类
     */
    private static <t> List<Object> HandleDataPOJO(Workbook workbook, int StatrRow, int EndRow, Class<?> t, boolean onlyReadFirstSheet) throws IntrospectionException, NoSuchFieldException, IllegalAccessException, InstantiationException, InvocationTargetException, ClassNotFoundException {
        //声明返回的结果集
        List<Object> result = new ArrayList<Object>();
        //解析sheet（sheet是Excel脚页）
        for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
            if (onlyReadFirstSheet && sheetNum > 0) {
                break;
            }
            Sheet sheet = workbook.getSheetAt(sheetNum);
            // 校验sheet是否合法
            if (sheet == null) {
                continue;
            }
            //获取头部数据
            //声明头部数据数列对象
            ArrayList<String> top = new ArrayList<>();
            //获取Excel第一行数据
            int firstRowNum = sheet.getFirstRowNum();
            Row firstRow = sheet.getRow(firstRowNum);
            if (null == firstRow) {
                logger.warn("解析Excel失败，在第一行没有读取到任何数据！");
                return null;
            }
            for (int i = 0; i < firstRow.getLastCellNum(); i++) {
                top.add(convertCellValueToString(firstRow.getCell(i)));
            }
            //获取实体类的成原变量
            Map<String, Object> POJOfields = getPOJOFieldAndValue(t);
            //判断所需要的数据列
            Map<String, Object> exceltoPOJO = new HashMap<>();
            for (int i = 0; i < top.size(); i++) {
                if (POJOfields.get(top.get(i)) != null && !"".equals(POJOfields.get(top.get(i)))) {
                    exceltoPOJO.put(String.valueOf(i), POJOfields.get(top.get(i)));
                }
            }
            /*处理Excel数据内容*/
            int endRowNum;
            //获取结束行数
            if (EndRow == -1) {
                endRowNum = sheet.getPhysicalNumberOfRows();
            } else {
                endRowNum = EndRow <= sheet.getPhysicalNumberOfRows() ? EndRow : sheet.getPhysicalNumberOfRows();
            }
            List<Map<String, Object>> mapList = new ArrayList<>();
            //遍历行数
            for (int i = StatrRow - 1; i < endRowNum; i++) {
//                if(i==endRowNum-1){
//                    System.out.println("sss");
//                }
                Row row = sheet.getRow(i);
                if (null == row) {
                    continue;
                }
                //获取需要的列数据
                t texcel = (t) t.newInstance();
                for (Map.Entry<String, Object> map : exceltoPOJO.entrySet()) {
                    //获取Exceld对应列的数据
                    String celldata = convertCellValueToString(row.getCell(Integer.parseInt(map.getKey())));
                    //使用发射
                    //获取实体类T中指定成员变量的对象
                    PropertyDescriptor pd = new PropertyDescriptor((String) map.getValue(), texcel.getClass());
                    //获取成员变量的set方法
                    Method method = pd.getWriteMethod();
                    //判断成员变量的类型
                    Field field = texcel.getClass().getDeclaredField((String) map.getValue());
                    String object = field.getGenericType().getTypeName();
                    if (object.endsWith("String")) {
                        //执行set方法
                        method.invoke(texcel, celldata);
                    }
                    if (object.endsWith("Double")) {
                        Double middata = Double.valueOf(celldata);
                        //执行set方法
                        method.invoke(texcel, middata);
                    }
                    if (object.endsWith("Float")) {
                        Float middata = Float.valueOf(celldata);
                        //执行set方法
                        method.invoke(texcel, middata);
                    }
                    if (object.endsWith("Integer")) {
                        Integer middata = Integer.parseInt(celldata);
                        //执行set方法
                        method.invoke(texcel, middata);
                    }
                }
                result.add(texcel);
            }
        }
        return result;
    }

    /**
     * 获取对应的实体类成员
     */
    private static Map<String, Object> getPOJOFieldAndValue(Class T) {
        //声明返回结果集
        Map<String, Object> result = new HashMap<>();
        Field[] fields = T.getDeclaredFields();//获取属性名
        if (fields != null) {
            for (Field field : fields) {
                ExcelResource Rescoure = field.getAnnotation(ExcelResource.class);
                if (Rescoure.value() != null && !"".equals(Rescoure.value())) {
                    result.put(Rescoure.value(), field.getName());
                }
            }
        } else {
            logger.warn("实体类：" + T + "不存在成员变量");
            return null;
        }
        return result;
    }

    /**
     * 向一个Excel工作簿中写入数据
     */
    public static <T> void writeExcel(String fileName, int sheetNum, List<T> dto) throws IOException, IllegalAccessException {
        //声明一个工作薄
        Workbook workbook = null;
        //声明一个文件输入流
        FileInputStream inputStream = null;
        // 获取Excel后缀名，判断文件类型
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        // 获取Excel文件
        File excelFile = new File(fileName);
        if (!excelFile.exists()) {
            logger.warn("指定的Excel文件不存在！");
        }
        // 获取Excel工作簿
        inputStream = new FileInputStream(excelFile);
        workbook = getWorkbook(inputStream, fileType);
        //处理Excel内容
        if (sheetNum <= workbook.getNumberOfSheets()) {
            Sheet sheet = workbook.getSheetAt(sheetNum - 1);
            // 校验sheet是否合法
            if (sheet != null) {
                //获取头部数据
                //声明头部数据数列对象
                ArrayList<String> top = new ArrayList<>();
                //获取Excel第一行数据
                int firstRowNum = sheet.getFirstRowNum();
                Row firstRow = sheet.getRow(firstRowNum);
                if (null == firstRow) {
                    logger.warn("解析Excel失败，在第一行没有读取到任何数据！");
                    return;
                }
                for (int i = 0; i < firstRow.getLastCellNum(); i++) {
                    top.add(convertCellValueToString(firstRow.getCell(i)));
                }
                //通过获得的头部信息匹配@ExcelResource注解进行属性的插入
                for (int j = 0; j < dto.size(); j++) {
                    T t = dto.get(j);
                    Row row = sheet.createRow(j + 1);//在指定位置创建一个新的Excel行对象,注意0行被表头占着
                    for (int i = 0; i < top.size(); i++) {//循环excel表中的每一列

                        Field[] fields = t.getClass().getDeclaredFields();//获取属性名
                        if (fields != null) {
                            for (Field field : fields) {
                                ExcelResource Rescoure = field.getAnnotation(ExcelResource.class);//获取当前属性上面注解
                                if (Rescoure.value() != null && top.get(i).equals(Rescoure.value())) {//如果注解中的值和表头一致，说明该属性应插入当前行的对应列位置
                                    field.setAccessible(true);//开启私有成员访问权限
                                    String str = (String) field.get(t);//获取该属性名的值
                                    row.createCell(i).setCellValue(str);
                                    break;
                                }
                            }
                            //row.createCell(i+1).setCellValue("");
                        } else {
                            logger.warn("实体类：" + t + "不存在成员变量");
                            return;
                        }
                    }


                }
                //输出到指定的excel中
                FileOutputStream outputStream = new FileOutputStream(excelFile);
                workbook.write(outputStream);
                outputStream.close();
            }

        }

    }


    /**
     * @param filePath:
     * @param map:
     * @return void
     * @author sunjian23
     * @description TODO：根据map生成Excel文件
     * @date 2022/10/29 20:43
     */
    public static void writeExcel(String filePath, Map<String, List<LinkedHashMap<String, Object>>> map, List<String> sheetList) throws IOException, IllegalAccessException {
        //声明一个工作薄
        Workbook workbook = null;
        // 创建Excel文件
        File excelFile = new File(filePath);
        if (!excelFile.exists()) {
            logger.warn("指定的Excel文件不存在！");
            //若不存在，则创建文件
            excelFile.createNewFile();
        }
        workbook = new SXSSFWorkbook();
        for (int i = 0; i < map.size(); i++) {
            //1.创建sheet页
            Sheet sheet = workbook.createSheet(sheetList.get(i));
            List<LinkedHashMap<String, Object>> list = map.get(sheetList.get(i));
            //空文件sheet,直接返回
            if (null == list) {
                continue;
            }
            //2.创建行
            for (int j = 0; j < list.size(); j++) {
                Row row = sheet.createRow(j);
                LinkedHashMap<String, Object> dataMap = list.get(j);
                //3.创建单元格
                int cellNum = 0;
                //空内容
                if (null == dataMap) {
                    continue;
                }
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    //4.写入数据
                    row.createCell(cellNum++).setCellValue((String) entry.getValue());
                }
            }
        }
        //写出文件
        FileOutputStream outputStream = new FileOutputStream(excelFile);
        workbook.write(outputStream);
        outputStream.close();
    }


    /**
     * @param filePath:
     * @param list:
     * @param isMapHasTitleRow: map中是否包含第一行标题行
     * @param expandColumn: 扩展列
     * @return void
     * @author sunjian23
     * @description TODO：根据list输出Excel（单Sheet页）
     * @date 2023/3/10 9:06
     */
    public static void writeExcel(String filePath, List<Map<String, String>> list, boolean isMapHasTitleRow,List<String> expandColumn) throws IOException{
        //声明一个工作薄
        Workbook workbook = null;
        // 创建Excel文件
        File excelFile = new File(filePath);
        if (!excelFile.exists()) {
            logger.warn("指定的Excel文件不存在！");
            //若不存在，则创建文件
            excelFile.createNewFile();
        }
        workbook = new SXSSFWorkbook();
        //1.创建sheet页
        Sheet sheet = workbook.createSheet();
        //空文件sheet,直接返回
        if (null == list) {
            return;
        }
        if(!isMapHasTitleRow){
            //创建标题行
            Row row = sheet.createRow(0);
            Set<String> keySet = list.get(0).keySet();
            Iterator<String> iterator = keySet.iterator();
            //创建单元格
            int cellNum = 0;
            while (iterator.hasNext()) {
                String next = iterator.next();
                row.createCell(cellNum++).setCellValue(next);
            }
            //补充扩展列
            for(int i=0;i<expandColumn.size();i++){
                row.createCell(cellNum++).setCellValue(expandColumn.get(i));
            }


        }
        //2.创建行
        for (int j = 0; j < list.size(); j++) {
            int index = j;
            if(!isMapHasTitleRow){
                index ++;
            }
            Row row = sheet.createRow(index);
            Map<String, String> dataMap = list.get(j);
            //3.创建单元格
            int cellNum = 0;
            //空内容
            if (null == dataMap) {
                continue;
            }
            for (Map.Entry<String, String> entry : dataMap.entrySet()) {
                //4.写入数据
                row.createCell(cellNum++).setCellValue(entry.getValue());
            }
            if(index==0){
                //补充扩展列
                for(int i=0;i<expandColumn.size();i++){
                    row.createCell(cellNum++).setCellValue(expandColumn.get(i));
                }
            }
        }

        //写出文件
        FileOutputStream outputStream = new FileOutputStream(excelFile);
        workbook.write(outputStream);
        outputStream.close();
    }


    public static String keyChange(String t) {
        if (t.contains("\\")) {
            String replace = t.replace("\\", ".");
            String substring = replace.substring(replace.indexOf(".") + 1);
            return substring;
        }
        return t;
    }


    public static String writeToSqlFile(List<LinkedHashMap<String, Object>> mapList, String idType, String tableName) throws IOException {
        if (null == mapList) {
            logger.warn("该Sheet页没有内容！");
            return null;
        }
        StringBuilder sqlString = new StringBuilder();
        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String sqlDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "excelToCreateAndInsert";
        File sqlDir = new File(sqlDirPath);
        //判断保存文件所对应路径是否存在
        if (!sqlDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            sqlDir.mkdirs();
        }
        //上传路径
        String uploadFilePath = sqlDir + File.separator + tableName + ".sql";
        File sqlFile = new File(uploadFilePath);
        //判断保存文件所对应路径是否存在
        if (!sqlFile.exists()) {
            //若不存在，则创建文件
            sqlFile.createNewFile();
        }
        //生成创建表sql
        sqlString.append(SQLUtil.create(tableName, mapList.get(0), idType));
        //删除第一行，第一行为标题行
        mapList.remove(0);
        if (mapList.size() > 0) {
            //生成插入数据sql
            sqlString.append("\n").append(SQLUtil.insert(tableName, mapList));
        }
        FileOutputStream outputStream = null;
        outputStream = new FileOutputStream(sqlFile.getAbsolutePath(), false);//注意这里的第二个参数true是指写入的时候不覆盖原有的内容
        outputStream.write(sqlString.toString().getBytes());
        outputStream.close();
        return sqlFile.getAbsolutePath();
    }

    /**
     * 功能描述:  将文件打包的方法，需要传一个压缩路径，和一个文件，一次只将一个文件写入压缩包
     *
     * @param filePath     文件路径
     * @param zipOut       压缩流
     * @param realFileName 真实的文件名
     * @return void
     */
    public static void fileToZip(String filePath, ZipOutputStream zipOut, String realFileName) throws IOException {
        // 需要压缩的文件
        File file = new File(filePath);
        //创建文件输入流
        FileInputStream fileInput = new FileInputStream(filePath);
        // 缓冲
        byte[] bufferArea = new byte[1024 * 10];
        BufferedInputStream bufferStream = new BufferedInputStream(fileInput, 1024 * 10);
        // 将当前文件作为一个zip实体写入压缩流,realFileName代表压缩文件中的文件名称
        zipOut.putNextEntry(new ZipEntry(realFileName));
        int length = 0;
        // 写操作
        while ((length = bufferStream.read(bufferArea, 0, 1024 * 10)) != -1) {
            zipOut.write(bufferArea, 0, length);
        }
        //关闭流
        fileInput.close();
        // 需要注意的是缓冲流必须要关闭流,否则输出无效
        bufferStream.close();
        // 压缩流不必关闭,使用完后再关
    }


    /**
     * 将key进行转换+筛选 (所有的/重复的且中文相同的/重复的且中文不同的，分为三个sheet来管理)
     *
     * @param excelClassPath
     * @param startRow
     * @param endRow
     * @param FinalFileClassPath
     * @param clazz
     * @throws Exception
     */
    public static <T> void keyChangeAndFilter(String excelClassPath, Integer startRow, Integer endRow, String FinalFileClassPath, Class<T> clazz) throws Exception {

        //读取Excel中的内容
        List<Object> list = ExcelUtil.ReadExcelByPOJO(excelClassPath, startRow, endRow, true, clazz);
        if (null == list) {
            throw new RuntimeException("上传失败：读取内容为空");
        }
        if (list.size() == 0) {
            throw new RuntimeException("上传失败:未读取到规定字段的数据(key/中文（简体）)，请检查文件格式是否正确");
        }
        List<DuplicateKeyBean> allLists = new ArrayList<>();
        Set<String> keyDuplicateChineseSameKeys = new HashSet<>();
        Set<String> keyDuplicateChineseDiffKeys = new HashSet<>();
        List<DuplicateKeyBean> keyDuplicateChineseSameLists = new ArrayList<>();
        List<DuplicateKeyBean> keyDuplicateChineseDiffLists = new ArrayList<>();
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
                    int firstIndex = key.indexOf('\\');
                    if (firstIndex != -1) {//index不是-1的时候说明该key含有反斜杠，进行裁切存入
                        String replaceKey = key.substring(firstIndex + 1).replace('\\', '.');
                        //1.遍历allLists，看是否存在了这个key
                        allLists.stream().forEach(duplicateBean -> {
                            if (replaceKey.equals(duplicateBean.getNewKey())) {
                                //2.key相同，看value是否相同
                                if (value.equals(duplicateBean.getZh_CN())) {
                                    //3.中文相同放在keyDuplicateChineseSameKeys中
                                    keyDuplicateChineseSameKeys.add(replaceKey);
                                } else {
                                    //4.中文不同放在keyDuplicateChineseDiffKeys中
                                    keyDuplicateChineseDiffKeys.add(replaceKey);
                                }
                            }
                        });
                        allLists.add(new DuplicateKeyBean(key, replaceKey, value));

                    } else {
                        //1.遍历allLists，看是否存在了这个key
                        allLists.stream().forEach(duplicateBean -> {
                            if (key.equals(duplicateBean.getNewKey())) {
                                //2.key相同，看value是否相同
                                if (value.equals(duplicateBean.getZh_CN())) {
                                    //3.中文相同放在keyDuplicateChineseSameKeys中
                                    keyDuplicateChineseSameKeys.add(key);
                                } else {
                                    //4.中文不同放在keyDuplicateChineseDiffKeys中
                                    keyDuplicateChineseDiffKeys.add(key);
                                }
                            }
                        });
                        allLists.add(new DuplicateKeyBean(key, key, value));
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
        //开始组装三个lists，
        //组装keyDuplicateChineseSameLists
        keyDuplicateChineseSameKeys.stream().forEach(newKey -> {
            allLists.stream().forEach(duplicateKeyBean -> {
                if (newKey.equals(duplicateKeyBean.getNewKey())) {
                    keyDuplicateChineseSameLists.add(duplicateKeyBean);
                }
            });
        });
        //组装keyDuplicateChineseDiffLists
        keyDuplicateChineseDiffKeys.stream().forEach(newKey -> {
            allLists.stream().forEach(duplicateKeyBean -> {
                if (newKey.equals(duplicateKeyBean.getNewKey())) {
                    keyDuplicateChineseDiffLists.add(duplicateKeyBean);
                }
            });
        });
        //写出Excel文件
        //声明一个工作薄
        Workbook workbook = null;
        // 创建Excel文件
        File excelFile = new File(FinalFileClassPath);
        if (!excelFile.exists()) {
            logger.warn("指定的Excel文件不存在！");
            //若不存在，则创建文件
            excelFile.createNewFile();
        }
        workbook = new SXSSFWorkbook();
        //创建Sheet
        Sheet sheet1 = workbook.createSheet("全部key+中文");
        Sheet sheet2 = workbook.createSheet("key相同且中文相同");
        Sheet sheet3 = workbook.createSheet("key相同但中文不同");
        //创建标题
        //创建行
        Row titleRow1 = sheet1.createRow(0);
        Row titleRow2 = sheet2.createRow(0);
        Row titleRow3 = sheet3.createRow(0);
        Field[] fields = DuplicateKeyBean.class.getDeclaredFields();//获取属性名
        int count = 0;
        for (Field field : fields) {
            ExcelResource Rescoure = field.getAnnotation(ExcelResource.class);
            if (Rescoure.value() != null && !"".equals(Rescoure.value())) {
                //3.写入数据
                titleRow1.createCell(count).setCellValue(Rescoure.value());
                titleRow2.createCell(count).setCellValue(Rescoure.value());
                titleRow3.createCell(count++).setCellValue(Rescoure.value());
            }
        }


        //第一个sheet写入
        for (int i = 1; i <= allLists.size(); i++) {
            //创建行
            Row row = sheet1.createRow(i);
            //创建单元格
            DuplicateKeyBean duplicateKeyBean = allLists.get(i - 1);
            //2.创建行
            int column = 0;
            //3.写入数据
            row.createCell(column++).setCellValue(duplicateKeyBean.getOldKey());
            row.createCell(column++).setCellValue(duplicateKeyBean.getNewKey());
            row.createCell(column++).setCellValue(duplicateKeyBean.getZh_CN());

        }
        //第二个sheet写入
        for (int i = 1; i <= keyDuplicateChineseSameLists.size(); i++) {
            //创建行
            Row row = sheet2.createRow(i);
            //创建单元格
            DuplicateKeyBean duplicateKeyBean = keyDuplicateChineseSameLists.get(i - 1);
            //2.创建行
            int column = 0;
            //3.写入数据
            row.createCell(column++).setCellValue(duplicateKeyBean.getOldKey());
            row.createCell(column++).setCellValue(duplicateKeyBean.getNewKey());
            row.createCell(column++).setCellValue(duplicateKeyBean.getZh_CN());

        }
        //第三个sheet写入
        for (int i = 1; i <= keyDuplicateChineseDiffLists.size(); i++) {
            //创建行
            Row row = sheet3.createRow(i);
            //创建单元格
            DuplicateKeyBean duplicateKeyBean = keyDuplicateChineseDiffLists.get(i - 1);
            //2.创建行
            int column = 0;
            //3.写入数据
            row.createCell(column++).setCellValue(duplicateKeyBean.getOldKey());
            row.createCell(column++).setCellValue(duplicateKeyBean.getNewKey());
            row.createCell(column++).setCellValue(duplicateKeyBean.getZh_CN());

        }
        //写出文件
        FileOutputStream outputStream = new FileOutputStream(excelFile);
        workbook.write(outputStream);
        outputStream.close();


    }


}
