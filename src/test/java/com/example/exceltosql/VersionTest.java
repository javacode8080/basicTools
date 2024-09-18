package com.example.exceltosql;

import com.alibaba.fastjson.JSONObject;
import com.example.exceltosql.Util.ExcelUtil;
import com.example.exceltosql.dto.JsonRootBean;
import com.example.exceltosql.dto.VersionsDTO;
import javafx.util.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author :sunjian23
 * @date : 2023/12/11 16:00
 */
@SpringBootTest(classes = VersionTest.class)
public class VersionTest {

    public void writeExcel(List<LinkedHashMap<String, Object>> linkedHashMaps, String savePathRoot, String productLine, Pair<String, String> cookie) {

        for (LinkedHashMap<String, Object> map : linkedHashMaps) {
            try {
                //1.首先生成的是组件的相关信息
//                String json = "{ \"resourceSign\": \"cluster\", \"version\": \"2.8.1\", \"exist\": true }";
//                Map<String, Object> map = JSONObject.parseObject(json, Map.class);
                //先要判断这个版本在不在Hirule里面，不在就跳过
                if (!(boolean) map.get("exist")) {
                    continue;
                }
                String resourceSign = (String) map.get("resourceSign");
                String version = (String) map.get("version");
//                String productLine = "HikCentral Master Lite";
                //计算保存路径
//                String savePathRoot = "F:\\2.业务\\0.公司相关任务\\7.HCM201+HCMLite221产品导出待翻译文件\\HCMLite221\\待翻译文件\\";

                //2.拼接出来json请求体
                String body = "{\"resourceModelList\":[{\"resourceSign\":" +
                        "\"" + resourceSign + "\"," +
                        "\"version\":" +
                        "\"" + version + "\"" +
                        "}],\"languageCodeList\":[\"zh_CN\",\"en_US\",\"tr\"],\"productLine\":" +
                        "\"" + productLine + "\",\"componentTypeList\":[\"component\",\"patch\"]}";
                //3.发起第一个请求
                String url = "http://hirule.hikvision.com/highwayMultiLanguage/getUntranslatedInfo";
                CloseableHttpClient client = HttpClientBuilder.create().build();
                HttpPost httpPost = new HttpPost(url);
                httpPost.addHeader(cookie.getKey(), cookie.getValue());
                httpPost.addHeader("Content-Type", "application/json");
                //设置json请求体
                httpPost.setEntity(new StringEntity(body));
                //执行http请求
                CloseableHttpResponse response = client.execute(httpPost);
                String result = EntityUtils.toString(response.getEntity(), "utf-8");
                JsonRootBean jsonRootBean = JSONObject.parseObject(result, JsonRootBean.class);
                if (jsonRootBean.getData().getStatus().equals("进行中")) {
                    //4.每个五秒钟循环查询一下状态
                    boolean flag = true;
                    while (flag) {
                        String url2 = "http://hirule.hikvision.com/highwayMultiLanguage/getTaskResult?id=" + jsonRootBean.getData().getTaskId();
                        CloseableHttpClient client2 = HttpClientBuilder.create().build();

                        HttpGet httpGet = new HttpGet(url2);
                        //添加请求头
                        httpGet.addHeader(cookie.getKey(), cookie.getValue());
                        //执行http请求
                        CloseableHttpResponse response2 = client2.execute(httpGet);
                        String result2 = EntityUtils.toString(response2.getEntity(), "utf-8");

                        JsonRootBean jsonRootBean2 = JSONObject.parseObject(result2, JsonRootBean.class);
                        if (jsonRootBean2.getData().getTaskResult().equals("未翻译文件导出成功！")) {
                            //5.发起第3个请求下载文件到本地
                            String url3 = "http://hirule.hikvision.com/" + jsonRootBean2.getData().getFilePath();
                            String encodeUrl3 = convertSpaceToURLEncoding(url3);
                            CloseableHttpClient client3 = HttpClientBuilder.create().build();
                            HttpGet httpGet2 = new HttpGet(encodeUrl3);
                            //执行http请求
                            CloseableHttpResponse response3 = client3.execute(httpGet2);
                            HttpEntity entity = response3.getEntity();
                            int responseCode = response3.getStatusLine().getStatusCode();
                            if (responseCode == HttpStatus.SC_OK) {
                                InputStream inputStream = entity.getContent();
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
                                String savePath = savePathRoot + resourceSign + "_" + version + "_" + sdf.format(new Date()) + ".xlsx";
                                OutputStream outputStream = new FileOutputStream(new File(savePath));

                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    outputStream.write(buffer, 0, bytesRead);
                                }

                                outputStream.flush();
                                outputStream.close();
                                inputStream.close();
                                System.out.println("文件下载完成！" + resourceSign + "_" + version + "_" + new Date().getTime() + ".xlsx" + "<-------------->" + jsonRootBean2.getData().getFilePath());
                            }
                            flag = false;

                        } else {
                            try {
                                Thread.sleep(5000);
//                                System.out.println("沉睡5s");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


    @Test
    public void test() {
        try {
            List<LinkedHashMap<String, Object>> linkedHashMaps = new ArrayList<>();

            String filePath = "F:\\2.业务\\11.HCM211_HCML231项目\\HikCentral Master_2.0.1_Lite230产品构成.xlsx";
            String savePathRoot = "F:\\2.业务\\11.HCM211_HCML231项目\\待翻译文件\\";
            String productLine = "HikCentral Master";
            Pair<String, String> cookie = new Pair<String, String>("Cookie", "JSESSIONID=4B08A20150F0560C005C30AEFDEAD61A");
            Map<String, List<LinkedHashMap<String, Object>>> map = ExcelUtil.ReadExcelByRC(filePath, 0, Integer.MAX_VALUE, true);
            Set<String> set = new HashSet<>();
            set.add("标识");
            set.add("版本");
            Map<String, Set<String>> filters = new HashMap<>();
            filters.put("核心服务", set);
            filters.put("构架", set);
            filters.put("组件", set);
            filters.put("移动应用", set);
            Map<String, String> componentAndVersion = getComponentAndVersion(map, filters);
            Iterator<Map.Entry<String, String>> iterator = componentAndVersion.entrySet().iterator();
            while (iterator.hasNext()) {
                LinkedHashMap<String, Object> resultType = new LinkedHashMap();
                Map.Entry<String, String> next = iterator.next();
                String ResourceSign = next.getKey();
                String version = next.getValue();
                String url = "http://hirule.hikvision.com/highwayMultiLanguage/getResourceSignVersions?resourceSign=" + ResourceSign;
                CloseableHttpClient client = HttpClientBuilder.create().build();
                HttpPost httpPost = new HttpPost(url);
                httpPost.addHeader(cookie.getKey(), cookie.getValue());
                //设置请求头Content-Type=application/json
                httpPost.addHeader("Content-Type", "application/json");
                //执行http请求
                CloseableHttpResponse response = client.execute(httpPost);
                String result = EntityUtils.toString(response.getEntity(), "utf-8");
                VersionsDTO versionsDTO = JSONObject.parseObject(result, VersionsDTO.class);
                List<String> data = versionsDTO.getData();
                boolean contains = data.contains(version);
                resultType.put("resourceSign", ResourceSign);
                resultType.put("version", version);
                resultType.put("exist", contains);
                linkedHashMaps.add(resultType);
            }
            System.out.println(JSONObject.toJSONString(linkedHashMaps));
            writeExcel(linkedHashMaps, savePathRoot, productLine, cookie);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, String> getComponentAndVersion(Map<String, List<LinkedHashMap<String, Object>>> map, Map<String, Set<String>> filters) {
        Set<String> keySet = filters.keySet();
        //1.过滤出需要的sheet
        map.keySet().retainAll(keySet);
        Iterator<Map.Entry<String, List<LinkedHashMap<String, Object>>>> iterator = map.entrySet().iterator();
        //2.记录最后结果的map
        Map<String, String> map2 = new LinkedHashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, List<LinkedHashMap<String, Object>>> next = iterator.next();
            List<LinkedHashMap<String, Object>> value = next.getValue();
            value.forEach((map1) -> {
                String resourceSign = (String) map1.get("标识");
                String version = (String) map1.get("版本");
                if (!resourceSign.equals("标识") && !version.equals("版本")) {
                    version = version.substring(0, version.lastIndexOf("."));
                    map2.put(resourceSign, version);
                }
            });
        }
        return map2;
    }

    public static String convertSpaceToURLEncoding(String url) {
        return url.replace(" ", "%20");
    }
}
