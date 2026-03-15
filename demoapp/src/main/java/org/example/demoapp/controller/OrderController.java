package org.example.demoapp.controller;

import org.example.demoapp.entity.Order;
import org.example.demoapp.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Order order, HttpServletRequest request){
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null){
                result.put("success", false);
                result.put("message", "用户未登录");
                result.put("code", 401);
                return ResponseEntity.status(401).body(result);
            }

            order.setBuyerId(userId);

            if (order.getProductId() == null) {
                result.put("success", false);
                result.put("message", "商品ID不能为空");
                return ResponseEntity.status(400).body(result);
            }

            Order createdOrder = orderService.createOrder(order);

            result.put("success", true);
            result.put("message", "订单创建成功");
            result.put("data", createdOrder);
            return ResponseEntity.ok(result);

        }catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建订单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable Long id,HttpServletRequest request){
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null){
                result.put("success", false);
                result.put("message", "用户未登录");
                result.put("code", 401);
                return ResponseEntity.status(401).body(result);
            }

            Order order = orderService.getOrderById(id);

            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return ResponseEntity.status(404).body(result);
            }

            if (!order.getBuyerId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权查看此订单");
                return ResponseEntity.status(403).body(result);
            }
            result.put("success", true);
            result.put("data", order);
            return ResponseEntity.ok(result);

        }catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取订单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/my")
    public ResponseEntity<Map<String, Object>> getMyOrders(HttpServletRequest request){
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null){
                result.put("success", false);
                result.put("message", "用户未登录");
                result.put("code", 401);
                return ResponseEntity.status(401).body(result);
            }

            List<Order> orders = orderService.getBuyerOrders(userId);

            result.put("success", true);
            result.put("data", orders);
            result.put("total", orders.size());
            return ResponseEntity.ok(result);

        }catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取订单列表失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<Map<String, Object>> payOrder(@PathVariable Long id,HttpServletRequest request){
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null){
                result.put("success", false);
                result.put("message", "用户未登录");
                result.put("code", 401);
                return ResponseEntity.status(401).body(result);
            }

            Order order = orderService.getOrderById(id);
            if (order == null){
                result.put("success", false);
                result.put("message", "订单不存在");
                return ResponseEntity.status(404).body(result);
            }

            if (!order.getBuyerId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权操作此订单");
                return ResponseEntity.status(403).body(result);
            }

            boolean payResult = orderService.payOrder(id);

            if (payResult) {
                result.put("success", true);
                result.put("message", "支付成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "支付失败");
                return ResponseEntity.status(400).body(result);
            }

        }catch (Exception e) {
            result.put("success", false);
            result.put("message", "支付失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable Long id,HttpServletRequest request){
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null){
                result.put("success", false);
                result.put("message", "用户未登录");
                result.put("code", 401);
                return ResponseEntity.status(401).body(result);
            }

            Order order = orderService.getOrderById(id);
            if (order == null){
                result.put("success", false);
                result.put("message", "订单不存在");
                return ResponseEntity.status(404).body(result);
            }

            if (!order.getBuyerId().equals(userId)) {
                result.put("success", false);
                result.put("message", "无权操作此订单");
                return ResponseEntity.status(403).body(result);
            }

            boolean cancelResult = orderService.cancelOrder(id);

            if (cancelResult) {
                result.put("success", true);
                result.put("message", "订单取消成功");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "取消失败");
                return ResponseEntity.status(400).body(result);
            }

        }catch (Exception e) {
            result.put("success", false);
            result.put("message", "取消失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeOrder(@PathVariable Long id, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Long userId = (Long) request.getAttribute("userId");

            if (userId == null){
                result.put("success", false);
                result.put("message", "用户未登录");
                result.put("code", 401);
                return ResponseEntity.status(401).body(result);
            }

            Order order = orderService.getOrderById(id);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return ResponseEntity.status(404).body(result);
            }

            // 注意：现有代码没有检查订单是否属于当前用户，可能存在安全风险
            // 可根据需要添加权限检查：if (!order.getBuyerId().equals(userId)) { ... }

            boolean completeResult = orderService.completeOrder(id);

            if (completeResult) {
                result.put("success", true);
                result.put("message", "订单已完成");
                return ResponseEntity.ok(result);
            } else {
                result.put("success", false);
                result.put("message", "完成订单失败");
                return ResponseEntity.status(400).body(result);
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "完成订单失败: " + e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception e) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "系统繁忙，请稍后重试");
        return result;
    }
}
