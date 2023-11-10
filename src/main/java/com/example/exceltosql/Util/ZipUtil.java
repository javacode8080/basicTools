package com.example.exceltosql.Util;


import com.example.exceltosql.constant.CommonConstant;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author :sunjian23
 * @date : 2022/12/29 15:37
 */
public class ZipUtil {

    //文件操作流缓冲区大小
    private static final int BUFFERSIZE = 10240;
    //临时文件基本路径
    private static String basePath;

    //静态加载解压基本路径
    static {
        String userDir = System.getProperties().getProperty("user.dir");
        basePath = userDir + File.separator + "documents" + File.separator + "file" + File.separator + "unzip" + File.separator + "propertiesWriteInExcel";
    }


    /**
     * @param fileZip:
     * @param pathToDest:指定路径(文件夹路径)，路径最后要带/
     * @return void
     * @author sunjian23
     * @description TODO:解压zip文件到指定路径
     * @date 2022/12/29 17:04
     */
    public static void unzip(String fileZip, String pathToDest) throws IOException {
        if (null == fileZip || "".equals(fileZip) || (!fileZip.contains(CommonConstant.ZIP_SUFFIX) && !fileZip.contains(CommonConstant.WAR_SUFFIX))) {
            IOException e = new IOException("文件路径格式错误！");
            throw e;
        }
        String name = "";
        BufferedOutputStream dest = null;
        BufferedInputStream is = null;
        FileOutputStream fos = null;
        ZipEntry entry = null;
        ZipFile zipfile = null;
        try {

            //指定字符集，文件包含中文的情况下不指定字符集会报错
            zipfile = new ZipFile(fileZip, Charset.forName(CommonConstant.GBK));

            Enumeration dir = zipfile.entries();
            while (dir.hasMoreElements()) {
                entry = (ZipEntry) dir.nextElement();
                if (entry.isDirectory()) {
                    name = entry.getName();
                    name = name.substring(0, name.length() - 1);
                    File fileObject = new File(pathToDest + name);
                    fileObject.mkdirs();
                }
            }

            Enumeration e = zipfile.entries();
            while (e.hasMoreElements()) {
                entry = (ZipEntry) e.nextElement();
                if (entry.isDirectory()) {
                    continue;
                } else {
                    is = new BufferedInputStream(zipfile.getInputStream(entry));
                    int count;
                    byte[] dataByte = new byte[BUFFERSIZE];
                    fos = new FileOutputStream(pathToDest + entry.getName());
                    dest = new BufferedOutputStream(fos, BUFFERSIZE);
                    while ((count = is.read(dataByte, 0, BUFFERSIZE)) != -1) {
                        dest.write(dataByte, 0, count);
                    }
                    dest.flush();
                    dest.close();
                    fos.close();
                    is.close();
                }
            }
        } catch (IOException e) {
            throw new IOException("文件解压失败！");
        } finally {
            if (null != dest) {
                dest.close();
            }
            if (null != fos) {
                fos.close();
            }
            if (null != is) {
                is.close();
            }
            if (null != zipfile) {
                zipfile.close();
            }
        }
    }

    /**
     * @param file:
     * @param pathToDest:指定路径(文件夹路径)
     * @return void
     * @author sunjian23
     * @description TODO:解压zip文件到指定路径
     * @date 2022/12/29 17:10
     */
    public static void unzip(File file, String pathToDest) throws IOException {
        String fileZip = file.getAbsolutePath();
        unzip(fileZip, pathToDest);
    }


    /**
     * @param multipartFile:
     * @param pathToDest:指定路径(文件夹路径)
     * @return void
     * @author sunjian23
     * @description TODO:解压zip文件到指定路径
     * @date 2022/12/29 17:10
     */
    public static void unzip(MultipartFile multipartFile, String pathToDest) throws IOException {
        File file = getFile(multipartFile);
        unzip(file, pathToDest);
        if (file.exists()) {
            //删除临时文件
            FileUtils.forceDelete(file);
        }
    }

    /**
     * MultipartFile 转 File
     *
     * @param multipartFile:
     * @return File
     * @author sunjian23
     * @description TODO:将上传的文件存到本地默认路径
     * @date 2022/12/29 16:26
     */
    private static File getFile(MultipartFile multipartFile) throws IOException {
        String fileName = System.currentTimeMillis() + CommonConstant.UNDER_LINE + multipartFile.getOriginalFilename();
        String filePath = basePath + fileName;
        return getFile(multipartFile, filePath);
    }

    /**
     * MultipartFile 转 File
     *
     * @param multipartFile:
     * @param filePath:指定路径(包含文件名，可自定义文件名)
     * @return File
     * @author sunjian23
     * @description TODO:将上传的文件存到指定路径
     * @date 2022/12/29 16:23
     */
    private static File getFile(MultipartFile multipartFile, String filePath) throws IOException {
        File file = new File(filePath);
        //文件所属文件夹不存在则创建改文件夹
        if (!file.getParentFile().exists()) {
            FileUtils.forceMkdir(file.getParentFile());
        }
        int len;
        try (OutputStream os = new FileOutputStream(file);
             InputStream in = multipartFile.getInputStream()) {
            byte[] buffer = new byte[BUFFERSIZE];
            while ((len = in.read(buffer, 0, BUFFERSIZE)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            throw new IOException("上传文件保存到本地失败！");
        }
        return file;
    }


    /**
     * @param directoryPath:指定路径(文件夹路径)
     * @param fileName:文件名(模糊匹配文件名，不包括文件类型)
     * @return void
     * @author sunjian23
     * @description TODO:找到指定文件夹下的指定文件
     * @date 2022/12/29 18:32
     */
    public static List<File> searchFile(String directoryPath, String fileName) {
        File file = new File(directoryPath);
        if (!file.exists()) {
            return null;
        }
        List<File> list = new ArrayList<>();
        searchFile(file, fileName, list);
        return list;
    }

    private static void searchFile(File directoryFile, String fileName, List list) {
        File[] files = directoryFile.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                searchFile(f, fileName, list);
            } else {
                //根据文件名进行模糊查询
                String fileStr = fileName.substring(0, fileName.lastIndexOf(CommonConstant.DOT));
                if (f.getName().contains(fileStr)) {
                    list.add(f);
                }
            }
        }
    }

    /**
     * @param directoryPath:
     * @return List<File>
     * @author sunjian23
     * @description TODO：获取文件夹下的所有文件
     * @date 2023/2/11 14:37
     */
    public static List<File> searchFile(String directoryPath) {
        File file = new File(directoryPath);
        if (!file.exists()) {
            return null;
        }
        List<File> list = new ArrayList<>();
        searchFile(file, list);
        return list;
    }

    /**
     * @param directoryFile:
     * @param list:
     * @return void
     * @author sunjian23
     * @description TODO：获取文件夹下的所有文件
     * @date 2023/2/11 14:36
     */
    private static void searchFile(File directoryFile, List list) {
        File[] files = directoryFile.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                searchFile(f, list);
            } else {
                list.add(f);
            }
        }
    }


    /**
     * @param file zip文件对象[支持MultipartFile,File,String]
     * @return boolean
     * @author sunjian23
     * @description TODO：获取压缩包中指定文件并复制到指定位置
     * @date 2022/12/29 20:09
     */
    public static List<File> unzipAndGetFilePath(Object file) {
        String directoryPath = basePath + UUID.randomUUID().toString() + File.separator;
        File directoryPathFile = new File(directoryPath);
        if (!directoryPathFile.exists()) {
            //若不存在，则创建多级目录【用mkdirs()】
            directoryPathFile.mkdirs();
        }
        try {
            if (file instanceof MultipartFile) {
                unzip((MultipartFile) file, directoryPath);
            } else if (file instanceof File) {
                unzip((File) file, directoryPath);
            } else if (file instanceof String) {
                unzip((String) file, directoryPath);
            } else {
                throw new RuntimeException("file不支持类型：" + file.getClass().getName());
            }
            List<File> files = searchFile(directoryPath);
            if (null == files || files.size() == 0) {
                throw new RuntimeException("压缩包中不存在该文件！");
            }
            return files;
        } catch (IOException e) {
            throw new RuntimeException("unzip file error");
        }
    }

    public static Map<String, File> unzipAndGetFilePathByMap(Object file) {
        List<File> files = unzipAndGetFilePath(file);
        return listFileToMapFile(files);
    }


    public static Map<String, File> listFileToMapFile(List<File> files) {
        Map<String, File> map = new HashMap<>();
        if (null == files || files.size() == 0) {
            return map;
        }
        try {
            return map = files.stream().collect(Collectors.toMap(File::getName, Function.identity()));
        } catch (Exception e) {
            throw new RuntimeException("压缩包内不可以有重复的文件名，文件名应根据Excel列名进行对应");
        }
    }

    /**
     * @param filePath:
     * @return void
     * @author sunjian23
     * @description TODO：删除指定路径下的文件/文件夹
     * @date 2022/12/29 20:14
     */
    private static void deletePack(String filePath) throws IOException {
        File productPack = new File(filePath);
        FileUtils.forceDelete(productPack);
    }


    /**
     * @param sourceFolder:                        源文件(夹)路径
     * @param destinationZipFile:目标压缩包路径(具体到压缩包名称)
     * @return void
     * @author sunjian23
     * @description TODO：压缩文件/文件夹，压缩文件夹内部不包含当前文件夹
     * @date 2023/6/27 19:53
     */
    public static void zipFolderContents(String sourceFolder, String destinationZipFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(destinationZipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);

        File folder = new File(sourceFolder);
        //判断当前是否为文件夹
        if (folder.isDirectory()) {
            //是文件夹去内部文件循环遍历压缩
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    addToZip(file, "", zos);
                } else if (file.isDirectory()) {
                    addFolderToZip(file, "", zos);
                }
            }
        } else {
            addToZip(folder, "", zos);
        }
        zos.close();
        fos.close();
    }

    /**
     * @param file:
     * @param parentFolderPath:
     * @param zos:
     * @return void
     * @author sunjian23
     * @description TODO：添加待压缩文件到zip流
     * @date 2023/6/27 19:58
     */
    private static void addToZip(File file, String parentFolderPath, ZipOutputStream zos) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        String zipFilePath = parentFolderPath + file.getName();
        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            zos.write(buffer, 0, bytesRead);
        }
        zos.closeEntry();
        fis.close();
    }

    /**
     * @param folder:
     * @param parentFolderPath:注意，对于压缩的方法，我们在记录路径是一定要使用反斜杠，而不能用File.separator去判断操作系统，这是因为在ZipEntry类的底层写死了判断是反斜杠，参考entry.isDirectory(),可以看到内部判断写死为反斜杠
     * @param zos:
     * @return void
     * @author sunjian23
     * @description TODO：递归遍历带压缩文件夹内容添加到zip流
     * @date 2023/6/27 19:59
     */
    private static void addFolderToZip(File folder, String parentFolderPath, ZipOutputStream zos) throws IOException {
        // 添加文件夹的ZipEntry(一定不能忘记对文件夹记录zipEntry，否则就会出现解压失败的情况)
        ZipEntry entry = new ZipEntry(parentFolderPath + folder.getName() + CommonConstant.SLASH);
        zos.putNextEntry(entry);
        zos.closeEntry();//回收防止资源泄露
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                addToZip(file, parentFolderPath + folder.getName() + CommonConstant.SLASH, zos);
            } else if (file.isDirectory()) {
                addFolderToZip(file, parentFolderPath + folder.getName() + CommonConstant.SLASH, zos);
            }
        }
    }
}
