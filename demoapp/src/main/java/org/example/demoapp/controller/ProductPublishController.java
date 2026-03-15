package org.example.demoapp.controller;

import org.example.demoapp.config.ImageUploadUtil;
import org.example.demoapp.entity.Product;
import org.example.demoapp.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品发布控制器
 * 专门处理前端需要的 POST /api/product 接口
 */
@RestController
@RequestMapping("/api")
public class ProductPublishController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ImageUploadUtil imageUploadUtil;

    /**
     * 商品发布接口
     * POST /api/product
     */
    @PostMapping("/product")
    public Map<String, Object> createProduct(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                result.put("success", false);
                result.put("message", "用户未登录，请先登录");
                return result;
            }

            // 调试日志：打印接收到的数据
            System.out.println("[DEBUG] ProductPublishController.createProduct: userId=" + userId + ", requestData=" + requestData);

            // 提取字段
            String title = (String) requestData.get("title");
            String description = (String) requestData.get("description");
            Object priceObj = requestData.get("price");
            String phone = (String) requestData.get("phone");
            // 优先使用imageUrl字段，向后兼容images字段
            Object imageUrlObj = requestData.get("imageUrl");
            Object imagesObj = requestData.get("images");

            // 验证必填字段
            if (title == null || title.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "商品标题不能为空");
                return result;
            }
            if (priceObj == null) {
                result.put("success", false);
                result.put("message", "商品价格不能为空");
                return result;
            }

            // 创建Product对象
            Product product = new Product();
            product.setUserId(userId);
            // 清理标题中的时间戳（如果以"-"后跟13位数字结尾）
            if (title != null && title.matches(".*-\\d{13}$")) {
                title = title.replaceFirst("-\\d{13}$", "");
                System.out.println("[DEBUG] 清理标题时间戳，新标题: " + title);
            }
            product.setTitle(title);
            product.setDescription(description != null ? description : "");
            product.setPhone(phone != null ? phone : "");

            // 处理价格
            BigDecimal price;
            try {
                if (priceObj instanceof Number) {
                    price = BigDecimal.valueOf(((Number) priceObj).doubleValue());
                } else if (priceObj instanceof String) {
                    price = new BigDecimal((String) priceObj);
                } else {
                    result.put("success", false);
                    result.put("message", "商品价格格式不正确");
                    return result;
                }
            } catch (Exception e) {
                result.put("success", false);
                result.put("message", "商品价格格式不正确: " + e.getMessage());
                return result;
            }
            product.setPrice(price);

            // 处理图片
            String imageUrl = null;
            // 优先使用imageUrl字段
            if (imageUrlObj instanceof String && !((String) imageUrlObj).trim().isEmpty()) {
                imageUrl = (String) imageUrlObj;
            } else if (imagesObj instanceof List) {
                List<?> images = (List<?>) imagesObj;
                if (!images.isEmpty()) {
                    Object firstImage = images.get(0);
                    if (firstImage instanceof String && !((String) firstImage).trim().isEmpty()) {
                        imageUrl = (String) firstImage;
                    }
                }
            }
            product.setImageUrl(imageUrl);

            // 调用服务创建商品
            Product createdProduct = productService.createProduct(product);

            result.put("success", true);
            result.put("message", "商品发布成功！");
            result.put("data", createdProduct);

            System.out.println("[DEBUG] ProductPublishController.createProduct: 商品创建成功, id=" + createdProduct.getId());
        } catch (Exception e) {
            System.out.println("[ERROR] ProductPublishController.createProduct: 商品发布失败 - " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "商品发布失败：" + e.getMessage());
        }
        return result;
    }
}