package org.example.demoapp.Mapper;

import org.example.demoapp.service.UserService;
import org.example.demoapp.util.PasswordUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordUtil passwordUtil;

    @Test
    void testRegister() {
        String username = "reg_" + System.currentTimeMillis();
        Map<String, Object> result = userService.register(
                username,"123456", "123456","1212121212@qq.com"
        );
        assertTrue((Boolean) result.get("success"),"注册应该成功。");
        assertEquals("注册成功",(String) result.get("message"));
    }

    @Test
    void testLogin() {
        String username = "login_" + System.currentTimeMillis();
        userService.register(username, "123456", "123456","1212121212@qq.com");

        Map<String, Object> result = userService.login(username, "123456");
        assertTrue((Boolean) result.get("success"),"登录应该成功。");
        assertEquals("登录成功",(String) result.get("message"));
        assertEquals(200,result.get("code"));
    }

    @Test
    void testPasswordEncryption() {
        String rawPassword = "123456";
        String encrypted = passwordUtil.encrypt(rawPassword);

        assertNotEquals(rawPassword, encrypted, "加密后的密码不应与原文相同");
        assertTrue(passwordUtil.matches(rawPassword, encrypted), "加密后的密码必须能通过验证");
        assertFalse(passwordUtil.matches("wrongpassword", encrypted), "错误密码应该无法通过验证");
    }
}
