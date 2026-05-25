package com.ecommerce.detail.ai.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件工具类
 * 提供文件读写、格式转换等通用方法
 * 
 * @author Administrator
 * @version 1.0.0
 */
@Slf4j
@Component
public class FileUtil {

    /**
     * 读取文本文件内容
     * 
     * @param filePath 文件路径
     * @return 文件内容
     */
    public static String readTextFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("读取文件失败: {}", filePath, e);
            throw new RuntimeException("读取文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 写入文本文件
     * 
     * @param filePath 文件路径
     * @param content 文件内容
     */
    public void writeTextFile(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.write(path, content.getBytes(StandardCharsets.UTF_8));
            log.info("文件写入成功: {}", filePath);
        } catch (IOException e) {
            log.error("写入文件失败: {}", filePath, e);
            throw new RuntimeException("写入文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 追加文本到文件
     * 
     * @param filePath 文件路径
     * @param content 要追加的内容
     */
    public void appendTextFile(String filePath, String content) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.write(path, content.getBytes(StandardCharsets.UTF_8), 
                       java.nio.file.StandardOpenOption.CREATE, 
                       java.nio.file.StandardOpenOption.APPEND);
            log.debug("文件追加成功: {}", filePath);
        } catch (IOException e) {
            log.error("追加文件失败: {}", filePath, e);
            throw new RuntimeException("追加文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 读取文件为字节数组
     * 
     * @param filePath 文件路径
     * @return 字节数组
     */
    public byte[] readFileToBytes(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error("读取文件为字节失败: {}", filePath, e);
            throw new RuntimeException("读取文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 写入字节数组到文件
     * 
     * @param filePath 文件路径
     * @param data 字节数据
     */
    public void writeBytesToFile(String filePath, byte[] data) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.write(path, data);
            log.info("字节文件写入成功: {}", filePath);
        } catch (IOException e) {
            log.error("写入字节文件失败: {}", filePath, e);
            throw new RuntimeException("写入文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 删除文件
     * 
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("删除文件失败: {}", filePath, e);
            return false;
        }
    }

    /**
     * 检查文件是否存在
     * 
     * @param filePath 文件路径
     * @return 是否存在
     */
    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 创建目录
     * 
     * @param dirPath 目录路径
     */
    public void createDirectory(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            Files.createDirectories(path);
            log.debug("目录创建成功: {}", dirPath);
        } catch (IOException e) {
            log.error("创建目录失败: {}", dirPath, e);
            throw new RuntimeException("创建目录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 复制文件
     * 
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     */
    public void copyFile(String sourcePath, String targetPath) {
        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);
            Files.createDirectories(target.getParent());
            Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            log.info("文件复制成功: {} -> {}", sourcePath, targetPath);
        } catch (IOException e) {
            log.error("复制文件失败: {} -> {}", sourcePath, targetPath, e);
            throw new RuntimeException("复制文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取文件扩展名
     * 
     * @param fileName 文件名
     * @return 扩展名（不含点）
     */
    public static String getFileExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return fileName.substring(lastIndexOf + 1).toLowerCase();
    }

    /**
     * 获取文件名（不含扩展名）
     * 
     * @param fileName 文件名
     * @return 文件名
     */
    public String getFileNameWithoutExtension(String fileName) {
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return fileName;
        }
        return fileName.substring(0, lastIndexOf);
    }

    /**
     * 读取CSV文件
     * 
     * @param filePath CSV文件路径
     * @return CSV数据列表（每行为一个字符串数组）
     */
    public List<String[]> readCSV(String filePath) {
        List<String[]> dataList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                // 简单CSV解析，实际生产环境建议使用专业CSV库
                String[] values = line.split(",");
                dataList.add(values);
            }
        } catch (IOException e) {
            log.error("读取CSV文件失败: {}", filePath, e);
            throw new RuntimeException("读取CSV文件失败: " + e.getMessage(), e);
        }
        return dataList;
    }

    /**
     * 写入CSV文件
     * 
     * @param filePath CSV文件路径
     * @param dataList 数据列表
     * @param headers 表头
     */
    public void writeCSV(String filePath, List<String[]> dataList, String[] headers) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            
            // 写入表头
            if (headers != null && headers.length > 0) {
                writer.write(String.join(",", headers));
                writer.newLine();
            }
            
            // 写入数据
            for (String[] row : dataList) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
            
            writer.flush();
            log.info("CSV文件写入成功: {}", filePath);
        } catch (IOException e) {
            log.error("写入CSV文件失败: {}", filePath, e);
            throw new RuntimeException("写入CSV文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 生成唯一文件名
     * 
     * @param originalFileName 原始文件名
     * @return 唯一文件名
     */
    public String generateUniqueFileName(String originalFileName) {
        String extension = getFileExtension(originalFileName);
        String nameWithoutExt = getFileNameWithoutExtension(originalFileName);
        long timestamp = System.currentTimeMillis();
        
        if (extension.isEmpty()) {
            return nameWithoutExt + "_" + timestamp;
        } else {
            return nameWithoutExt + "_" + timestamp + "." + extension;
        }
    }

    /**
     * 获取文件大小（字节）
     * 
     * @param filePath 文件路径
     * @return 文件大小
     */
    public long getFileSize(String filePath) {
        try {
            Path path = Paths.get(filePath);
            return Files.size(path);
        } catch (IOException e) {
            log.error("获取文件大小失败: {}", filePath, e);
            return 0;
        }
    }

    /**
     * 格式化文件大小
     * 
     * @param sizeInBytes 文件大小（字节）
     * @return 格式化后的字符串（如：1.5 MB）
     */
    public String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024 * 1024) {
            return String.format("%.2f KB", sizeInBytes / 1024.0);
        } else if (sizeInBytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", sizeInBytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", sizeInBytes / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 批量删除文件
     * 
     * @param filePaths 文件路径列表
     * @return 成功删除的文件数量
     */
    public static int deleteFiles(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            return 0;
        }
        
        int deletedCount = 0;
        for (String filePath : filePaths) {
            if (deleteFile(filePath)) {
                deletedCount++;
            }
        }
        log.info("批量删除文件完成，成功删除 {} 个文件", deletedCount);
        return deletedCount;
    }

    /**
     * 从图片中提取文本（OCR）
     * 
     * @param imagePaths 图片路径列表
     * @return 提取的文本内容
     */
    public static String extractTextFromImages(List<String> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return "";
        }

        throw new UnsupportedOperationException("图片OCR尚未配置服务提供方，已阻止返回占位OCR文本");
    }

    /**
     * 从文档中提取文本
     * 
     * @param documentPaths 文档路径列表
     * @return 提取的文本内容
     */
    public static String extractTextFromDocuments(List<String> documentPaths) {
        if (documentPaths == null || documentPaths.isEmpty()) {
            return "";
        }
        
        StringBuilder textContent = new StringBuilder();
        for (String docPath : documentPaths) {
            String extension = getFileExtension(docPath);
            if ("txt".equalsIgnoreCase(extension)) {
                textContent.append(readTextFile(docPath)).append("\n");
            } else if ("docx".equalsIgnoreCase(extension)) {
                textContent.append(readDocxText(docPath)).append("\n");
            } else {
                throw new UnsupportedOperationException("暂不支持解析" + extension + "文档，已阻止返回占位文档文本: " + docPath);
            }
        }
        
        log.info("从 {} 个文档中提取文本完成", documentPaths.size());
        return textContent.toString();
    }

    private static String readDocxText(String docPath) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(docPath));
             XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (IOException e) {
            log.error("解析Word文档失败: {}", docPath, e);
            throw new RuntimeException("解析Word文档失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证图片文件格式
     * 
     * @param imagePaths 图片路径列表
     * @return 是否全部有效
     */
    public static boolean validateImageFiles(List<String> imagePaths) {
        if (imagePaths == null || imagePaths.isEmpty()) {
            return true;
        }
        
        List<String> validExtensions = List.of("jpg", "jpeg", "png", "gif", "webp", "bmp");
        
        for (String imagePath : imagePaths) {
            String extension = getFileExtension(imagePath);
            if (!validExtensions.contains(extension)) {
                log.warn("无效的图片格式: {}", imagePath);
                return false;
            }
            if (!fileExists(imagePath)) {
                log.warn("图片文件不存在: {}", imagePath);
                return false;
            }
        }
        
        return true;
    }

    /**
     * 验证视频文件格式
     * 
     * @param videoPaths 视频路径列表
     * @return 是否全部有效
     */
    public static boolean validateVideoFiles(List<String> videoPaths) {
        if (videoPaths == null || videoPaths.isEmpty()) {
            return true;
        }
        
        List<String> validExtensions = List.of("mp4", "avi", "mov", "wmv", "flv", "mkv", "webm");
        
        for (String videoPath : videoPaths) {
            String extension = getFileExtension(videoPath);
            if (!validExtensions.contains(extension)) {
                log.warn("无效的视频格式: {}", videoPath);
                return false;
            }
            if (!fileExists(videoPath)) {
                log.warn("视频文件不存在: {}", videoPath);
                return false;
            }
        }
        
        return true;
    }
}
