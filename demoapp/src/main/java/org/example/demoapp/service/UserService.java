package org.example.demoapp.service;

import org.apache.ibatis.annotations.Mapper;
import org.example.demoapp.entity.User;
import org.example.demoapp.mapper.UserMapper;
import org.example.demoapp.util.JwtUtil;
import org.example.demoapp.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordUtil passwordUtil;

    private boolean isUsernameExists(String username){
        User user = userMapper.selectByUsername(username);
        return user != null;
    }
    private boolean validatePassword(String username, String inputPassword) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return false;
        }
        return inputPassword.equals(user.getPassword());
    }

    public Map<String, Object> register(String username, String password, String confirmPassword, String email) {
        Map<String, Object> result = new HashMap<>();

            if (isUsernameExists(username)) {
                result.put("success", false);
                result.put("message", "用户名已存在");
                return result;
            }
            if (!password.equals(confirmPassword)) {
                result.put("success", false);
                result.put("message", "两次密码不一致");
                return result;
            }

            String encryptedPassword = passwordUtil.encrypt(password);

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(encryptedPassword);
            newUser.setEmail(email);
            newUser.setCreateTime(LocalDateTime.now());
            newUser.setUpdateTime(LocalDateTime.now());

            userMapper.insert(newUser);

            result.put("success", true);
            result.put("message", "注册成功");
            return result;

    }

    public Map<String, Object> login(String username, String password) {
        Map<String, Object> result = new HashMap<>();

        User user = userMapper.selectByUsername(username);

        if (user == null) {
            result.put("success", false);
            result.put("code", 401);
            result.put("message", "用户名或密码错误");
            return result;
        }

        if (!passwordUtil.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("code", 401);
            result.put("message", "用户名或密码错误");
            return result;
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        result.put("success", true);
        result.put("code", 200);
        result.put("message", "登录成功");
        // 按照要求将token和userInfo合并到data中
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", getUserInfo(user));
        result.put("data", data);
        return result;

    }
    private Map<String, Object> getUserInfo(User user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("email", user.getEmail());
        info.put("phone", user.getPhone());
        info.put("avatar", user.getAvatar());
        info.put("createdAt", user.getCreateTime());
        info.put("updatedAt", user.getUpdateTime());
        return info;
    }

    public Map<String, Object> getUserById(Long userId) {
        Map<String, Object> result = new HashMap<>();
        User user = userMapper.selectById(userId);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }
        result.put("success", true);
        result.put("message", "获取用户信息成功");
        result.put("data", getUserInfo(user));
        return result;
    }

    public Map<String, Object> updateUserProfile(Long userId, String username, String email, String phone) {
        Map<String, Object> result = new HashMap<>();

        // 验证userId不为null
        if (userId == null) {
            result.put("success", false);
            result.put("message", "用户ID不能为空");
            return result;
        }

        System.out.println("[DEBUG] UserService.updateUserProfile: 开始更新用户, userId=" + userId +
                         ", username=" + username + ", email=" + email + ", phone=" + phone);

        User user = userMapper.selectById(userId);
        if (user == null) {
            System.out.println("[ERROR] UserService.updateUserProfile: 用户不存在, userId=" + userId);
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        // 双重验证：确保查询到的用户ID与传入的userId一致
        if (!user.getId().equals(userId)) {
            System.out.println("[ERROR] UserService.updateUserProfile: 用户数据不一致! 查询到的用户ID=" +
                             user.getId() + ", 传入的userId=" + userId);
            result.put("success", false);
            result.put("message", "用户数据不一致");
            return result;
        }

        // 检查用户名是否已被其他用户使用
        if (username != null && !username.equals(user.getUsername())) {
            User existingUser = userMapper.selectByUsername(username);
            if (existingUser != null && !existingUser.getId().equals(userId)) {
                result.put("success", false);
                result.put("message", "用户名已被其他用户使用");
                return result;
            }
            user.setUsername(username);
        }

        if (email != null) {
            user.setEmail(email);
        }

        if (phone != null) {
            user.setPhone(phone);
        }

        user.setUpdateTime(java.time.LocalDateTime.now());

        // 确保用户ID在更新前没有被意外修改
        user.setId(userId);

        System.out.println("[DEBUG] UserService.updateUserProfile: 准备更新用户, user.id=" + user.getId() +
                         ", username=" + user.getUsername() + ", email=" + user.getEmail());

        int rows = userMapper.update(user);
        if (rows > 0) {
            System.out.println("[DEBUG] UserService.updateUserProfile: 更新成功, 影响行数=" + rows);
            result.put("success", true);
            result.put("message", "更新成功");
            result.put("user", getUserInfo(user));
        } else {
            System.out.println("[DEBUG] UserService.updateUserProfile: 更新失败, 影响行数=" + rows);
            result.put("success", false);
            result.put("message", "更新失败");
        }

        System.out.println("[DEBUG] UserService.updateUserProfile: 更新完成, 结果=" + result.get("success"));
        return result;
    }
}
