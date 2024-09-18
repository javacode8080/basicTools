package com.example.exceltosql.Util;

import com.alibaba.fastjson.JSONObject;
import com.example.exceltosql.constant.CommonConstant;
import com.example.exceltosql.entity.CurlResult;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author :sunjian23
 * @date : 2024/9/18 5:05
 */
public class DigitalSignatureUtil {


    public static String fileProcessing(String filePath, String originalFilename) {

        //1.当前用户程序所在的目录
        String userDir = System.getProperties().getProperty("user.dir");
        String zipDirPath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "download" + File.separator + "DigitalSignature";
        File zipDir = new File(zipDirPath);
        //2.判断保存文件所对应路径是否存在
        if (!zipDir.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            zipDir.mkdirs();
        }
        //3.最终导出的文件名字
        String finalPath = zipDirPath + File.separator + originalFilename;
        //4.调用的工具名字
        String curlToolPath = userDir + File.separator + "outTool" + File.separator + "curl" + File.separator + "curl.exe";
        if (filePath.endsWith(CommonConstant.ZIP_SUFFIX)) {
            String cmdString1 = cmdString(filePath, finalPath, curlToolPath, false);
            String result1 = cmdCommand(cmdString1);
            CurlResult curlResult = null;
            try {
                curlResult = JSONObject.parseObject(result1, CurlResult.class);
            } catch (Exception e) {
                //无法转换成对象说明当前结果正常，可以直接输出
                String cmdString = cmdString(filePath, finalPath, curlToolPath, true);
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(cmdString)) {
                    String result = cmdCommand(cmdString);
                    if (StringUtils.isEmpty(result)) {
                        //1.首先要读取一下文件是否是存在异常信息
                        return finalPath;
                    } else {
                        throw new RuntimeException("文件:" + originalFilename + "数字签名生成失败！\n" + result);
                    }
                } else {
                    throw new RuntimeException("文件:" + originalFilename + "数字签名生成失败！\n");
                }
            }
            if (null != curlResult) {
                throw new RuntimeException(curlResult.getResultString());
            } else {
                throw new RuntimeException("文件:" + originalFilename + "数字签名生成失败！\n");
            }

        } else {
            throw new RuntimeException("文件:" + originalFilename + "格式不符合，请将文件后缀修改为.zip！\n");
        }

    }

    private static void outFile(byte[] bytes, String filePath) {
        try {
            Files.write(Paths.get(filePath), bytes);
        } catch (IOException e) {
            throw new RuntimeException("文件生成异常");
        }
    }


    //执行CMD命令
    public static String cmdCommand(String command) {
        StringBuilder sb = new StringBuilder("");
        BufferedReader reader = null;
        try {
            Process process = Runtime.getRuntime().exec(command); // 执行命令
            reader = new BufferedReader(new InputStreamReader(process.getInputStream(), "GBK")); // 获取命令输出流
            String line;
            while ((line = reader.readLine()) != null) { // 逐行读取输出结果
                sb.append(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("执行命令<" + command + ">失败!");
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String cmdString(String filePath, String finalPath, String curlToolPath, boolean isCout) {
        File file = new File(finalPath);
        boolean newFile = false;
        try {
            if (file.exists()) {
                //文件存在则删除该文件
                file.delete();
            }
            newFile = file.createNewFile();
        } catch (IOException e) {
            throw new RuntimeException("文件创建失败！\n");
        }
        if (!newFile) {
            throw new RuntimeException("文件创建失败！\n");
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(curlToolPath)
                .append(CommonConstant.SPACE)
                .append(" -F \"auth_id=44ad8108-19f4-7aa3-c199-8051a6ca8a44\" -F \"keyIndex=hiksign_e38d59f0\" -F \"signType=zip\" -F \"file=@")
                .append(filePath)
                .append("\" \"https://sign.hikvision.com.cn/sign/sign_arm_abstract.php\" -k")
                .append(CommonConstant.SPACE);
        if (isCout) {
            stringBuilder.append(" -o").append(finalPath);
        }
        return stringBuilder.toString();
    }

}

