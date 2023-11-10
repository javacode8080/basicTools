package com.example.exceltosql;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author :sunjian23
 * @date : 2023/8/18 4:35
 */
public class yamlTest3 {


    public static void removeUnnecessaryInterfaceInformation(String yamlFilePath, String yamlOutputFilePath, List<String> serviceNameList, List<String> serviceGroupList) throws IOException {
        // 加载yaml文件
        InputStream input = new FileInputStream(yamlFilePath);
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(input);
        //1.根据提供方服务名称获取pIdList
        List<String> pIdList = getPIdList(data, serviceNameList);

        //2.根据服务分组获取groupIdList(会根据服务分组id查询所有子分组id[递归])
        List<String> groupIdList = new ArrayList<>();
        getGroupIdList(data, serviceGroupList, groupIdList);
        //1.移除指定的pid信息接口
        removePId(data, pIdList);
        //2.移除指定的接口分组层级
        removeGroupId(data, groupIdList);
        //将yaml格式化成json
        String jsonString = JSONObject.toJSONString(data);
        //转化为json
        JsonNode jsonNodeTree = new ObjectMapper().readTree(jsonString);
        //jaosn转化为yaml
        String jsonAsYaml = new YAMLMapper().writeValueAsString(jsonNodeTree);
        //todo：输出时尽量使用输出流形式，这样不会丢失字节，直接采用FileWriter()可能会丢失数据
        FileOutputStream outputStream = null;
        outputStream = new FileOutputStream(yamlOutputFilePath, false);//注意这里的第二个参数true是指写入的时候不覆盖原有的内容
        outputStream.write(jsonAsYaml.getBytes());
        outputStream.close();
    }

    private static void getGroupIdList(Map<String, Object> data, List<String> serviceGroupList, List<String> groupIdList) {
        //0.校验
        if (null == serviceGroupList || serviceGroupList.isEmpty()) {
            return;
        }
        List<String> missingServiceGroupList = new ArrayList<String>();
        //1.获取pId及pName
        Object group = data.get("group");
        if (group instanceof ArrayList) {
            List<LinkedHashMap> groupList = (ArrayList<LinkedHashMap>) group;
            for (String serviceGroup : serviceGroupList) {
                Optional<LinkedHashMap> groupMap = groupList.stream().filter(map -> map.get("groupName").equals(serviceGroup)).findFirst();
                if (!groupMap.isPresent()) {
                    missingServiceGroupList.add(serviceGroup);
                } else {
                    String serviceGroupId = groupMap.get().get("groupId").toString();
                    getGroupIdList(groupList, serviceGroupId, groupIdList);
                }

            }
            if (!missingServiceGroupList.isEmpty()) {
                throw new RuntimeException("以下服务分组名称错误/不存在" + JSONObject.toJSONString(missingServiceGroupList));
            }
        } else {
            throw new RuntimeException("yaml文件缺少provider字段/provider字段格式不正确，请检查文件格式！");
        }

    }

    private static void getGroupIdList(List<LinkedHashMap> groupList, String serviceGroupId, List<String> groupIdList) {
        groupIdList.add(serviceGroupId);
        List<String> childrenGroupIdList = groupList.stream().filter(map -> map.get("parentId").toString().equals(serviceGroupId)).map(map -> map.get("groupId").toString()).collect(Collectors.toList());
        if (childrenGroupIdList.isEmpty()) {
            return;
        } else {
            for (String childrenGroupId : childrenGroupIdList) {
                getGroupIdList(groupList, childrenGroupId, groupIdList);
            }
        }
    }

    private static List<String> getPIdList(Map<String, Object> data, List<String> serviceNameList) {
        //0.校验
        if (null == serviceNameList || serviceNameList.isEmpty()) {
            return null;
        }
        //1.获取pId及pName
        Object provider = data.get("provider");
        if (provider instanceof ArrayList) {
            List<LinkedHashMap> providerList = (ArrayList<LinkedHashMap>) provider;
            Map<String, String> collect = providerList.stream().filter(map -> {
                Object pName = map.get("pName");
                boolean contains = serviceNameList.contains(pName.toString());
                return contains;
            }).collect(Collectors.toMap(
                    map -> map.get("pName").toString(),
                    map -> map.get("pId").toString()
            ));
            //2.判断serviceNameList和collect的key值是否对应，不对应说明存在用户写错的内容
            List<String> collect1 = serviceNameList.stream().filter(serviceName -> !collect.containsKey(serviceName)).collect(Collectors.toList());
            if (!collect1.isEmpty()) {
                throw new RuntimeException("以下服务名称错误/不存在" + JSONObject.toJSONString(collect1));
            }
            //3.返回获取的pIdList
            return new ArrayList<>(collect.values());

        } else {
            throw new RuntimeException("yaml文件缺少provider字段/provider字段格式不正确，请检查文件格式！");
        }
    }

    private static void removeGroupId(Map<String, Object> data, List<String> groupIdList) {
        //0.校验pIdList
        if (null == groupIdList || groupIdList.isEmpty()) {
            return;
        }
        //1.删除group中的groupId,同时删除所以以当前groupId为parentId的接口信息
        Object provider = data.get("group");
        if (provider instanceof ArrayList) {
            List<LinkedHashMap> providerList = (ArrayList<LinkedHashMap>) provider;
            List<LinkedHashMap> collect = providerList.stream().filter(map -> {
                Object groupId = map.get("groupId");
                Object parentId = map.get("parentId");
                boolean contains = groupIdList.contains(groupId.toString()) || groupIdList.contains(parentId.toString());
                return !contains;
            }).collect(Collectors.toList());
            data.put("group", collect);
        } else {
            throw new RuntimeException("yaml文件缺少group字段/group字段格式不正确，请检查文件格式！");
        }
        //2.删除api中的groupId
        Object api = data.get("api");
        if (api instanceof ArrayList) {
            List<LinkedHashMap> apiList = (ArrayList<LinkedHashMap>) api;
            List<LinkedHashMap> collect = apiList.stream().filter(map -> {
                LinkedHashMap apiProviderInfo = (LinkedHashMap) map.get("apiGroupInfo");
                Object pId = apiProviderInfo.get("groupId");
                boolean contains = groupIdList.contains(pId.toString());
                return !contains;
            }).collect(Collectors.toList());
            data.put("api", collect);
        } else {
            throw new RuntimeException("yaml文件缺少api字段/api字段格式不正确，请检查文件格式！");
        }
    }

    private static void removePId(Map<String, Object> data, List<String> pIdList) {
        //0.校验pIdList
        if (null == pIdList || pIdList.isEmpty()) {
            return;
        }
        //1.删除provider中的PId
        Object provider = data.get("provider");
        if (provider instanceof ArrayList) {
            List<LinkedHashMap> providerList = (ArrayList<LinkedHashMap>) provider;
            List<LinkedHashMap> collect = providerList.stream().filter(map -> {
                Object pId = map.get("pId");
                boolean contains = pIdList.contains(pId.toString());
                return !contains;
            }).collect(Collectors.toList());
            data.put("provider", collect);
        } else {
            throw new RuntimeException("yaml文件缺少provider字段/provider字段格式不正确，请检查文件格式！");
        }
        //2.删除api中的PId
        Object api = data.get("api");
        if (api instanceof ArrayList) {
            List<LinkedHashMap> apiList = (ArrayList<LinkedHashMap>) api;
            List<LinkedHashMap> collect = apiList.stream().filter(map -> {
                LinkedHashMap apiProviderInfo = (LinkedHashMap) map.get("apiProviderInfo");
                Object pId = apiProviderInfo.get("pId");
                boolean contains = pIdList.contains(pId.toString());
                return !contains;
            }).collect(Collectors.toList());
            data.put("api", collect);
        } else {
            throw new RuntimeException("yaml文件缺少api字段/api字段格式不正确，请检查文件格式！");
        }
    }


    public static void main(String[] args) throws IOException {
        //字符串中是否包含中文
        String yamlFilePath = "C:\\Users\\sunjian23\\Desktop\\artemis_resource_apidoc_2.1.0.20230418213905\\综合安防产品iSecure Center_OpenAPI.yaml";
        String yamlOutputFilePath = "C:\\Users\\sunjian23\\Desktop\\artemis_resource_apidoc_2.1.0.20230418213905\\综合安防产品iSecure Center_OpenAPI_Output.yaml";
        List<String> pIdList = Arrays.asList("封闭区域人车管控应用服务","升降柱管理应用服务","行车监控应用服务","数据联网网关服务","消防网络管理服务","动环监控应用服务","食堂消费应用服务","消防管理应用服务");
        List<String> groupIdList = Arrays.asList("食堂消费接口","升降柱管理应用服务接口","停车场联网网关","动环监控接口","消防应用服务","封闭区域人车管控");
        removeUnnecessaryInterfaceInformation(yamlFilePath, yamlOutputFilePath, pIdList, groupIdList);
    }

}
