package org.example.demoapp.controller;

import org.example.demoapp.config.ImageUploadUtil;
import org.example.demoapp.entity.Product;
import org.example.demoapp.mapper.ProductMapper;
import org.example.demoapp.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private ImageUploadUtil imageUploadUtil;

    @PostMapping("/create")
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
            System.out.println("[DEBUG] ProductController.createProduct: userId=" + userId + ", requestData=" + requestData);

            // 提取字段
            String title = (String) requestData.get("title");
            String description = (String) requestData.get("description");
            Object priceObj = requestData.get("price");
            String category = (String) requestData.get("category");
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

            // 注意：数据库已删除category字段，忽略前端发送的category
            // product.setCategory(category != null ? category : "其他");

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

            System.out.println("[DEBUG] ProductController.createProduct: 商品创建成功, id=" + createdProduct.getId());
        } catch (Exception e) {
            System.out.println("[ERROR] ProductController.createProduct: 商品发布失败 - " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "商品发布失败：" + e.getMessage());
        }
        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getProduct(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();

        Product product = productService.getProductById(id);
        if (product != null) {
            result.put("success", true);
            result.put("data", product);
        } else {
            result.put("success", false);
            result.put("message", "商品不存在");
        }

        return result;
    }

    @GetMapping
    public Map<String, Object> getAllProducts() {
        Map<String, Object> result = new HashMap<>();

        List<Product> products = productService.getAllProducts();

        result.put("success", true);
        result.put("data", products);
        result.put("total", products.size());

        return result;
    }

    @GetMapping("/my")
    public Map<String, Object> getMyProducts(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Long userId = (Long) request.getAttribute("userId");
        List<Product> products = productService.getUserProducts(userId);

        result.put("success", true);
        result.put("data", products);
        result.put("total", products.size());

        return result;
    }

    // 数据库已删除category字段，暂时禁用分类查询
    // @GetMapping("/category/{category}")
    // public Map<String, Object> getProductsByCategory(@PathVariable String category) {
    //     Map<String, Object> result = new HashMap<>();
    //
    //     List<Product> products = productService.getProductsByCategory(category);
    //
    //     result.put("success", true);
    //     result.put("data", products);
    //     result.put("total", products.size());
    //     result.put("category", category);
    //
    //     return result;
    // }


    /**
     * 更新商品信息
     */
    @PutMapping("/{id}")
    public Map<String,Object> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> requestData, HttpServletRequest request){
        Map<String,Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");
            Product existingProduct = productService.getProductById(id);
            if (existingProduct == null) {
                result.put("success", false);
                result.put("message", "商品不存在");
                return result;
            }
            if (!existingProduct.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权修改他人的商品");
                return result;
            }

            // 调试日志：打印接收到的数据
            System.out.println("[DEBUG] ProductController.updateProduct: id=" + id + ", userId=" + userId + ", requestData=" + requestData);

            // 提取字段
            String title = (String) requestData.get("title");
            String description = (String) requestData.get("description");
            Object priceObj = requestData.get("price");
            String category = (String) requestData.get("category");
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

            // 创建Product对象用于更新
            Product product = new Product();
            product.setId(id); // 设置商品ID
            // 清理标题中的时间戳（如果以"-"后跟13位数字结尾）
            if (title != null && title.matches(".*-\\d{13}$")) {
                title = title.replaceFirst("-\\d{13}$", "");
                System.out.println("[DEBUG] 清理标题时间戳，新标题: " + title);
            }
            product.setTitle(title);
            product.setDescription(description != null ? description : existingProduct.getDescription());

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

            // 注意：数据库已删除category字段，忽略前端发送的category
            // product.setCategory(category != null ? category : existingProduct.getCategory());
            product.setStatus(existingProduct.getStatus()); // 保持原有状态
            product.setImageUrl(existingProduct.getImageUrl()); // 默认使用原有图片

            // 处理图片
            // 优先使用imageUrl字段
            if (imageUrlObj instanceof String && !((String) imageUrlObj).trim().isEmpty()) {
                product.setImageUrl((String) imageUrlObj);
            } else if (imagesObj instanceof List) {
                List<?> images = (List<?>) imagesObj;
                if (!images.isEmpty()) {
                    Object firstImage = images.get(0);
                    if (firstImage instanceof String && !((String) firstImage).trim().isEmpty()) {
                        product.setImageUrl((String) firstImage);
                    }
                } else {
                    // 如果images为空数组，可能需要清除图片？
                    // 目前保持原有图片，不清除
                }
            }

            boolean success = productService.updateProduct(product);

            if (success) {
                result.put("success", true);
                result.put("message", "商品更新成功");
                System.out.println("[DEBUG] ProductController.updateProduct: 商品更新成功, id=" + id);
            } else {
                result.put("success", false);
                result.put("message", "商品更新失败");
            }
        } catch (Exception e) {
            System.out.println("[ERROR] ProductController.updateProduct: 商品更新失败 - " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/{id}")
    public Map<String,Object> deleteProduct(@PathVariable Long id,HttpServletRequest request){
        Map<String,Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");

            Product existingProduct = productService.getProductById(id);
            if (existingProduct == null) {
                result.put("success", false);
                result.put("message", "商品不存在");
                return result;
            }

            if (!existingProduct.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权删除他人的商品");
                return result;
            }

            boolean success = productService.deleteProduct(id);
            if (success) {
                result.put("success", true);
                result.put("message", "商品删除成功");
            } else {
                result.put("success", false);
                result.put("message", "商品删除失败");
            }
        }catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败: " + e.getMessage());
        }

        return result;
    }

    @PostMapping("/{id}/disable")
    public Map<String,Object> disableProduct(@PathVariable Long id,HttpServletRequest request){
        Map<String,Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");

            Product existingProduct = productService.getProductById(id);
            if (existingProduct == null) {
                result.put("success", false);
                result.put("message", "商品不存在");
                return result;
            }

            if (!existingProduct.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权下架他人的商品");
                return result;
            }

            boolean success = productService.disableProduct(id);
            if (success) {
                result.put("success", true);
                result.put("message", "商品下架成功");
            } else {
                result.put("success", false);
                result.put("message", "商品下架失败");
            }
        }catch (Exception e) {
            result.put("success", false);
            result.put("message", "下架失败: " + e.getMessage());
        }

        return result;
    }

    @PostMapping("/{id}/toggle-status")
    public Map<String, Object> toggleProductStatus(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                return result;
            }

            Product existingProduct = productService.getProductById(id);
            if (existingProduct == null) {
                result.put("success", false);
                result.put("message", "商品不存在");
                return result;
            }

            if (!existingProduct.getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权操作他人的商品");
                return result;
            }

            boolean success = productService.toggleProductStatus(id);
            if (success) {
                String statusMessage = existingProduct.getStatus() == Product.STATUS_ON_SALE ? "下架" : "上架";
                result.put("success", true);
                result.put("message", "商品已" + statusMessage);
                result.put("data", Map.of(
                        "id", id,
                        "status", existingProduct.getStatus() == Product.STATUS_ON_SALE ? "下架" : "上架"
                ));
            } else {
                result.put("success", false);
                result.put("message", "切换商品状态失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 商品发布接口（兼容前端调用）
     * POST /api/product
     */
    @RequestMapping(value = "/api/product", method = RequestMethod.POST)
    public Map<String, Object> createProductApi(@RequestBody Map<String, Object> requestData, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                result.put("success", false);
                result.put("message", "用户未登录，请先登录");
                return result;
            }

            // 调试日志：打印接收到的数据
            System.out.println("[DEBUG] ProductController.createProductApi: userId=" + userId + ", requestData=" + requestData);

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

            System.out.println("[DEBUG] ProductController.createProductApi: 商品创建成功, id=" + createdProduct.getId());
        } catch (Exception e) {
            System.out.println("[ERROR] ProductController.createProductApi: 商品发布失败 - " + e.getMessage());
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "商品发布失败：" + e.getMessage());
        }
        return result;
    }

}
