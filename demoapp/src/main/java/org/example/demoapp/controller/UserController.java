package org.example.demoapp.controller;


import org.example.demoapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Map<String, Object> register (@RequestBody Map<String,String>request){
        String username = request.get("username");
        String password = request.get("password");
        String confirmPassword = request.get("confirmPassword");
        String email = request.get("email");

        return userService.register(username,password,confirmPassword,email);
    }

    @PostMapping("/login")
    public Map<String,Object>login(@RequestBody Map<String,String>request){
        String username = request.get("username");
        String password = request.get("password");

        return userService.login(username,password);
    }

    @GetMapping("/profile")
    public Map<String, Object> getProfile(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        // 从拦截器设置的属性中获取用户ID
        Long userId = (Long) request.getAttribute("userId");

        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }

        return userService.getUserById(userId);
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }

        return userService.getUserById(userId);
    }

    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody Map<String, String> requestBody, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户未登录");
            return result;
        }

        // 调试日志：记录哪个用户正在尝试更新信息
        System.out.println("[DEBUG] UserController.updateProfile: userId=" + userId +
                         ", requestBody=" + requestBody);

        String username = requestBody.get("username");
        String email = requestBody.get("email");
        String phone = requestBody.get("phone");

        // 检查请求体中是否包含userId字段（不应该包含）
        if (requestBody.containsKey("userId") || requestBody.containsKey("id")) {
            System.out.println("[WARNING] UserController.updateProfile: 请求体包含用户ID字段，将被忽略");
        }

        return userService.updateUserProfile(userId, username, email, phone);
    }

    /**
     * 测试接口：公开接口（不需要Token）
     * 访问：GET /user/public
     */
    @GetMapping("/public")
    public Map<String, Object> publicInfo() {
        return Map.of(
                "success", true,
                "message", "这是公开信息，无需登录"
        );
    }


}
