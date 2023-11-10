package com.example.exceltosql.controller;

import com.example.exceltosql.dto.HttpResponseDto;
import com.example.exceltosql.enums.BusinessTypeEnum;
import com.example.exceltosql.enums.PackageInfoEnum;
import com.example.exceltosql.service.Impl.FileToSqlServiceImpl;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


@Controller
public class FileUpAndDownController {
    //日志输出
    private static Logger logger = LoggerFactory.getLogger(FileUpAndDownController.class);
    @Autowired
    private FileToSqlServiceImpl fileToSqlService;


    @RequestMapping("/upload")
    @ResponseBody
    public HttpResponseDto upload(MultipartHttpServletRequest request, HttpServletResponse response) {
        HttpResponseDto<String> result = HttpResponseDto.success(null);
        String zipPath = null;
        try {
            Map<String, String[]> parameterMap = request.getParameterMap();
            //获取业务功能
            String businessType = parameterMap.get("businessType")[0];
            if (BusinessTypeEnum.EXCELTODATABASECREATEANDDATAINSERT.getBusinessType().equals(businessType)) {
                //上传文件
                String filePath = fileToSqlService.fileUpload(request);
                //获取id策略
                String idType = parameterMap.get("idType")[0];
                //获取所有写成sql的文件路径
                zipPath = fileToSqlService.sqlFileWrite(filePath, idType);
            } else if (BusinessTypeEnum.TEMPNEWTABLEINSERTSQL.getBusinessType().equals(businessType)) {
                //上传文件
                String filePath = fileToSqlService.fileUpload(request);
                zipPath = fileToSqlService.temp_newTableFile(filePath);
            } else if (BusinessTypeEnum.EXCELTOJSON.getBusinessType().equals(businessType)) {
                //上传文件
                String filePath = fileToSqlService.fileUpload(request);
                zipPath = fileToSqlService.jsonFileWrite(filePath);
            } else if (BusinessTypeEnum.LANGUAGETRANSLATIONMATCH.getBusinessType().equals(businessType)) {
                //上传文件
                String filePath = fileToSqlService.fileUpload(request);
                zipPath = fileToSqlService.translationMatch(filePath);
            } else if (BusinessTypeEnum.CONVERTKEYSANDFILTERDUPLICATES.getBusinessType().equals(businessType)) {
                //上传文件
                String filePath = fileToSqlService.fileUpload(request);
                zipPath = fileToSqlService.keyChangeAndFilter(filePath);
            } else if (BusinessTypeEnum.PROPERTIESWRITEINEXCEL.getBusinessType().equals(businessType)) {
                //上传文件
                String filePath = fileToSqlService.fileUpload(request);
                zipPath = fileToSqlService.propertiesWriteInExcel(filePath);
            } else if (BusinessTypeEnum.TSTOEXCEL.getBusinessType().equals(businessType)) {
                //上传文件
                String filePath = fileToSqlService.fileUpload(request);
                //注：此处预留了扩展其他多语言的功能，但是目前写死只支持中文
                zipPath = fileToSqlService.xmlToExcel(filePath, Arrays.asList("en_translation"));
            } else if (BusinessTypeEnum.EXCELTOTS.getBusinessType().equals(businessType)) {
                //上传文件
                String filePath = fileToSqlService.fileUpload(request);
                zipPath = fileToSqlService.excelToXml(filePath);
            } else if (BusinessTypeEnum.REMOVEBLANKTAGS.getBusinessType().equals(businessType)) {
                //上传文件
                String filePath = fileToSqlService.fileUpload(request);
                zipPath = fileToSqlService.removeBlankTags(filePath);
            } else if (BusinessTypeEnum.GENERATEMETAINFFILE.getBusinessType().equals(businessType)) {
                //上传文件
                String filePath = fileToSqlService.fileUpload(request);
                List<Pair<String, String>> mapList = PackageInfoEnum.parameterMapToPackageInfoEnumList(parameterMap);
                zipPath = fileToSqlService.generateMETAINFFile(filePath, mapList);
            } else if (BusinessTypeEnum.GENERATELANGUAGEPACKSBASEDONEXCEL.getBusinessType().equals(businessType)) {
                List<MultipartFile> fileList = fileToSqlService.getMultipleFileList(request);
                zipPath = fileToSqlService.generateLanguagePackByExcel(fileList);
            } else if (BusinessTypeEnum.COMPAREFILE.getBusinessType().equals(businessType)) {
                List<MultipartFile> fileList = fileToSqlService.getMultipleFileList(request);
                zipPath = fileToSqlService.fileCompare(fileList);
            } else if (BusinessTypeEnum.YAMLEXCELINTERCHANGE.getBusinessType().equals(businessType)) {
                List<MultipartFile> fileList = fileToSqlService.getMultipleFileList(request);
                zipPath = fileToSqlService.yamlExcelInterchange(fileList);
            } else if (BusinessTypeEnum.ARTEMISREMOVEINTERFACEBYSERVICENAMEORGROUPNAME.getBusinessType().equals(businessType)) {
                String filePath = fileToSqlService.fileUpload(request);
                List<String> serviceNameList = new ArrayList<>(new HashSet<>(Arrays.asList(parameterMap.get("serviceName")[0].split(","))));
                serviceNameList= serviceNameList.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
                List<String> groupNameList = new ArrayList<>(new HashSet<>(Arrays.asList(parameterMap.get("groupName")[0].split(","))));
                groupNameList= groupNameList.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
                zipPath = fileToSqlService.artemisRemoveInterfaceByserviceNameORgroupName(filePath, serviceNameList, groupNameList);
            }
            result.setData(zipPath);
            //文件返回到前端下载
            //fileToSqlService.fileDownLoad(zipPath,response);
        } catch (Exception e) {
            logger.warn(e.toString());
            result.setCode("0x001");
            result.setMessage(e.toString());
            return result;
        }
        return result;
    }


    @RequestMapping("/download")
    public void download(@RequestParam("zipPath") String zipPath, HttpServletResponse response) {
        //文件返回到前端下载
        fileToSqlService.fileDownLoad(zipPath, response);
    }

    @RequestMapping("/downloadReference")
    public void downdownloadReferenceload(HttpServletResponse response) {
        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String sqlDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "reference" + File.separator + "reference.xls";
        //文件返回到前端下载
        fileToSqlService.fileDownLoad(sqlDirPath, response);
    }

    @RequestMapping("/downloadReference2")
    public void downdownloadReferenceload2(HttpServletResponse response) {
        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String sqlDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "reference" + File.separator + "reference2.zip";
        //文件返回到前端下载
        fileToSqlService.fileDownLoad(sqlDirPath, response);
    }

    @RequestMapping("/downloadReference3")
    public void downdownloadReferenceload3(HttpServletResponse response) {
        //当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String sqlDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "reference" + File.separator + "tools.zip";
        //文件返回到前端下载
        fileToSqlService.fileDownLoad(sqlDirPath, response);
    }
}
