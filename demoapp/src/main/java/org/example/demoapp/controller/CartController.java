package org.example.demoapp.controller;

import org.example.demoapp.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 获取当前用户的购物车
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                result.put("code", 401);
                return ResponseEntity.status(401).body(result);
            }

            var cartItems = cartService.getCartItems(userId);
            result.put("success", true);
            result.put("message", "获取购物车成功");
            result.put("data", cartItems);
            result.put("total", cartItems.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取购物车失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 添加商品到购物车
     */
    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addItem(@RequestBody Map<String, Object> requestBody,
                                       HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                result.put("code", 401);
                return ResponseEntity.status(401).body(result);
            }

            // 解析请求参数
            Long productId = null;
            Integer quantity = 1; // 默认数量为1

            if (requestBody.get("productId") != null) {
                productId = Long.valueOf(requestBody.get("productId").toString());
            }
            if (requestBody.get("quantity") != null) {
                quantity = Integer.valueOf(requestBody.get("quantity").toString());
            }

            if (productId == null || productId <= 0) {
                result.put("success", false);
                result.put("message", "商品ID不能为空");
                return ResponseEntity.status(400).body(result);
            }
            if (quantity <= 0) {
                result.put("success", false);
                result.put("message", "商品数量必须大于0");
                return ResponseEntity.status(400).body(result);
            }

            var cartItem = cartService.addItem(userId, productId, quantity);
            result.put("success", true);
            result.put("message", "商品已加入购物车");
            result.put("data", cartItem);
            return ResponseEntity.ok(result);

        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "参数格式错误");
            return ResponseEntity.status(400).body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加商品失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 更新购物车项数量
     */
    @PutMapping("/items/{itemId}")
    public ResponseEntity<Map<String, Object>> updateItemQuantity(@PathVariable Long itemId,
                                                  @RequestBody Map<String, Object> requestBody,
                                                  HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                result.put("code", 401);
                return ResponseEntity.status(401).body(result);
            }

            // 解析数量参数
            Integer quantity = null;
            if (requestBody.get("quantity") != null) {
                quantity = Integer.valueOf(requestBody.get("quantity").toString());
            }

            if (quantity == null || quantity <= 0) {
                result.put("success", false);
                result.put("message", "商品数量必须大于0");
                return ResponseEntity.status(400).body(result);
            }

            boolean success = cartService.updateItemQuantity(itemId, userId, quantity);
            if (success) {
                result.put("success", true);
                result.put("message", "更新数量成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "更新数量失败");
                return ResponseEntity.status(400).body(result);
            }

        } catch (NumberFormatException e) {
            result.put("success", false);
            result.put("message", "参数格式错误");
            return ResponseEntity.status(400).body(result);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新数量失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 从购物车移除商品
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Map<String, Object>> removeItem(@PathVariable Long itemId,
                                          HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                result.put("code", 401);
                return ResponseEntity.status(401).body(result);
            }

            boolean success = cartService.removeItem(itemId, userId);
            if (success) {
                result.put("success", true);
                result.put("message", "移除商品成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "移除商品失败");
                return ResponseEntity.status(400).body(result);
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "移除商品失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 清空购物车
     */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearCart(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");
            if (userId == null) {
                result.put("success", false);
                result.put("message", "用户未登录");
                result.put("code", 401);
                return ResponseEntity.status(401).body(result);
            }

            boolean success = cartService.clearCart(userId);
            if (success) {
                result.put("success", true);
                result.put("message", "购物车已清空");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "清空购物车失败");
                return ResponseEntity.status(400).body(result);
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "清空购物车失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * 异常处理器（复用OrderController模式）
     */
    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception e) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "系统繁忙，请稍后重试");
        return result;
    }
}