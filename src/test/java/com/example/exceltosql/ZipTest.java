package com.example.exceltosql;

import org.objectweb.asm.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author :sunjian23
 * @date : 2023/8/18 4:35
 */
public class ZipTest {
    private static final String testClassFile = "C:\\Users\\sunjian23\\Desktop\\test2\\hik-common-adapter-0.0.2\\com\\hikvision\\bamboo\\common\\adapter\\CpuArchUtil.class";

    public static void main(String[] args) throws IOException {
        String classFilePath = testClassFile;

        byte[] bytes = Files.readAllBytes(Paths.get(classFilePath));

        ClassReader cr = new ClassReader(bytes);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM8) {
            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                // 打印类名
                System.out.println("Class: " + name);

                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                // 打印类的方法
                System.out.println(name + descriptor);

                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                // 打印类的字段
                System.out.println(name + " : " + descriptor);

                return super.visitField(access, name, descriptor, signature, value);
            }

            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {
                // 打印类的内部类信息
                System.out.println(name);

                super.visitInnerClass(name, outerName, innerName, access);
            }
        };

        cr.accept(cv, ClassReader.SKIP_DEBUG | ClassReader.SKIP_CODE);
    }


    private static final String INPUT_ZIP_FILE = "C:\\Users\\sunjian23\\Desktop\\新建文件夹1\\com\\hikvision\\test\\pom-hik-install.zip";
    private static final String OUTPUT_ZIP_FILE = "C:\\Users\\sunjian23\\Desktop\\新建文件夹1\\com\\hikvision\\test\\pom-hik-install1.zip";
    private static final String SEARCH_STRING = "hik";
    private static final String REPLACEMENT_STRING = "company";


    private static void replaceInZip(String inputFile, String outputFile) throws IOException {
        FileInputStream fis = new FileInputStream(inputFile);
        ZipInputStream zis = new ZipInputStream(fis);

        FileOutputStream fos = new FileOutputStream(outputFile);
        ZipOutputStream zos = new ZipOutputStream(fos);

        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            // 获取当前文件名
            String fileName = entry.getName();

            // 创建临时字节数组输出流
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // 将当前文件内容复制到临时字节数组输出流中
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }

            // 关闭当前文件的输入流
            zis.closeEntry();

            // 获取临时字节数组
            byte[] fileBytes = baos.toByteArray();

            // 替换文件名和文件内容中的字符串
            String replacedFileName = replaceString(fileName);
            byte[] replacedFileBytes = replaceString(new String(fileBytes)).getBytes();

            // 创建新的ZipEntry，并将其添加到输出流中
            ZipEntry replacedEntry = new ZipEntry(replacedFileName);
            zos.putNextEntry(replacedEntry);
            zos.write(replacedFileBytes, 0, replacedFileBytes.length);

            // 关闭当前ZipEntry的输出流
            zos.closeEntry();
        }

        // 关闭输入流和输出流
        zis.close();
        zos.close();
    }

    private static String replaceString(String original) {
        return original.replaceAll("(?i)" + SEARCH_STRING, REPLACEMENT_STRING);
    }

}
