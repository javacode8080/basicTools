package com.example.exceltosql.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.example.exceltosql.Util.*;
import com.example.exceltosql.constant.CommonConstant;
import com.example.exceltosql.entity.JsonBean;
import com.example.exceltosql.entity.SqlBean;
import javafx.util.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipOutputStream;

import static com.example.exceltosql.Util.ZipUtil.unzip;

/**
 * @author :sunjian23
 * @date : 2022/10/19 9:45
 */
@Service
public class FileToSqlServiceImpl {
    //日志输出
    private static Logger logger = LoggerFactory.getLogger(FileToSqlServiceImpl.class);
    private final static Integer MAXCOLUMNNUMBER = 10000;


    //单文件上传
    public String fileUpload(MultipartHttpServletRequest request) throws IOException {
        //获取文件
        MultipartFile file = request.getFile("file");
        //获取上传到服务器中的文件的绝对路径
        String filePath = fileUpload(file);
        return filePath;
    }

    //单文件上传
    public String fileUpload(MultipartFile file) throws IOException {
        //获取上传的文件的文件名
        String fileName = file.getOriginalFilename();
        //获取上传的文件的后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        //将UUID作为文件名
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        //将uuid和后缀名拼接后的结果作为最终的文件名
        fileName = uuid + suffixName;
        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        //上传路径
        String uploadFilePath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "upload";
        File excelFile = new File(uploadFilePath);
        //判断保存文件所对应路径是否存在
        if (!excelFile.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            excelFile.mkdirs();
        }
        String finalPath = excelFile.getAbsolutePath() + File.separator + fileName;
        //上传文件
        file.transferTo(new File(finalPath));
        return finalPath;
    }

    //多文件上传本地
    public List<String> multipleFileUpload(MultipartHttpServletRequest request) throws IOException {
        //获取多文件map
        MultiValueMap<String, MultipartFile> multiFileMap = request.getMultiFileMap();
        //创建多文件list集合
        List<MultipartFile> fileList = new LinkedList<>();
        //遍历获取多文件
        for (Map.Entry<String, List<MultipartFile>> temp : multiFileMap.entrySet()) {
            fileList = temp.getValue();
        }
        //创建文件上传路径list集合
        List<String> uploadFilePathList = new LinkedList<>();
        //遍历多文件list上传到服务器，并记录文件上传路径
        for (MultipartFile temp : fileList) {
            String uploadFilePath = fileUpload(temp);
            uploadFilePathList.add(uploadFilePath);
        }
        return uploadFilePathList;
    }

    //多文件获取MultipartFile文件list
    public List<MultipartFile> getMultipleFileList(MultipartHttpServletRequest request) throws IOException {
        //获取多文件map
        MultiValueMap<String, MultipartFile> multiFileMap = request.getMultiFileMap();
        //创建多文件list集合
        List<MultipartFile> fileList = new LinkedList<>();
        //遍历获取多文件
        for (Map.Entry<String, List<MultipartFile>> temp : multiFileMap.entrySet()) {
            fileList = temp.getValue();
        }
        return fileList;
    }


    //并发限制
    public synchronized String sqlFileWrite(String filePath, String idType) throws IOException {
        List<String> sqlFilePathList = new ArrayList<>();
        Map<String, List<LinkedHashMap<String, Object>>> map = ExcelUtil.ReadExcelByRC(filePath, 1, MAXCOLUMNNUMBER, true);
        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            String sqlFilePath = ExcelUtil.writeToSqlFile(map.get(next), idType, next);
            sqlFilePathList.add(sqlFilePath);
        }
        //将创建的文件进行压缩
        if (sqlFilePathList.size() != 0) {
            // 压缩输出流,包装流,将临时文件输出流包装成压缩流,将所有文件输出到这里,打成zip包
            ZipOutputStream zipOut = null;
            String zipDirPath = File.separator + sqlFilePathList.get(0).substring(0, sqlFilePathList.get(0).lastIndexOf(File.separator));
            String zipFilePath = zipDirPath + File.separator + "sql_" + UUID.randomUUID().toString().replaceAll("-", "") + ".zip";
            try {
                zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            // 循环调用压缩文件方法,将一个一个需要下载的文件打入压缩文件包
            for (String path : sqlFilePathList) {
                if (null != path && !"".equals(path)) {
                    //将路径进行拆分，将上面拼接的真实文件名拆分出来作为参数传递进去
                    int lastIndexOf = path.lastIndexOf(File.separator) + 1;
                    String realFileName = path.substring(lastIndexOf, path.length());
                    try {
                        //调用工具类方法,传递路径和压缩流，压缩包文件的名字
                        ExcelUtil.fileToZip(path, zipOut, realFileName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
            try {
                // 压缩完成后,关闭压缩流
                zipOut.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //删除所有sql临时文件
            sqlFilePathList.forEach(pathURL -> {
                if (null != pathURL && !"".equals(pathURL)) {
                    Path path = Paths.get(pathURL);
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            return zipFilePath;
        }
        return null;
    }

    public void fileDownLoad(String zipPath, HttpServletResponse response) {
        ServletOutputStream outputStream = null;
        FileInputStream fileInputStream = null;
        try {
            if (null != zipPath) {
                //输入流，通过输入流读取文件
                fileInputStream = new FileInputStream(new File(zipPath));
                //输出流，通过输出流将文件写回浏览器，在浏览器展示图片
                outputStream = response.getOutputStream();
//            //设置文件类型:这种方式是实现直接在浏览器中显示
//            response.setContentType("image/jpg");
                response.setContentType("application/octet-stream");
                //设置文件类型：这种方式是实现以附件下载
                String encodedFileName = URLEncoder.encode(zipPath.substring(zipPath.lastIndexOf(File.separator) + 1), "UTF-8").replaceAll("\\+", "%20");
                response.setHeader("Content-disposition", "attachment;filename=" + encodedFileName);
                int len = 0;
                byte[] bytes = new byte[1024];
                while ((len = fileInputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, len);
                    outputStream.flush();
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                outputStream.close();
                fileInputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public synchronized String temp_newTableFile(String filePath) throws Exception {
        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String sqlDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "temp_newTableInsert";
        File sqlDir = new File(sqlDirPath);
        //判断保存文件所对应路径是否存在
        if (!sqlDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            sqlDir.mkdirs();
        }
        //上传路径
        String uploadFilePath = sqlDir + File.separator + "temp_newTableInsert_" + UUID.randomUUID().toString().replaceAll("-", "") + ".sql";
        SQLUtil.temp_newtableInsertSql(filePath, 2, MAXCOLUMNNUMBER, uploadFilePath, SqlBean.class);
        return uploadFilePath;
    }

    public synchronized String jsonFileWrite(String filePath) throws Exception {
        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String sqlDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "json";
        File sqlDir = new File(sqlDirPath);
        //判断保存文件所对应路径是否存在
        if (!sqlDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            sqlDir.mkdirs();
        }
        //上传路径
        String uploadFilePath = sqlDir + File.separator + "jsonFile_" + UUID.randomUUID().toString().replaceAll("-", "") + ".json";
        JSONUtil.toJSON(filePath, 2, MAXCOLUMNNUMBER, uploadFilePath, JsonBean.class);

        //JSONUtil.toJSONNotRemoveDuplicates(filePath, 2, MAXCOLUMNNUMBER, uploadFilePath, JsonBean.class);
        return uploadFilePath;
    }


    public synchronized String keyChangeAndFilter(String filePath) throws Exception {
        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String sqlDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "keyChangeAndFilter";
        File sqlDir = new File(sqlDirPath);
        //判断保存文件所对应路径是否存在
        if (!sqlDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            sqlDir.mkdirs();
        }
        //上传路径
        String uploadFilePath = sqlDir + File.separator + "keyChangeAndFilter_" + UUID.randomUUID().toString().replaceAll("-", "") + ".xlsx";
        ExcelUtil.keyChangeAndFilter(filePath, 2, MAXCOLUMNNUMBER, uploadFilePath, JsonBean.class);

        //        JSONUtil.toJSONNotRemoveDuplicates(filePath, 2, MAXCOLUMNNUMBER, uploadFilePath, JsonBean.class);
        return uploadFilePath;
    }

    public synchronized String translationMatch(String filePath) throws IOException, IllegalAccessException {
        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String sqlDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "translationMatch";
        File sqlDir = new File(sqlDirPath);
        //判断保存文件所对应路径是否存在
        if (!sqlDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            sqlDir.mkdirs();
        }
        //上传路径
        String uploadFilePath = sqlDir + File.separator + "translationMatch_" + UUID.randomUUID().toString().replaceAll("-", "") + ".xlsx";
        TranslationMatchUtil.translationMatch(filePath, 1, MAXCOLUMNNUMBER, uploadFilePath);
        return uploadFilePath;
    }

    /**
     * @param filePath:
     * @return String
     * @author sunjian23
     * @description TODO:将Properties文件内容写入到Excel中，写入的对应关系为Properties的文件名对应Excel的列名
     * @date 2023/2/11 14:54
     */
    public synchronized String propertiesWriteInExcel(String filePath) throws IOException, IllegalAccessException {
        Map<String, File> fileMap = ZipUtil.unzipAndGetFilePathByMap(filePath);
        Iterator<Map.Entry<String, File>> iterator = fileMap.entrySet().iterator();
        Map<String, List<LinkedHashMap<String, Object>>> excelMap = null;
        Map<String, Map<String, String>> propertiesMap = new HashMap<>();
        while (iterator.hasNext()) {
            Map.Entry<String, File> next = iterator.next();
            if (next.getKey().contains(".xls") || next.getKey().contains(".xlsx")) {
                //读取到excel文件的时候将Excel文件的内容读取为map
                excelMap = ExcelUtil.ReadExcelByRC(next.getValue().getAbsolutePath(), 1, MAXCOLUMNNUMBER, true);
            } else if (next.getKey().contains(".properties")) {
                Map<String, String> map = PropertiesUtil.propertiesToMap(next.getValue().getAbsolutePath());
                propertiesMap.put(next.getKey().substring(0, next.getKey().indexOf(".")), map);
            } else {
                throw new RuntimeException("压缩包中包含不支持的文件格式");
            }
        }
        if (propertiesMap.size() == 0) {
            throw new RuntimeException("压缩包中没有properties文件");
        }
        if (null == excelMap || excelMap.size() == 0) {
            throw new RuntimeException("Excel文件不存在或者内容为空，无法匹配");
        }
        if (excelMap.size() > 1) {
            throw new RuntimeException("Excel文件只允许存在一个sheet页");
        }

        //开始进行循环匹配
        String sheetName = (String) excelMap.keySet().toArray()[0];
        List<LinkedHashMap<String, Object>> excelList = excelMap.get(sheetName);
        //获取Excel的列名
        List<String> columnList = new ArrayList<String>();
        Iterator<Map.Entry<String, Object>> iterator1 = excelList.get(0).entrySet().iterator();
        while (iterator1.hasNext()) {
            Map.Entry<String, Object> next = iterator1.next();
            columnList.add(next.getKey());
        }
        //根据列名去找properties对应的内容进行匹配
        excelList.stream().forEach((map) -> {
            //1.获取当前的key
            String key = (String) map.get(columnList.get(0));
            //2.propertiesMap获取上述key的对应后面各列
            for (int i = 1; i < columnList.size(); i++) {
                Map<String, String> map1 = propertiesMap.get(columnList.get(i));
                if (null == map1) {
                    throw new RuntimeException("properties文件名与Excel列名之间未建立对应关系【原因可能是缺失某一种语言的properties文件】");
                }
                if (map1.size() == 0) {
                    // throw new RuntimeException("properties文件为空");
                    continue;
                }
                String value = map1.get(key);
                if (!StringUtils.isEmpty(value)) {
                    map.put(columnList.get(i), value);
                }
            }
        });

        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String sqlDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "propertiesWriteInExcel";
        File sqlDir = new File(sqlDirPath);
        //判断保存文件所对应路径是否存在
        if (!sqlDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            sqlDir.mkdirs();
        }
        //上传路径
        String uploadFilePath = sqlDir + File.separator + "propertiesWriteInExcel_" + UUID.randomUUID().toString().replaceAll("-", "") + ".xlsx";
        //写出文件
        ExcelUtil.writeExcel(uploadFilePath, excelMap, Arrays.asList(sheetName));
        return uploadFilePath;
    }


    public synchronized String xmlToExcel(String filePath, List<String> expandColumn) throws IOException, IllegalAccessException {
        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String sqlDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "xmlToExcel";
        File sqlDir = new File(sqlDirPath);
        //判断保存文件所对应路径是否存在
        if (!sqlDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            sqlDir.mkdirs();
        }
        //上传路径
        String uploadFilePath = sqlDir + File.separator + "xmlToExcel" + UUID.randomUUID().toString().replaceAll("-", "") + ".xlsx";
        //输出Excel文件
        XmlUtils.xmlToExcel(filePath, uploadFilePath, expandColumn);
        return uploadFilePath;
    }

    public synchronized String excelToXml(String filePath) throws IOException, IllegalAccessException, DocumentException {
        Map<String, File> fileMap = ZipUtil.unzipAndGetFilePathByMap(filePath);
        Iterator<Map.Entry<String, File>> iterator = fileMap.entrySet().iterator();
        List<LinkedHashMap<String, Object>> excelMap = null;
        String sourcePath = null;
        while (iterator.hasNext()) {
            Map.Entry<String, File> next = iterator.next();
            String uploadFilePath = next.getKey();
            //确定其是否为ts文件
            if (uploadFilePath.contains(".ts")) {
                sourcePath = next.getValue().getAbsolutePath();
            } else if (uploadFilePath.contains(".xls") || uploadFilePath.contains(".xlsx")) {
                //读取到excel文件的时候将Excel文件的内容读取为map
                Map<String, List<LinkedHashMap<String, Object>>> excelMaps = ExcelUtil.ReadExcelByRC(next.getValue().getAbsolutePath(), 1, MAXCOLUMNNUMBER, true);
                if (excelMaps.size() != 1) {
                    throw new IllegalStateException("Excel文件有误");
                }
                Iterator<String> iterator1 = excelMaps.keySet().iterator();
                while (iterator1.hasNext()) {
                    String next1 = iterator1.next();
                    excelMap = excelMaps.get(next1);
                }
            } else {
                throw new IllegalStateException("压缩包中含有其他类型文件");
            }

        }
        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String sqlDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "excelToTs";
        File sqlDir = new File(sqlDirPath);
        //判断保存文件所对应路径是否存在
        if (!sqlDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            sqlDir.mkdirs();
        }
        //上传路径
        String uploadFilePath = sqlDir + File.separator + "excelToTs" + UUID.randomUUID().toString().replaceAll("-", "") + ".ts";

        //输出文件
        XmlUtils.listWriteInXml(sourcePath, uploadFilePath, excelMap);

        return uploadFilePath;
    }

    public synchronized String removeBlankTags(String filePath) throws IOException, IllegalAccessException, DocumentException {
        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String sqlDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "removeBlankTags";
        File sqlDir = new File(sqlDirPath);
        //判断保存文件所对应路径是否存在
        if (!sqlDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            sqlDir.mkdirs();
        }
        //上传路径
        String uploadFilePath = sqlDir + File.separator + "removeBlankTags" + UUID.randomUUID().toString().replaceAll("-", "") + ".ts";
        //输出文件
        XmlUtils.removeBlankTags(filePath, uploadFilePath);

        return uploadFilePath;
    }

    public String generateLanguagePackByExcel(List<MultipartFile> fileList) throws IOException {

        if (fileList.size() > 2) {
            throw new IllegalArgumentException("文件数量不匹配");
        }
        String url = "http://localhost:8080/MutiLanguage/multiLanguagePackageByExcelData";
        CloseableHttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(url);
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.setMode(HttpMultipartMode.RFC6532);   // 处理中文文件名称乱码
        multipartEntityBuilder.setCharset(Charset.forName("UTF-8"));
        //添加MultiPartFile文件
        for (MultipartFile file : fileList) {
            if (file.getOriginalFilename().endsWith(CommonConstant.ZIP_SUFFIX)) {
                multipartEntityBuilder.addBinaryBody("file", file.getInputStream(), ContentType.MULTIPART_FORM_DATA, file.getOriginalFilename());
            } else if (file.getOriginalFilename().endsWith(CommonConstant.XLS) || file.getOriginalFilename().endsWith(CommonConstant.XLSX)) {
                multipartEntityBuilder.addBinaryBody("excelFile", file.getInputStream(), ContentType.MULTIPART_FORM_DATA, file.getOriginalFilename());
            } else {
                throw new IllegalArgumentException("文件类型不匹配");
            }
        }
        //json请求体
        String body = "{\n" +
                "    \"componentType\":\"component\",\n" +
                "    \"componentVersion\":\"2.0.1\",\n" +
                "    \"componentID\":\"tas\"\n" +
                "}";
        //添加json请求体@RequestPart,ContentType参数用于指定当前text内容的具体格式(ContentType必须指定，否则无法映射)
        multipartEntityBuilder.addTextBody("body", body, ContentType.APPLICATION_JSON);
        HttpEntity httpEntity = multipartEntityBuilder.build();
        //设置请求体
        httpPost.setEntity(httpEntity);
        //执行http请求
        CloseableHttpResponse response = client.execute(httpPost);
        String result = EntityUtils.toString(response.getEntity(), "utf-8");
        return result;
    }


    /**
     * @param filePath:文件上传路径
     * @param enumList:参数列表，用于文件中不包含packageinfo.xml文件时使用
     * @return String：返回最后压缩后的路径
     * @author sunjian23
     * @description TODO:生成资源包或者语言包的校验文件
     * @date 2023/6/28 13:32
     */
    public String generateMETAINFFile(String filePath, List<Pair<String, String>> enumList) throws Exception {
        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String zipDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "METAINFFile";
        File zipDir = new File(zipDirPath);
        //判断保存文件所对应路径是否存在
        if (!zipDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            zipDir.mkdirs();
        }
        //0.解压文件到指定目录
        String basePath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "unzip" + File.separator + "METAINF" + UUID.randomUUID().toString();
        File basePathDir = new File(basePath);
        if (!basePathDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            basePathDir.mkdirs();
        }
        unzip(filePath, basePath + File.separator);
        //1.校验/生成packageinfo.xml文件
        String zipFileNamePrefix = METAINFUtil.getPackageInfoFile(basePath, enumList);
        //2.生成file_checksum.xml文件
        String buildTime = METAINFUtil.generateFileChecksum(basePath + File.separator + "META-INF", true);
        //3.压缩包名称
        String zipPath = zipDirPath + File.separator + zipFileNamePrefix + CommonConstant.DOT + buildTime + CommonConstant.ZIP_SUFFIX;
        ZipUtil.zipFolderContents(basePath, zipPath);
        return zipPath;
    }


    public String fileCompare(List<MultipartFile> filePaths) throws Exception {
        //0.检验文件格式
        if (filePaths.size() != 2) {
            throw new IllegalArgumentException("文件数量不匹配");
        }
        for (MultipartFile filePath : filePaths) {
            String name = filePath.getOriginalFilename();
            if (!name.equals("master.war") && !name.equals("master.zip") && !name.equals("slave.war") && !name.equals("slave.zip")) {
                throw new RuntimeException("文件名称/类型不符合要求");
            }
        }
        if (filePaths.get(0).getOriginalFilename().equals(filePaths.get(1).getOriginalFilename())) {
            throw new RuntimeException("两文件名称相同");
        }
        //1.业务逻辑
        String userDir = System.getProperties().getProperty("user.dir");

        String masterFileRootPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "unzip" + File.separator + "FileCompare-master";
        String masterDirName = UUID.randomUUID().toString();

        String slaveFileRootPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "unzip" + File.separator + "FileCompare-slave";
        String slaveDirName = UUID.randomUUID().toString();
        //当前用户程序所在的目录
        String zipDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "FileCompare";
        String resultFileName = "compareResult-" + UUID.randomUUID().toString() + ".json";
        File zipDir = new File(zipDirPath);
        //判断保存文件所对应路径是否存在
        if (!zipDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            zipDir.mkdirs();
        }
        //解压文件到指定目录
        for (MultipartFile file : filePaths) {
            String name = file.getOriginalFilename();
            if (name.equals("master.war") || name.equals("master.zip")) {
                String filePath = fileUpload(file);
                FileCompareUtil.unzip(filePath, masterFileRootPath + File.separator + masterDirName);
            } else if (name.equals("slave.war") || name.equals("slave.zip")) {
                String filePath = fileUpload(file);
                FileCompareUtil.unzip(filePath, slaveFileRootPath + File.separator + slaveDirName);
            } else {
                throw new RuntimeException("文件名称/类型不符合要求");
            }
        }
        //获取两个文件夹的内部文件的md5码
        Map<String, String> masterFileMap = FileCompareUtil.getFilesMap(masterFileRootPath, masterDirName);
        Map<String, String> slaveFileMap = FileCompareUtil.getFilesMap(slaveFileRootPath, slaveDirName);

        //获取对比结果
        Map<String, List<String>> listMap = FileCompareUtil.compareFiles(masterFileMap, slaveFileMap);

        //输出文件
        File result = new File(zipDirPath + File.separator + resultFileName);
        if (!result.exists()) {
            result.createNewFile();
        }
        String s = JSON.toJSONString(listMap, SerializerFeature.PrettyFormat);
        FileOutputStream outputStream = null;
        outputStream = new FileOutputStream(result.getAbsoluteFile(), false);//注意这里的第二个参数true是指写入的时候不覆盖原有的内容
        outputStream.write(s.getBytes());
        outputStream.close();


        return zipDirPath + File.separator + resultFileName;
    }

    public String yamlExcelInterchange(List<MultipartFile> fileList) throws IOException {
        //字符串中是否包含中文
        String regex = "[\\u4E00-\\u9FA5]";
        //0.检验文件格式
        if (fileList.size() == 1) {
            String name = fileList.get(0).getOriginalFilename();
            if (!Objects.requireNonNull(name).contains(".yaml")) {
                throw new RuntimeException("文件类型不符合要求");
            }
            //1.业务逻辑
            String filePath = fileUpload(fileList.get(0));
            String userDir = System.getProperties().getProperty("user.dir");
            String zipDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "YamlToExcel";
            String resultFileName = "TranslateFile-" + UUID.randomUUID().toString() + ".xlsx";
            File zipDir = new File(zipDirPath);
            //判断保存文件所对应路径是否存在
            if (!zipDir.exists()) {
                //若不存在，则创建多级目录【用mkdirs()】
                zipDir.mkdirs();
            }
            YamlToExcelUtil.convertYamlToExcel(filePath, zipDirPath + File.separator + resultFileName, regex);
            return zipDirPath + File.separator + resultFileName;
        } else if (fileList.size() == 2) {
            String yamlFilePath = "";
            String excelFilePath = "";
            int flag = 0;
            for (MultipartFile filePath : fileList) {
                String name = filePath.getOriginalFilename();
                if (Objects.requireNonNull(name).contains(".yaml")) {
                    flag++;
                    yamlFilePath = fileUpload(filePath);
                } else if (Objects.requireNonNull(name).contains(".xlsx") || Objects.requireNonNull(name).contains(".xls")) {
                    flag++;
                    excelFilePath = fileUpload(filePath);
                }
            }
            if (flag != 2) {
                throw new RuntimeException("文件类型不符合要求");
            }
            //1.业务逻辑
            String resultFilePath = ExcelToYamlUtil.yamlTranslationMatch(yamlFilePath, excelFilePath, regex);
            return resultFilePath;
        } else {
            throw new IllegalArgumentException("文件数量不匹配");
        }

    }


    public String artemisRemoveInterfaceByserviceNameORgroupName(String filePath, List<String> serviceNameList, List<String> groupNameList) throws IOException {
        return ArtemisUtil.removeUnnecessaryInterfaceInformation(filePath, serviceNameList, groupNameList);
    }

    public String makeDigitalSignature(String filePath, String originalFilename) {

        return DigitalSignatureUtil.fileProcessing(filePath, originalFilename);
    }
}
