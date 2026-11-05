package com.example.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.Result;
import com.example.service.ImageThumbService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.OutputStream;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件接口
 * 图片下发优化：流式输出（不再整文件读入堆内存）+ 正确 Content-Type + 浏览器长缓存
 * 缩略图：/files/thumb/{flag} 懒生成（WebP），列表页加载缩略图、详情页加载原图
 */
@RestController
@RequestMapping("/files")
public class FileController {

    // 文件上传存储路径
    private static final String filePath = System.getProperty("user.dir") + "/files/";

    /** 上传扩展名白名单（系统所有上传入口均为图片/附件） */
    private static final List<String> ALLOWED_EXTS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp", "pdf");

    /** 图片扩展名 -> Content-Type */
    private static final Map<String, String> CONTENT_TYPES = new HashMap<>();
    static {
        CONTENT_TYPES.put("jpg", "image/jpeg");
        CONTENT_TYPES.put("jpeg", "image/jpeg");
        CONTENT_TYPES.put("png", "image/png");
        CONTENT_TYPES.put("gif", "image/gif");
        CONTENT_TYPES.put("webp", "image/webp");
        CONTENT_TYPES.put("bmp", "image/bmp");
        CONTENT_TYPES.put("pdf", "application/pdf");
    }

    @Value("${server.port:9090}")
    private String port;

    @Value("${ip:localhost}")
    private String ip;

    @Resource
    private ImageThumbService imageThumbService;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        String flag;
        synchronized (FileController.class) {
            flag = System.currentTimeMillis() + "";
            ThreadUtil.sleep(1L);
        }
        String fileName = file.getOriginalFilename();
        try {
            if (!checkExt(fileName)) {
                return Result.error("5018", "仅支持图片与 PDF 文件");
            }
            if (!FileUtil.isDirectory(filePath)) {
                FileUtil.mkdir(filePath);
            }
            // 文件存储形式：时间戳-文件名
            FileUtil.writeBytes(file.getBytes(), filePath + flag + "-" + fileName);
            System.out.println(fileName + "--上传成功");
            // 预热缩略图：首个访客零等待
            imageThumbService.warmUp(flag + "-" + fileName);
        } catch (Exception e) {
            System.err.println(fileName + "--文件上传失败");
        }
        String http = "http://" + ip + ":" + port + "/files/";
        return Result.success(http + flag + "-" + fileName);  //  http://localhost:9090/files/1697438073596-avatar.png
    }

    /**
     * 富文本文件上传
     */
    @PostMapping("/editor/upload")
    public Dict editorUpload(MultipartFile file) {
        String flag;
        synchronized (FileController.class) {
            flag = System.currentTimeMillis() + "";
            ThreadUtil.sleep(1L);
        }
        String fileName = file.getOriginalFilename();
        try {
            if (!checkExt(fileName)) {
                return Dict.create().set("errno", 1).set("message", "仅支持图片与 PDF 文件");
            }
            if (!FileUtil.isDirectory(filePath)) {
                FileUtil.mkdir(filePath);
            }
            // 文件存储形式：时间戳-文件名
            FileUtil.writeBytes(file.getBytes(), filePath + flag + "-" + fileName);
            System.out.println(fileName + "--上传成功");
            imageThumbService.warmUp(flag + "-" + fileName);
        } catch (Exception e) {
            System.err.println(fileName + "--文件上传失败");
        }
        String http = "http://" + ip + ":" + port + "/files/";
        return Dict.create().set("errno", 0).set("data", CollUtil.newArrayList(Dict.create().set("url", http + flag + "-" + fileName)));
    }

    /**
     * 获取原图：流式输出 + 正确 Content-Type + 浏览器一年长缓存
     * （文件名含毫秒时间戳全局唯一，immutable 缓存安全；详情页/预览用）
     */
    @GetMapping("/{flag}")   //  1697438073596-avatar.png
    public void getOriginal(@PathVariable String flag, HttpServletResponse response) {
        try {
            File f = new File(filePath + flag);
            if (StrUtil.isEmpty(flag) || flag.contains("..") || !f.exists() || !f.isFile()) {
                response.sendError(404);
                return;
            }
            writeImage(response, f.toPath(), contentTypeOf(flag));
        } catch (Exception e) {
            System.out.println("文件获取失败: " + flag);
        }
    }

    /**
     * 获取缩略图：懒生成（WebP），列表页小图专用
     * 三级缓存：Caffeine 内存 -> 磁盘 thumb 目录 -> 现场由原图生成
     */
    @GetMapping("/thumb/{flag}")
    public void getThumb(@PathVariable String flag, HttpServletResponse response) {
        try {
            byte[] thumb = imageThumbService.getThumb(flag);
            if (thumb == null) {
                response.sendError(404);
                return;
            }
            response.setHeader("Cache-Control", "public, max-age=31536000, immutable");
            response.setContentType(ImageThumbService.sniffContentType(thumb));
            response.setContentLength(thumb.length);
            OutputStream os = response.getOutputStream();
            os.write(thumb);
            os.flush();
        } catch (Exception e) {
            System.out.println("缩略图获取失败: " + flag);
        }
    }

    /**
     * 删除文件：联动清理缩略图磁盘缓存与内存缓存
     */
    @DeleteMapping("/{flag}")
    public void delFile(@PathVariable String flag) {
        FileUtil.del(filePath + flag);
        imageThumbService.evict(flag);
        System.out.println("删除文件" + flag + "成功");
    }

    /**
     * 流式写出文件（替代整文件读入内存，避免大图并发打爆堆）
     * 附浏览器长缓存头：文件名含时间戳唯一，immutable 安全
     */
    private void writeImage(HttpServletResponse response, Path path, String contentType) throws Exception {
        response.setContentType(contentType);
        response.setHeader("Cache-Control", "public, max-age=31536000, immutable");
        response.setHeader("Last-Modified", DateTimeFormatter.RFC_1123_DATE_TIME
                .format(Files.getLastModifiedTime(path).toInstant().atOffset(ZoneOffset.UTC)));
        response.setContentLengthLong(Files.size(path));
        OutputStream os = response.getOutputStream();
        Files.copy(path, os);   // 流式复制，堆占用仅为缓冲区
        os.flush();
    }

    /** 按扩展名映射 Content-Type */
    private String contentTypeOf(String flag) {
        int i = flag.lastIndexOf('.');
        String ext = i < 0 ? "" : flag.substring(i + 1).toLowerCase();
        return CONTENT_TYPES.getOrDefault(ext, "application/octet-stream");
    }

    /** 上传扩展名白名单校验 */
    private boolean checkExt(String fileName) {
        int i = fileName == null ? -1 : fileName.lastIndexOf('.');
        String ext = i < 0 ? "" : fileName.substring(i + 1).toLowerCase();
        return ALLOWED_EXTS.contains(ext);
    }

}
