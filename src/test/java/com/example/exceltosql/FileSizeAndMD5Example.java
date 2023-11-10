package com.example.exceltosql;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileSizeAndMD5Example {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\sunjian23\\Desktop\\cis_language_2.1.4.20230530093940\\META-INF\\file_checksum.xml";

        try {
            long originalFileSize = getFileSize(filePath);
            String originalMD5 = getFileMD5(filePath);

            String fileInfo = "文件大小: " + originalFileSize + "字节\r\n" +
                    "MD5码: " + originalMD5 + "\r\n";

//            writeFileInfo(filePath, fileInfo);

            long newFileSize = getFileSize(filePath);
            String newMD5 = getFileMD5(filePath);

            System.out.println("原始文件大小：" + originalFileSize + "字节");
            System.out.println("原始MD5码：" + originalMD5);
            System.out.println("新文件大小：" + newFileSize + "字节");
            System.out.println("新的MD5码：" + newMD5);
        } catch (IOException e) {
            System.out.println("操作失败：" + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("操作失败，不支持MD5算法：" + e.getMessage());
        }
    }

    public static long getFileSize(String filePath) throws IOException {
        File file = new File(filePath);
        return file.length();
    }

    public static String getFileMD5(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(filePath))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = bis.read(buffer)) != -1) {
                md.update(buffer, 0, bytesRead);
            }
        }

        byte[] mdBytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte mdByte : mdBytes) {
            sb.append(Integer.toString((mdByte & 0xFF) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    public static void writeFileInfo(String filePath, String fileInfo) throws IOException {
        Path tempFilePath = Files.createTempFile("temp", ".txt");
        File tempFile = tempFilePath.toFile();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write(fileInfo);
        }

        RandomAccessFile originalFile = new RandomAccessFile(filePath, "rw");

        // 备份原始文件的内容
        byte[] content = new byte[(int) originalFile.length()];
        originalFile.readFully(content);

        // 将临时文件的内容写入原始文件
        originalFile.seek(0);
        try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(tempFile))) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                originalFile.write(buffer, 0, bytesRead);
            }
        }

        // 恢复原始文件的内容
        originalFile.seek(fileInfo.getBytes().length);
        originalFile.write(content);

        // 调整文件大小
        originalFile.setLength(originalFile.length() + fileInfo.getBytes().length);

        tempFile.delete();
        originalFile.close();
    }
}