package com.example.exceltosql;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author :sunjian23
 * @date : 2024/4/8 9:12
 */
@SpringBootTest
public class CsvTest {

    @Test
    public void test() throws Exception {

        String fileName = "C:\\Users\\sunjian23\\Desktop\\CrossInfo_2024-04-07_190339_+0800\\CrossBasicInfo.csv";

        List<String[]> list = CsvTest.readCsvByCsvReader(fileName);
        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < list.get(i).length; j++) {
                System.out.println(list.get(i)[j]);
            }
        }
    }


    public static List<String[]> readCsvByCsvReader(String filePath) {
        List<String[]> arrList = new ArrayList<String[]>();
        try {
            CsvReader reader = new CsvReader(filePath, ',', Charset.forName("GBK"));
            while (reader.readRecord()) {
                arrList.add(reader.getValues()); // 按行读取，并把每一行的数据添加到list集合
            }
            reader.close();
            System.out.println("读取的行数：" + arrList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return arrList;
    }

    @Data
    @AllArgsConstructor
    class Person {
        private String name;
        private String address;
    }

    @Test
    public void test2() throws Exception {
        Person person = new Person("John", "JohnAdd");
        change(person);
        System.out.println(person.getName()+"---------"+person.getAddress());
    }

    public void change(Person person){
        person.setName("Tom");
        person.setAddress("TomAdd");
    }
}
