package com.example.exceltosql.Util;

import com.example.exceltosql.constant.CommonConstant;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;


@Deprecated
public class FileUtil {

    private static final byte[] READ_WRITE_LOCK = new byte[]{};

    /**
     * @param filePath 文件路径
     * @function 删除文件
     */
    private static void deletePack(String filePath) throws Exception {
        File productPack = new File(filePath);
        try {
            FileUtils.forceDelete(productPack);
        } catch (IOException e) {
            throw new Exception(e.getMessage());
        }
    }

    /**
     * @param file        上传文件
     * @param destination 目标路径
     * @function 保存文件到指定路径
     */
    public static boolean saveFile(MultipartFile file, String destination) throws Exception {
        try {
            File desFile = new File(destination);
            if (!desFile.getParentFile().exists()) {
                FileUtils.forceMkdir(desFile.getParentFile());
            }
            file.transferTo(desFile);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * @param filePath 文件路径
     * @return 文件名
     * @function 从文件路径中获取文件名
     */
    public static String fileName(String filePath) {
        try {
            int index = filePath.lastIndexOf(CommonConstant.SLASH);
            if (index == -1) {
                index = filePath.lastIndexOf(CommonConstant.BACK_SLASH);
            }
            return filePath.substring(index + 1);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @param filePath 文件路径
     * @return 文件夹路径
     * @function 从文件路径中获取文件夹路径
     */
    public static String dirPath(String filePath) {
        try {
            int index = filePath.lastIndexOf(CommonConstant.SLASH);
            if (index == -1) {
                index = filePath.lastIndexOf(CommonConstant.BACK_SLASH);
            }
            return filePath.substring(0, index + 1);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return 文件夹路径
     * @function 创建父文件夹
     */
    public static void mkdir(File file) throws Exception {
        if (!file.getParentFile().exists()) {
            try {
                FileUtils.forceMkdir(file.getParentFile());
            } catch (Exception e) {
                throw new Exception(e);
            }
        }
    }

    /**
     * @param srcDir 源文件夹
     * @param desDir 目标文件夹
     * @function 拷贝文件夹
     */
    public static boolean copyPackageDir(String srcDir, String desDir) {
        try {
            File srcFile = new File(srcDir);
            File desFile = new File(desDir);
            if (!srcFile.exists()) {
                return false;
            }
            if (!desFile.getParentFile().exists()) {
                FileUtils.forceMkdir(desFile.getParentFile());
            }
            FileUtils.copyDirectory(srcFile, desFile);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
