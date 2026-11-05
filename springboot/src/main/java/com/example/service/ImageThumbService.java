package com.example.service;

import cn.hutool.core.io.FileUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.luciad.imageio.webp.WebPImageWriterSpi;
import com.luciad.imageio.webp.WebPWriteParam;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 图片缩略图服务：
 * 1. 懒生成 —— 首次请求 /files/thumb/{flag} 时由原图实时生成，磁盘缓存后复用，存量图片零迁移
 * 2. 三级缓存 —— Caffeine 内存字节缓存（热点图零磁盘 IO）→ 磁盘 files/thumb/ → 原图现场生成
 * 3. WebP 输出 —— thumbnailator 等比缩放 + WebP 编码（体积比原图小数倍）；WebP 编码器异常自动回退 JPEG，保证服务不中断
 * 4. GIF 不缩略直接原图，保留动画
 */
@Service
public class ImageThumbService {

    /** 原图目录（与 FileController 保持一致） */
    private static final String BASE_DIR = System.getProperty("user.dir") + "/files/";
    /** 缩略图磁盘缓存目录 */
    private static final String THUMB_DIR = BASE_DIR + "thumb/";
    /** 缩略图最大宽度（列表展示位 80~150px，480px 足够 2x 高清屏） */
    private static final int THUMB_MAX_WIDTH = 480;
    /** WebP 压缩质量 */
    private static final float WEBP_QUALITY = 0.8f;
    /** 可缩略的图片扩展名 */
    private static final List<String> IMAGE_EXTS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp", "bmp");

    /**
     * Caffeine 字节缓存：按字节重量限制总量 32MB（2G 小服务器内存安全），
     * 1 小时无访问过期 —— 热点封面/头像常驻，冷图自动淘汰
     */
    private final Cache<String, byte[]> cache = Caffeine.newBuilder()
            .maximumWeight(32L * 1024 * 1024)
            .weigher((String k, byte[] v) -> v.length)
            .expireAfterAccess(1, TimeUnit.HOURS)
            .build();

    /**
     * 获取缩略图字节（懒生成：内存 → 磁盘 → 现场生成）
     * @param flag 文件名（时间戳-原名），如 1697438073596-avatar.png
     * @return 缩略图字节；原图不存在或非图片文件返回 null
     */
    public byte[] getThumb(String flag) {
        if (flag == null || flag.isEmpty() || flag.contains("..")) {
            return null;
        }
        // 1. 内存缓存
        byte[] cached = cache.getIfPresent(flag);
        if (cached != null) {
            return cached;
        }
        // 2. 磁盘缓存（缩略图文件与原图同名，存放在独立 thumb 目录）
        // 注意：Hutool readBytes 对不存在文件会抛异常，必须先判存在
        if (FileUtil.exist(THUMB_DIR + flag)) {
            byte[] disk = FileUtil.readBytes(THUMB_DIR + flag);
            if (disk != null && disk.length > 0) {
                cache.put(flag, disk);
                return disk;
            }
        }
        // 3. 现场生成
        String ext = getExt(flag);
        if (ext == null || !IMAGE_EXTS.contains(ext)) {
            return null;   // 非图片文件不生成缩略图
        }
        if (!FileUtil.exist(BASE_DIR + flag)) {
            return null;   // 原图不存在
        }
        byte[] original = FileUtil.readBytes(BASE_DIR + flag);
        if (original == null || original.length == 0) {
            return null;
        }
        byte[] thumb;
        try {
            thumb = "gif".equals(ext) ? original : generateScaled(original, ext);
        } catch (Exception e) {
            System.err.println("缩略图生成失败，回退原图: " + flag + " - " + e.getMessage());
            thumb = original;
        }
        // 落盘 + 进内存缓存（原图字节兜底时也缓存，避免反复读原图）
        try {
            FileUtil.writeBytes(thumb, THUMB_DIR + flag);
        } catch (Exception ignore) { }
        cache.put(flag, thumb);
        return thumb;
    }

    /**
     * 等比缩放 + WebP 编码；WebP 编码异常时回退 JPEG
     */
    private byte[] generateScaled(byte[] original, String ext) throws Exception {
        BufferedImage source = ImageIO.read(new ByteArrayInputStream(original));
        if (source == null) {
            return original;   // 解码失败（损坏/伪图片），兜底原图
        }
        // 原图不超限宽：直接转 WebP（不放大）
        BufferedImage scaled = source.getWidth() > THUMB_MAX_WIDTH
                ? Thumbnails.of(source).width(THUMB_MAX_WIDTH).imageType(BufferedImage.TYPE_INT_ARGB).asBufferedImage()
                : source;
        try {
            // libwebp ImageWriter（SPI 实例化，WebP 类为 package-private 不可直调）
            ImageWriter writer = new WebPImageWriterSpi().createWriterInstance(null);
            WebPWriteParam param = new WebPWriteParam(null);
            param.setCompressionQuality(WEBP_QUALITY);   // 默认 lossy
            ByteArrayOutputStream webpOut = new ByteArrayOutputStream();
            MemoryCacheImageOutputStream imgOut = new MemoryCacheImageOutputStream(webpOut);
            writer.setOutput(imgOut);
            writer.write(null, new IIOImage(scaled, null, null), param);
            writer.dispose();
            imgOut.flush();   // 关键：ImageWriter 的输出缓冲必须 flush 才会落到底层 ByteArrayOutputStream
            byte[] webp = webpOut.toByteArray();
            if (webp.length == 0) throw new IllegalStateException("WebP 编码输出为空");
            return webp;
        } catch (Throwable e) {
            // WebP 编码器不可用（如 native 加载失败）：回退 JPEG（透明通道转白底）
            System.err.println("WebP 编码不可用，回退 JPEG: " + e.getMessage());
            ByteArrayOutputStream jpgOut = new ByteArrayOutputStream();
            Thumbnails.of(scaled).scale(1.0).imageType(BufferedImage.TYPE_INT_RGB)
                    .outputFormat("jpg").outputQuality(0.8).toOutputStream(jpgOut);
            return jpgOut.toByteArray();
        }
    }

    /**
     * 上传成功后预热缩略图（首个访客零等待）；非图片或生成失败静默忽略，不影响上传
     */
    public void warmUp(String flag) {
        try {
            getThumb(flag);
        } catch (Exception ignore) { }
    }

    /**
     * 原图删除时联动清理：内存缓存失效 + 磁盘缩略图删除
     */
    public void evict(String flag) {
        if (flag == null) return;
        cache.invalidate(flag);
        FileUtil.del(THUMB_DIR + flag);
    }

    /**
     * 从字节魔数嗅探 Content-Type（缩略图格式由生成结果决定：WebP/JPEG/原格式）
     */
    public static String sniffContentType(byte[] bytes) {
        if (bytes == null || bytes.length < 12) return "application/octet-stream";
        if (bytes[0] == (byte) 0x89 && bytes[1] == 0x50) return "image/png";
        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) return "image/jpeg";
        if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F') return "image/gif";
        if (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P') return "image/webp";
        if (bytes[0] == 'B' && bytes[1] == 'M') return "image/bmp";
        return "application/octet-stream";
    }

    private String getExt(String fileName) {
        int i = fileName.lastIndexOf('.');
        return i < 0 ? null : fileName.substring(i + 1).toLowerCase();
    }
}
