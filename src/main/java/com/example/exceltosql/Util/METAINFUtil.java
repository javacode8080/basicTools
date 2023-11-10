package com.example.exceltosql.Util;

import com.example.exceltosql.constant.CommonConstant;
import com.example.exceltosql.enums.PackageInfoEnum;
import javafx.util.Pair;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author :sunjian23
 * @date : 2023/6/27 16:15
 */
public class METAINFUtil {

    private static final String META_INF = "META-INF" + File.separator;

    public static final String UTC_BEIJING_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS+'08:00'";

    public static final String DATETIME_FORMAT = "yyyyMMddHHmmss";

    private static final String FILE_CHECKSUM_FILE = "file_checksum.xml";

    private static final String PACKAGE_INFO_FILE = "packageinfo.xml";


    //根据packageinfo.xml中的package标签获取文件名前缀，如果不存在packageinfo.xml，则根据模板和传递的参数
    public static String getPackageInfoFile(String filePath, List<Pair<String, String>> packageInfoEnumList) throws Exception {
        File fileDir = new File(filePath + File.separator + META_INF);
        //判断当前解压文件是否存在META-INF文件夹
        if (!fileDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            fileDir.mkdirs();
            //生成packageinfo.xml文件
            return generateFilePackageInfo(fileDir.getAbsolutePath() + File.separator + PACKAGE_INFO_FILE, packageInfoEnumList);
        } else {
            //存在文件夹的话，判断是否存在packageinfo.xml文件
            File file = new File(fileDir.getAbsolutePath() + File.separator + PACKAGE_INFO_FILE);
            if (file.exists()) {
                //文件存在直接读取文件内容拼接成压缩文件名称前缀
                //2.读取文件内容
                SAXReader saxReader = new SAXReader();
                Document document = saxReader.read(file);
                Element root = document.getRootElement();
                Element element = root.element("package");
                assert element != null;
                String id = element.attributeValue("id");
                String version = element.attributeValue("version");
                if(StringUtils.isEmpty(id) || StringUtils.isEmpty(version)){
                    throw new RuntimeException("packageinfo.xml文件的<package>标签缺失重要属性值：id,version,type；请检查该文件！");
                }
                String packageNamePrefix = id + CommonConstant.UNDER_LINE + version;
                return packageNamePrefix;
            } else {
                //生成packageinfo.xml文件
                return generateFilePackageInfo(file.getAbsolutePath(), packageInfoEnumList);
            }
        }

    }

    /**
     * @param filePath: 具体的文件实际路径，精确到文件名
     * @param packageInfoEnumList:
     * @return String
     * @author sunjian23
     * @description TODO
     * @date 2023/6/29 16:54
     */
    private static String generateFilePackageInfo(String filePath, List<Pair<String, String>> packageInfoEnumList) throws Exception {
        //0.检验参数传递是否正确
        List<Pair<String, String>> infoEnums = new ArrayList<>();
        packageInfoEnumList.forEach(packageInfoEnum -> {
            if (StringUtils.isEmpty(packageInfoEnum.getValue())) {
                if (PackageInfoEnum.ID.getAttribute().equals(packageInfoEnum.getKey()) || PackageInfoEnum.TYPE.getAttribute().equals(packageInfoEnum.getKey()) || PackageInfoEnum.VERSION.getAttribute().equals(packageInfoEnum.getKey()) || PackageInfoEnum.DISPLAYNAME_ZH.getAttribute().equals(packageInfoEnum.getKey()) || PackageInfoEnum.COMPONENTID.getAttribute().equals(packageInfoEnum.getKey())) {
                    throw new RuntimeException("在缺失packageinfo.xml文件时，包类型、id、版本、中文描述、组件标识为必传字段！");
                }
            }
            if (PackageInfoEnum.MAJORVERSION.getAttribute().equals(packageInfoEnum.getKey()) && StringUtils.isEmpty(packageInfoEnum.getValue())) {
                infoEnums.add(packageInfoEnum);
            }
            if (PackageInfoEnum.MINORVERSION.getAttribute().equals(packageInfoEnum.getKey()) && StringUtils.isEmpty(packageInfoEnum.getValue())) {
                infoEnums.add(packageInfoEnum);
            }
        });
        if (infoEnums.size() > 1) {
            throw new RuntimeException("最低适配版本/最高适配版本 至少传递一个！");
        }
        //1.获取参考模板
        String userDir = System.getProperties().getProperty("user.dir");
        String packageInfoPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "reference" + File.separator + "META-INF" + File.separator + PACKAGE_INFO_FILE;
        //2.读取模板
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(packageInfoPath));
        //3.获取根节点元素对象
        Element root = document.getRootElement();
        StringBuilder sb = new StringBuilder();
        packageInfoEnumList.forEach(packageInfoEnum -> {
            //如果type是resourcePack则删除languages
            if (PackageInfoEnum.TYPE.getAttribute().equals(packageInfoEnum.getKey())) {
                if ("resourcePack".equals(packageInfoEnum.getValue())) {
                    Element params = root.element("params");
                    if(null != params){
                        removeTagsByAttributeValue(params, "param", PackageInfoEnum.ID.getAttribute(), "languages");
                    }
                    root.element("package").setAttributeValue(PackageInfoEnum.TYPE.getAttribute(), packageInfoEnum.getValue());
                } else {
                    root.element("package").setAttributeValue(PackageInfoEnum.TYPE.getAttribute(), packageInfoEnum.getValue());
                }
            } else if (PackageInfoEnum.ID.getAttribute().equals(packageInfoEnum.getKey())) {
                sb.append(packageInfoEnum.getValue()).append(CommonConstant.UNDER_LINE);
                root.element("package").setAttributeValue(PackageInfoEnum.ID.getAttribute(), packageInfoEnum.getValue());
            } else if (PackageInfoEnum.VERSION.getAttribute().equals(packageInfoEnum.getKey())) {
                sb.append(packageInfoEnum.getValue());
                root.element("package").setAttributeValue(PackageInfoEnum.VERSION.getAttribute(), packageInfoEnum.getValue());
            } else if (PackageInfoEnum.MINORVERSION.getAttribute().equals(packageInfoEnum.getKey()) && StringUtils.isEmpty(packageInfoEnum.getValue())) {
                Element params = root.element("params");
                if(null != params){
                    removeTagsByAttributeValue(params, "param", PackageInfoEnum.ID.getAttribute(), PackageInfoEnum.MINORVERSION.getAttribute());
                    removeTagsByAttributeValue(params, "param", PackageInfoEnum.ID.getAttribute(), PackageInfoEnum.MINORACTION.getAttribute());
                }

            } else if (PackageInfoEnum.MAJORVERSION.getAttribute().equals(packageInfoEnum.getKey()) && StringUtils.isEmpty(packageInfoEnum.getValue())) {
                Element params = root.element("params");
                if(null != params){
                    removeTagsByAttributeValue(params, "param", PackageInfoEnum.ID.getAttribute(), PackageInfoEnum.MAJORVERSION.getAttribute());
                    removeTagsByAttributeValue(params, "param", PackageInfoEnum.ID.getAttribute(), PackageInfoEnum.MAJORACTION.getAttribute());
                }
            } else {
                String xpathExpression = "//*[local-name()='param' and @id='" + packageInfoEnum.getKey() + "']";
                Element element = getElementByXPath(document, xpathExpression);
                if (element != null) {
                    element.setAttributeValue("value", packageInfoEnum.getValue());
                }
            }
        });
        generateXmlFile(filePath,document);
        return sb.toString();
    }


    /**
     * @param filePath:META-INF文件夹路径
     * @param isAddFileChecksum:     是否在file_checksum.xml中加入自己文件的校验md5和size
     * @return String:返回时间戳
     * @author sunjian23
     * @description TODO:生成file_checksum.xml文件
     * @date 2023/6/28 10:16
     */
    public static String generateFileChecksum(String filePath, boolean isAddFileChecksum) throws Exception {
        //1.获取参考模板
        String userDir = System.getProperties().getProperty("user.dir");
        String fileCheckSumPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "reference" + File.separator + "META-INF" + File.separator + FILE_CHECKSUM_FILE;
        //2.读取模板
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(new File(fileCheckSumPath));
        //3.获取根节点元素对象
        Element root = document.getRootElement();
        //4.获取传入的文件(夹)的文件的MD5的list【除file_checksum.xml文件外的所有文件】
        List<Map<String, String>> md5MapList = new ArrayList<Map<String, String>>();
        MD5Util.generateFilesMD5Value(filePath, META_INF, md5MapList);
        //5.根据md5MapList向file_checksum.xml中写入文件内容
        //5.1获得当前时间
        LocalDateTime dateTime = LocalDateTime.now();
        //5.2获取时间格式1写入file_checksum.xml
        String packageTime = dateTime.format(DateTimeFormatter.ofPattern(UTC_BEIJING_TIME_FORMAT));
        //5.3获取时间格式2作为文件夹名称
        String buildTime = dateTime.format(DateTimeFormatter.ofPattern(DATETIME_FORMAT));
        //5.5写入文件标签
        if (!CollectionUtils.isEmpty(md5MapList)) {
            for (Map<String, String> md5Map : md5MapList) {
                Element fileEls = root.addElement("file");
                fileEls.addAttribute("path", md5Map.get("filePath"));
                fileEls.addAttribute("size", md5Map.get("fileSize"));
                fileEls.addAttribute("md5", md5Map.get("md5"));
            }
        }
        //5.6向packageTime标签中写入时间
        Element packageTimeEls = root.addElement("packageTime");
        packageTimeEls.addText(packageTime);
        //5.7判断是否添加自身文件MD5
        if (isAddFileChecksum) {
            addFileChecksumSizeAndMD5(filePath, document);
        } else {
            removeTagsByAttributeValue(root, "file", "path", "META-INF/file_checksum.xml");
        }
        //6输出正式文件
        generateXmlFile(filePath + File.separator + FILE_CHECKSUM_FILE, document);
        return buildTime;
    }


    /**
     * @param filePath: 输出文件路径
     * @param document: xml的文档对象
     * @return void
     * @author sunjian23
     * @description TODO
     * @date 2023/6/28 11:12
     */
    private static void generateXmlFile(String filePath, Document document) throws Exception {
        //1.指定文件输出的位置
        FileOutputStream featuresOut = new FileOutputStream(filePath);
        //2.漂亮格式：有空格换行
        OutputFormat featuresFormat = OutputFormat.createPrettyPrint();
        //3.指定文本的写出的格式
        featuresFormat.setEncoding("UTF-8");
        //4.创建写出对象
        XMLWriter featuresWriter = new XMLWriter(featuresOut, featuresFormat);
        //5.写出Document对象
        featuresWriter.write(document);
        //6.关闭流
        featuresOut.close();
        featuresWriter.close();
    }

    /**
     * @param filePath:当前所在文件夹路径
     * @param document:
     * @return void
     * @author sunjian23
     * @description TODO:添加自身文件的大小和MD5
     * @date 2023/6/28 11:08
     */
    private static void addFileChecksumSizeAndMD5(String filePath, Document document) throws Exception {
        //0.获取最终文件的路径
        String finalPath = filePath + File.separator + FILE_CHECKSUM_FILE;
        //1.输出第一份文件
        generateXmlFile(finalPath, document);
        //2.获取文件的大小和MD5码
        File singleFile = new File(finalPath);
        String fileSize = singleFile.length() + "";
        String md5 = MD5Util.getMD5(singleFile);
        //3.写入到document的指定path中
        String xpathExpression = "//*[local-name()='file' and @path='META-INF/file_checksum.xml']";//Xpath表达式，获取file标签下path属性值为META-INF/file_checksum.xml的element
        Element element = getElementByXPath(document, xpathExpression);
        if (null == element) {
            return;
        }
        element.setAttributeValue("size", fileSize);
        element.addAttribute("md5", md5);
        //2.输出第二次文件重新执行上面的过程
        generateXmlFile(finalPath, document);
        singleFile = new File(finalPath);
        fileSize = singleFile.length() + "";
        md5 = MD5Util.getMD5(singleFile);
        element = getElementByXPath(document, xpathExpression);
        element.setAttributeValue("size", fileSize);
        element.addAttribute("md5", md5);
        //最终的document更新完成
    }


    /**
     * @param element:element必须指定为想要删除节点的父节点，这样迭代器才有效
     * @param tagName:
     * @param attributeValueToRemove:
     * @return void
     * @author sunjian23
     * @description TODO：根据指定的标签名、标签属性、属性内容移除标签
     * @date 2023/6/28 10:54
     */

    public static void removeTagsByAttributeValue(Element element, String tagName, String attribute, String attributeValueToRemove) {
        Iterator<Element> iterator = element.elementIterator();
        while (iterator.hasNext()) {
            Element child = iterator.next();
            if (child.getName().equals(tagName) && child.attributeValue(attribute) != null && child.attributeValue(attribute).equals(attributeValueToRemove)) {
                iterator.remove();
            }
        }
    }

    /**
     * @param document:
     * @param xpathExpression:
     * @return Element
     * @author sunjian23
     * @description TODO:根据XPath表达式获取element
     * @date 2023/6/28 11:25
     */
    public static Element getElementByXPath(Document document, String xpathExpression) {
        XPath xpath = DocumentHelper.createXPath(xpathExpression);
        Node node = xpath.selectSingleNode(document);
        if (node != null && node instanceof Element) {
            return (Element) node;
        }
        return null;
    }
}

