package com.example.exceltosql.connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author :sunjian23
 * @date : 2022/10/17 19:48
 */
public class test {
    public static void main(String[] args) {
        try {
            String Driver="org.postgresql.Driver"; //连接数据库的方法
            String URL="jdbc:postgresql://10.19.219.44:5432/postgres?useUnicode=true&characterEncoding=utf-8"; //db_name为数据可名
            String Username="postgres"; //用户名
            String Password="Abc12345"; //密码
            Class.forName(Driver).newInstance();
            Connection con= DriverManager.getConnection(URL,Username,Password);
            ResultSet tables1 = con.getMetaData().getColumns(null, null, "click_statistics_log", null);
            while (tables1.next()) {
                Object object = tables1.getObject(1);
                List<byte[]> rows = (List<byte[]> )tables1.getObject("rows");
                byte[] bytes = rows.get(0);
                System.out.println("1:"+String.valueOf(bytes));
            }
            String Driver2="org.postgresql.Driver"; //连接数据库的方法
            String URL2="jdbc:postgresql://10.1.77.101:5432/postgres?useUnicode=true&characterEncoding=utf-8"; //db_name为数据可名
            String Username2="postgres"; //用户名
            String Password2="Abc123456"; //密码
            Class.forName(Driver2).newInstance();
            Connection con2= DriverManager.getConnection(URL2,Username2,Password2);
            ResultSet tables = con2.getMetaData().getColumns(null, null, "keyword_task_table", null);
            while (tables.next()) {
                String TABLE_CAT = tables.getString("id");
                System.out.println("2:"+TABLE_CAT);
            }

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

    }
}
