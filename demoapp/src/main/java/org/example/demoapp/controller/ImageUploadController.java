package org.example.demoapp.controller;

import org.example.demoapp.config.ImageUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {
    @Autowired
    private ImageUploadUtil imageUploadUtil;

    @PostMapping("/image")
    public Map<String,Object> uploadImage(@RequestParam("file") MultipartFile file){
        Map<String,Object> result = new HashMap<>();

        try{
            if(file == null || file.isEmpty()) {
                result.put("success", false);
                result.put("message", "请选择要上传的文件");
                return result;
            }

            String imageUrl = imageUploadUtil.saveImage(file);
                result.put("success",true);
                result.put("message","上传成功");
                result.put("data", Map.of("imageUrl", imageUrl));

            }catch (Exception e) {
                result.put("success", false);
                result.put("message", "上传失败: " + e.getMessage());
            }
        return result;
        }
    }

