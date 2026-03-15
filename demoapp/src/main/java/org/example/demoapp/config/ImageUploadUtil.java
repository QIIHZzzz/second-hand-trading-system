package org.example.demoapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Component
public class ImageUploadUtil {
    private String uploadDirectory;

    @Value("${app.image.base-url:}")
    private String imageBaseUrl;

    public ImageUploadUtil() {
        // 使用绝对路径，避免任何路径歧义
        String projectRoot = System.getProperty("user.dir");
        this.uploadDirectory = projectRoot + "/uploads/images";

        // 强制创建目录
        File dir = new File(uploadDirectory);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                System.out.println("上传目录创建成功");
            } else {
                System.err.println("上传目录创建失败");
                // 终极备用方案：使用当前目录
                this.uploadDirectory = "upload_images";
                new File(this.uploadDirectory).mkdirs();
                System.out.println("使用备用目录: " + this.uploadDirectory);
            }
        }
        System.out.println("最终上传目录: " + new File(uploadDirectory).getAbsolutePath());
        System.out.println("====================");
    }

    public String saveImage(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID().toString() +
                (file.getOriginalFilename() != null ?
                        file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")) : ".jpg");

        File dest = new File(uploadDirectory, fileName);
        file.transferTo(dest);

        String imagePath = "/uploads/images/" + fileName;
        // 如果有配置基础URL，则返回完整URL，否则返回相对路径
        if (imageBaseUrl != null && !imageBaseUrl.trim().isEmpty()) {
            String baseUrl = imageBaseUrl.trim();
            // 确保baseUrl不以斜杠结尾
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            return baseUrl + imagePath;
        }
        return imagePath;
    }
}
