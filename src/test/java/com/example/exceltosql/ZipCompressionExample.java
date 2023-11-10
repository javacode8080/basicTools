package com.example.exceltosql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCompressionExample {
    public static void main(String[] args) {
        String sourceFolder = "C:\\Users\\sunjian23\\Desktop\\ls_resource_for_hce_2.0.0_2.3.105.20230608092319 - 副本";
        String destinationZipFile = "C:\\Users\\sunjian23\\Desktop\\ls_resource_for_hce_2.0.0_2.3.105.20230608092319 - 副本.zip";

        try {
            zipFolderContents(sourceFolder, destinationZipFile);
            System.out.println("压缩完成");
        } catch (IOException e) {
            System.out.println("压缩失败：" + e.getMessage());
        }
    }

    public static void zipFolderContents(String sourceFolder, String destinationZipFile) throws IOException {
        FileOutputStream fos = new FileOutputStream(destinationZipFile);
        ZipOutputStream zos = new ZipOutputStream(fos);

        File folder = new File(sourceFolder);
        File[] files = folder.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                addToZip(file, "", zos);
            } else if (file.isDirectory()) {
                addFolderToZip(file, "", zos);
            }
        }

        zos.close();
        fos.close();
    }

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

        fis.close();
        zos.closeEntry();
    }

    private static void addFolderToZip(File folder, String parentFolderPath, ZipOutputStream zos) throws IOException {
        File[] files = folder.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                addToZip(file, parentFolderPath + folder.getName() + "/", zos);
            } else if (file.isDirectory()) {
                addFolderToZip(file, parentFolderPath + folder.getName() + "/", zos);
            }
        }
    }
}