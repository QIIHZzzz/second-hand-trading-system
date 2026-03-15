package org.example.demoapp.Mapper;

import org.example.demoapp.entity.User;
import org.example.demoapp.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest // 这个注解表示这是一个Spring Boot测试
class UserMapperTest {

    @Autowired // 自动注入UserMapper，Spring会帮你实例化
    private UserMapper userMapper;

    @Test
        // 这个注解表示这是一个测试方法
    void testSelectByUsername() {
        // 这里是在测试你的Mapper，不是处理用户请求
        User user = userMapper.selectByUsername("Hzz");
        System.out.println("查询结果: " + user);
    }
}