package com.example.exceltosql.Util;

import com.example.exceltosql.entity.SqlBean;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.exceltosql.constant.SqlType.*;

/**
 * @author :sunjian23
 * @date : 2022/10/19 11:35
 */
public class SQLUtil {

    public static StringBuilder create(String tableName, Map<String, Object> column, String idType) {
        StringBuilder builder = new StringBuilder();
        builder.append(CREATE.getSql()).append("\"" + tableName + "\"").append("(\n\t\t");
        if ("Serial".equals(idType)) {
            builder.append("id " + SERIAL.getSql() + " " + NOTNULL.getSql() + ",").append("\n");
        } else if ("UUID".equals(idType)) {
            builder.append("id" + UUID.getSql() + NOTNULL.getSql() + DEFAULT.getSql() + UUID_GENERATE.getSql() + ",").append("\n");
        }
        Iterator<String> iterator = column.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            builder.append("\t\t\"" + next + "\"").append(VARCHAR_1000.getSql()).append(",\n");
        }
        builder.append(CONSTRAINT.getSql() + "\"" + tableName + "_pkey" + "\"" + PRIMARYKEYBYID.getSql()).append("\n);");
        return builder;
    }

    public static StringBuilder insert(String tableName, List<LinkedHashMap<String, Object>> column) {
        if (null == column || column.size() == 0) {
            return new StringBuilder("");
        }
        StringBuilder builder = new StringBuilder();
        builder.append(INSERT.getSql() + "\"" + tableName + "\"" + "\t(");
        Iterator<String> iterator = column.get(0).keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            builder.append("\"" + next + "\"").append(",");
        }
        builder.delete(builder.length() - 1, builder.length());
        builder.append(")\n\t\tVALUES\n");
        column.forEach(map -> {
            Iterator<String> keyIterator = map.keySet().iterator();
            builder.append("(");
            while (keyIterator.hasNext()) {
                String next = keyIterator.next();
                //如果要是字段中出现'，就会导致sql识别出现问题，因此将'转化为''【DBeaver中就是这样实现的】
                if (null != map.get(next)) {
                    String value = (String) map.get(next);
                    String replace = value.replace("'", "''");
                    builder.append("'" + replace + "'").append(",");
                } else {
                    builder.append("'" + "'").append(",");
                }
            }
            builder.delete(builder.length() - 1, builder.length());
            builder.append("),\n");
        });
        builder.delete(builder.length() - 2, builder.length());
        builder.append(";");
        return builder;
    }


    public static <T> void temp_newtableInsertSql(String excelClassPath, Integer startRow, Integer endRow, String FinalFileClassPath, Class<T> clazz) throws Exception {
        File sqlFile = new File(FinalFileClassPath);
        //判断保存文件所对应路径是否存在
        if (!sqlFile.exists()) {
            //若不存在，则创建文件
            sqlFile.createNewFile();
        }
        //定义基本的sql语段
        StringBuilder sql = new StringBuilder("truncate table temp_newtable;\n insert into temp_newtable (\"key\",chinese) values\n");
        //读取Excel中的内容
        List<Object> list = ExcelUtil.ReadExcelByPOJO(excelClassPath, startRow, endRow, true, clazz);
        if (null == list) {
            throw new RuntimeException("上传失败:读取内容为空");
        }
        if (list.size() == 0) {
            throw new RuntimeException("上传失败:未读取到规定字段的数据(Key/现中文)，请检查文件格式是否正确，格式详见wiki");
        }
        if (list.stream().filter(o -> {
            SqlBean o1 = (SqlBean) o;
            return StringUtils.isNotEmpty(o1.getKey()) && StringUtils.isNotEmpty(o1.getZh_CN());
        }).collect(Collectors.toList()).isEmpty()) {
            throw new RuntimeException("未获取到指定标题行(Key/现中文)");
        }
//        System.out.println("1");
        list.stream().forEach(obj -> {
            T bean = (T) obj;
            String key = null;
            String value = null;
            try {
                Method getKey = clazz.getMethod("getKey");//这里get方法名是不变的，所以可以写死
                Method getZh_CN = clazz.getMethod("getZh_CN");//这里get方法名是不变的，所以可以写死
                key = (String) getKey.invoke(bean);
                value = (String) getZh_CN.invoke(bean);

                if ((StringUtils.isEmpty(key) && StringUtils.isNotEmpty(value)) || (StringUtils.isNotEmpty(key) && StringUtils.isEmpty(value))) {
                    throw new RuntimeException("上传失败:不允许出现只有key或只有中文的情况，请检查文件是否正确，格式详见wiki");
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            if (StringUtils.isNotEmpty(key) && StringUtils.isNotEmpty(value)) {
                //2022.12.28:由于用户在书写Excel时总会莫名的多加入一些空格，因此这里先去除前后得空格
                key = key.trim();
                value = value.trim();
                //如果要是字段中出现'，就会导致sql识别出现问题，因此将'转化为''【DBeaver中就是这样实现的】
                if (value.contains("'")) {
                    value = value.replace("'", "''");
                }
                int firstIndex = key.indexOf('\\');
                if (firstIndex != -1) {//index不是-1的时候说明该key含有反斜杠，进行裁切存入
                    String replaceKey = key.substring(firstIndex + 1).replace('\\', '.');
                    sql.append("(" + "\'" + replaceKey + "\'" + "," + "\'" + value + "\'" + ")" + "," + "\n");
                } else {
                    sql.append("(" + "\'" + key + "\'" + "," + "\'" + value + "\'" + ")" + "," + "\n");
                }
            }
        });
        sql.delete(sql.length() - 2, sql.length());
        sql.append(";");
//        System.out.println(sql);
        FileOutputStream outputStream = null;
        outputStream = new FileOutputStream(sqlFile.getAbsolutePath(), false);//注意这里的第二个参数true是指写入的时候不覆盖原有的内容
        outputStream.write(sql.toString().getBytes());
        outputStream.close();
    }
}
