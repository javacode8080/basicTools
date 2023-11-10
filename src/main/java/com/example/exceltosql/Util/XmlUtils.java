package com.example.exceltosql.Util;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class XmlUtils {

    public static Map<String, String> transMap = new HashMap<>();

    // key前缀
    public static String prefix = "issc.dicts.common.";

    // key后缀 e.g .name
    public static String suffix = "";


    //标签名称
    public static String TS = "TS";
    public static String CONTEXT = "context";
    public static String NAME = "name";
    public static String MESSAGE = "message";
    public static String SOURCE = "source";
    public static String TRANSLATION = "translation";
    public static String EN_TRANSLATION = "en_translation";

    public static String xmlPath = "C:\\Users\\sunjian23\\Desktop\\客户端ts\\isecureClient2.1.1_unpass_publish\\Portal_zh.ts";
    public static String toPath = "C:\\Users\\sunjian23\\Desktop\\客户端ts\\isecureClient2.1.1_unpass_publish\\Portal_cn.ts";

    //追加内容至文件最后，从起一行开始
    public static String languageFilePath = "C:\\Users\\sunjian23\\Desktop\\客户端ts\\isecureClient2.1.1_unpass_publish\\11111111111.properties";

    public static final String REGEX_CHINESE = "[\u4E00-\u9FA5|\\！|\\，|\\。|\\（|\\）|\\《|\\》|\\“|\\”|\\？|\\：|\\；|\\【|\\】|\\、|\\’|\\‘]";


    public static void getChildNodes(Element element, List<Attribute> chineseNodeList) {
        Iterator<Node> item = element.nodeIterator();
        while (item.hasNext()) {
            Node node = item.next();
            if (node instanceof Element) {
                Element el = (Element) node;
                int count = el.attributeCount();
                if (count != 0) {
                    Iterator<Attribute> elementIterator = el.attributeIterator();
                    while (elementIterator.hasNext()) {
                        Attribute next = elementIterator.next();
                        if (StringUtils.isNotEmpty(next.getValue()) && containChiese(next.getValue())) {
                            chineseNodeList.add(next);
                        }
                    }
//                    String nameValue = el.attributeValue("name");
                }
                getChildNodes(el, chineseNodeList);
            }
        }
    }

    public static List<String> idList = new ArrayList<>();

    public static String getKey() {
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 15);
        if (idList.contains(uuid)) {
            return getKey();
        } else {
            idList.add(uuid);
            return prefix + uuid + suffix;
        }
    }

    public static void write(String path, String content) throws IOException {
        //如果文件不存在，先创建文件
        File file = new File(path);
        if (!file.exists()) {
            file.createNewFile();
        }
        //获取随机读写文件的实例
        RandomAccessFile aFile = new RandomAccessFile(file, "rw");
        //获取连接到文件的通道
        FileChannel inChannel = aFile.getChannel();
        //将文件定位到结尾
        inChannel.position(inChannel.size());
        //在当前定位的位子上写入内容
        //将内容存入ByteBuffer
        ByteBuffer sendBuffer = ByteBuffer.wrap(content.getBytes("GBK"));
        //将ByteBuffer放入文件通道
        inChannel.write(sendBuffer);

        //关闭资源
        sendBuffer.clear();
        inChannel.close();
        aFile.close();
    }

    public static boolean containChiese(String str) {
        Pattern p = Pattern.compile(REGEX_CHINESE);
        Matcher m = p.matcher(str);
        return m.find();
    }


    /**
     * 将xml转换为JSON对象
     *
     * @param xml xml字符串
     * @return
     * @throws Exception
     */
    public static JSONObject xmltoJson(String xml) throws Exception {
        JSONObject jsonObject = new JSONObject();
        Document document = DocumentHelper.parseText(xml);
        //获取根节点元素对象
        Element root = document.getRootElement();
        iterateNodes(root, jsonObject);
        return jsonObject;
    }

    /**
     * 将xml转换为JSON对象
     *
     * @param file xml文件
     * @return
     * @throws Exception
     */
    public static JSONObject xmltoJson(File file) throws Exception {
        JSONObject jsonObject = new JSONObject();
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(file);
        //获取根节点元素对象
        Element root = document.getRootElement();
        iterateNodes(root, jsonObject);
        return jsonObject;
    }


    /**
     * 遍历元素
     *
     * @param node 元素
     * @param json 将元素遍历完成之后放的JSON对象
     */
    @SuppressWarnings("unchecked")
    public static void iterateNodes(Element node, JSONObject json) {
        //获取当前元素的名称
        String nodeName = node.getName();
        //判断已遍历的JSON中是否已经有了该元素的名称
        if (json.containsKey(nodeName)) {
            //该元素在同级下有多个
            Object Object = json.get(nodeName);
            JSONArray array = null;
            if (Object instanceof JSONArray) {
                array = (JSONArray) Object;
            } else {
                array = new JSONArray();
                array.add(Object);
            }
            //获取该元素下所有子元素
            List<Element> listElement = node.elements();
            if (listElement.isEmpty()) {
                //该元素无子元素，获取元素的值
                String nodeValue = node.getText();
                array.add(nodeValue);
                json.put(nodeName, array);
                return;
            }
            //有子元素
            JSONObject newJson = new JSONObject();
            //遍历所有子元素
            for (Element e : listElement) {
                //递归
                iterateNodes(e, newJson);
            }
            array.add(newJson);
            json.put(nodeName, array);
            return;
        }
        //该元素同级下第一次遍历
        //获取该元素下所有子元素
        List<Element> listElement = node.elements();
        if (listElement.isEmpty()) {
            //该元素无子元素，获取元素的值
            String nodeValue = node.getText();
            json.put(nodeName, nodeValue);
            return;
        }
        //有子节点，新建一个JSONObject来存储该节点下子节点的值
        JSONObject object = new JSONObject();
        //遍历所有一级子节点
        for (Element e : listElement) {
            //递归
            iterateNodes(e, object);
        }
        json.put(nodeName, object);
        return;
    }


    public static List<Map<String, String>> jsonObjectToList(JSONObject json) {
        List<Map<String, String>> tsEntities = new ArrayList<>();
        JSONArray contextArray = ((JSONObject) json.get(TS)).getJSONArray(CONTEXT);
        contextArray.stream().forEach(o -> {
            JSONObject jsonObject = (JSONObject) o;
            String name = (String) jsonObject.get(NAME);
            Object obj = jsonObject.get(MESSAGE);
            if (obj instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) obj;
                jsonArray.stream().forEach(o1 -> {
                    Map<String, String> map = new LinkedHashMap<>();
                    JSONObject jsonObject1 = (JSONObject) o1;
                    String source = (String) jsonObject1.get(SOURCE);
                    String translation = (String) jsonObject1.get(TRANSLATION);
                    map.put(NAME, name);
                    map.put(SOURCE, source);
                    map.put(TRANSLATION, translation);
                    tsEntities.add(map);
                });
            } else {
                Map<String, String> map = new LinkedHashMap<>();
                JSONObject jsonObject1 = (JSONObject) obj;
                String source = (String) jsonObject1.get(SOURCE);
                String translation = (String) jsonObject1.get(TRANSLATION);
                map.put(NAME, name);
                map.put(SOURCE, source);
                map.put(TRANSLATION, translation);
                tsEntities.add(map);
            }
        });
        return tsEntities;
    }


    public static void listWriteInXml(String sourcePath, String toPath, List<LinkedHashMap<String, Object>> list) throws DocumentException {
        SAXReader reader = new SAXReader();
        XMLWriter xmlWriter = null;
        try {
            File file = new File(sourcePath);
            if (!file.exists()) {
                throw new IllegalStateException("ts文件不存在，无法替换英文");
            }
            Document document = reader.read(new File(sourcePath));
            Element rootElement = document.getRootElement();
            Iterator contextElement = rootElement.elementIterator();
            while (contextElement.hasNext()) {
                Element contextNext = (Element) contextElement.next();
                Iterator nameElement = contextNext.elementIterator();
                //1.先获取到包含当前name，注意，1个context对应1个name，所以可以直接不用迭代器
                Element element1 = contextNext.element(NAME);
                //获取name标签中的内容
                String name = element1.getText();
                //先将具有这个name的list过滤出来
                List<Map<String, Object>> filterList = list.stream().filter(o -> o.get(NAME).equals(name)).collect(Collectors.toList());
                while (nameElement.hasNext()) {
                    Element element = (Element) nameElement.next();
                    String elementName = element.getName();
                    if (!elementName.equals(NAME)) {
                        Element source = element.element(SOURCE);
                        String key = source.getText();
                        //再过滤一下source
                        List<Map<String, Object>> filterList2 = filterList.stream().filter(o -> o.get(SOURCE).equals(key)).collect(Collectors.toList());
                        if (filterList2.size() > 1) {
                            throw new IllegalStateException("同一个name标签下不应该有相同内容的Resource标签");
                        }
                        Map<String, Object> filterMap = filterList2.get(0);
                        Element translation = element.element(TRANSLATION);
                        if (translation != null) {
                            if (!"".equals(translation.getText())) {
                                if (!translation.getText().equals(filterMap.get(TRANSLATION))) {
                                    throw new IllegalStateException("当前文件的中文和Excel中的对应key的中文不一致");
                                }
                                //将中文替换成英文
                                String en_translation = (String) filterMap.get(EN_TRANSLATION);
                                if (null == en_translation) {
                                    en_translation = "ERROR:  [" + translation.getText() + "]  not translation ! please attention";
                                }
                                translation.setText(en_translation);
                            } else {
                                //对于translation是空字符串的时候，我们统一将替换的设置为空字符串
                                translation.setText("");
                            }
                        }
                    }
                }
            }
            OutputFormat outputFormat = OutputFormat.createPrettyPrint();
            outputFormat.setEncoding("utf-8");
            File toFile = new File(toPath);
            if (!toFile.exists()) {
                toFile.getParentFile().mkdirs();
                toFile.createNewFile();
            }
            xmlWriter = new XMLWriter(new FileOutputStream(toPath), outputFormat);
            //将dom对象写入文件
            xmlWriter.write(document);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage());
        }finally {
            if(xmlWriter != null){
                try {
                    xmlWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public static void xmlToExcel(String filePath, String toFilePath, List<String> expandColumn) {
        JSONObject jsonObject = null;
        try {
            //1.将xml转化为json
            jsonObject = xmltoJson(new File(filePath));
            //2.根据json生成一个支持输出成Excel的list
            List<Map<String, String>> tsEntities = jsonObjectToList(jsonObject);
            //3.输出Excel
            ExcelUtil.writeExcel(toFilePath, tsEntities, false, expandColumn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void removeBlankTags(String sourcePath, String toPath) {
        SAXReader reader = new SAXReader();
        XMLWriter xmlWriter = null;
        try {
            File file = new File(sourcePath);
            if (!file.exists()) {
                throw new IllegalStateException("ts文件不存在");
            }
            Document document = reader.read(new File(sourcePath));
            //1.获取根节点
            Element rootElement = document.getRootElement();
            //2.获取根节点迭代器<迭代context>
            Iterator contextElement = rootElement.elementIterator();
            while (contextElement.hasNext()) {
                //3.获取context的内级标签
                Element contextNext = (Element) contextElement.next();
                //4.获取context的内级标签的迭代器
                Iterator nameAndMessageElement = contextNext.elementIterator();
                //5.获取name标签中的内容
                while (nameAndMessageElement.hasNext()) {
                    //6.重点：获取当前的message标签（作为移除的最小单元）
                    Element messageElement = (Element) nameAndMessageElement.next();
                    String messageElementName = messageElement.getName();
                    if (messageElementName.equals(MESSAGE)) {
                        //7.获取translation标签
                        Element translation = messageElement.element(TRANSLATION);
                        //8.获取translation标签内容
                        String value = translation.getText();
                        //9.当translation的标签为空时，术语value缺失的标签，移除这个translation所在的message标签
                        if(StringUtils.isEmpty(value)){
                            contextNext.remove(messageElement);
                        }
                    }
                }
            }
            //10.输出文件（createPrettyPrint方法输出较为良好的格式化xml文件，但是问题是如果标签内的文本存在换行符/n会将文本的换行符消除）
            OutputFormat outputFormat = OutputFormat.createPrettyPrint();
            outputFormat.setEncoding("utf-8");
            File toFile = new File(toPath);
            if (!toFile.exists()) {
                toFile.getParentFile().mkdirs();
                toFile.createNewFile();
            }
            xmlWriter = new XMLWriter(new FileOutputStream(toPath), outputFormat);
            //将dom对象写入文件
            xmlWriter.write(document);
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException(e.getMessage());
        }finally {
            if(xmlWriter != null){
                try {
                    xmlWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    //test
    public static void main(String[] args) throws DocumentException, IOException {
        JSONObject jsonObject = null;
        try {
            jsonObject = xmltoJson(new File(xmlPath));
            List<Map<String, String>> tsEntities = jsonObjectToList(jsonObject);
//            listWriteInXml(xmlPath,toPath,tsEntities);
            System.out.println(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        SAXReader saxReader = new SAXReader();
//        Document document = saxReader.read(new File(xmlPath));
//        OutputFormat format = OutputFormat.createPrettyPrint();
//        XMLWriter xmlWriter = new XMLWriter(new FileWriter(xmlPath), format);
//
//        List<Attribute> chineseNodeList = new ArrayList<>();
//        Element root = document.getRootElement();
//        List<Element> list = root.elements();
//        //遍历list取值
//        for (Element element : list) {
//            //使用递归函数
//            getChildNodes(element, chineseNodeList);
//        }
//
//        StringBuilder sb = new StringBuilder();
//
//        for (Attribute node : chineseNodeList) {
//            String value = node.getValue();
//            String key;
//            if (transMap.containsKey(value)) {
//                key = transMap.get(value);
//            } else {
//                key = getKey();
//                sb.append("\n").append(key).append("=").append(node.getStringValue());
//                transMap.put(value, key);
//            }
//            node.setValue(key);
//        }
//
//        write(languageFilePath, sb.toString());
//        xmlWriter.write(document);
//        xmlWriter.close();

    }

}
