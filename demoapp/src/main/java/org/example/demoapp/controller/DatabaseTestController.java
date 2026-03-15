package org.example.demoapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@RestController
public class DatabaseTestController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/test-db")
    public String testDatabaseConnection() {
        // 尝试获取数据库连接
        try (Connection connection = dataSource.getConnection()) {
            return "数据库连接成功！";
        } catch (SQLException e) {
            // 如果密码错误，这里会抛出异常
            return "数据库连接失败！错误信息：" + e.getMessage();
        }
    }
}
