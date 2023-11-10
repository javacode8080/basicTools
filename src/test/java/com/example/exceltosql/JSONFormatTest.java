package com.example.exceltosql;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.example.exceltosql.Util.JSONUtil;
import com.example.exceltosql.dto.HttpResponseDto;
import com.example.exceltosql.entity.JsonBean;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :sunjian23
 * @date : 2022/10/29 16:39
 */
@SpringBootTest
public class JSONFormatTest {

//    @Test
//    public void jsonFormatTest(){
//        String s ="{\"data\":{\"pageSize\":20,\"rows\":[{\"chinese\":\"The name can only be number, letter, Chinese, -, #;\",\"product_line\":\"Common\",\"productline\":\"Common\",\"multi_language_management_id\":149840,\"type\":\"name\",\"resource_sign\":\"isfd\",\"version\":\"1.4.100002\",\"key\":\"pcs.person.excel.validate.personName.format\"},{\"chinese\":\"The name can only be number, letter, Chinese, -, #;\",\"product_line\":\"Common\",\"productline\":\"Common\",\"multi_language_management_id\":149840,\"type\":\"name\",\"resource_sign\":\"isfd\",\"version\":\"1.4.100014\",\"key\":\"pcs.person.excel.validate.personName.format\"},{\"chinese\":\"Chinese to Pinyin exception\",\"product_line\":\"Common\",\"productline\":\"Common\",\"multi_language_management_id\":322246,\"type\":\"msg\",\"resource_sign\":\"ibuilding\",\"version\":\"1.3.1\",\"key\":\"errorCode.0x1500a008.description\"},{\"chinese\":\"Chinese to Pinyin exception\",\"product_line\":\"Common\",\"productline\":\"Common\",\"multi_language_management_id\":322246,\"type\":\"msg\",\"resource_sign\":\"ibuilding\",\"version\":\"1.2.1001\",\"key\":\"errorCode.0x1500a008.description\"},{\"chinese\":\"Chinese Simplified - 简体中文\",\"product_line\":\"Common\",\"productline\":\"Common\",\"multi_language_management_id\":526592,\"english\":\"Chinese Simplified - 简体中文\",\"type\":\"name-local\",\"resource_sign\":\"nrs\",\"version\":\"1.0.100\",\"key\":\"zh_CN\"}],\"total\":5,\"totalPage\":1,\"page\":1},\"errorCode\":\"200\",\"code\":\"200\"}";
//        String s2 ="{\"data\":{\"pageSize\":20,\"rows\":[\"zh\",\"ying\",\"mei\",\"fa\"]}}";
//        String s3 ="{\"key1\":\"{}\",\"key2\":\"{}\"}";
//        String JsonFormat = JSONUtil.formatJson(s3);
//        System.out.println(JsonFormat);
//    }


    @Test
    public void jsonFormatTest2(){
//        Map<String, String> map = new HashMap<>();
//        map.put("key","asbdcu\"{}\"ds,hiu,dsa");
//        map.put("key2","cu\"{}dshiudsa");
//
//        String pretty = JSON.toJSONString(map, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
//                SerializerFeature.WriteDateUseDateFormat);
//
//        System.out.println(pretty);
//        String s="{\"key\"\"asbdcu\"}";
//        JSONObject jsonObject = JSON.parseObject(s);
//        System.out.println(jsonObject);
    }

    @Test
    public void test1(){
        HttpResponseDto<List<JsonBean>> dto = new HttpResponseDto<>();

        List<JsonBean> jsonBeans = new ArrayList<>();
        JsonBean jsonBean = new JsonBean("key1", "value1");
        JsonBean jsonBean2 = new JsonBean("key2", "value2");
        JsonBean jsonBean3 = new JsonBean("key3", "value3");
        jsonBeans.add(jsonBean);
        jsonBeans.add(jsonBean2);
        jsonBeans.add(jsonBean3);
        dto.setData(jsonBeans);
        dto.setMessage("message");
        dto.setCode("code");
        String s = JSON.toJSONString(dto);
        HttpResponseDto<List<JsonBean>> dto1 = JSONObject.parseObject(s, HttpResponseDto.class);
        System.out.println();

    }

}
