package com.rag.ragbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication
public class RagBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagBackendApplication.class, args);
    }

    @Component
    static class DatabaseConnectionRunner implements ApplicationRunner {

        @Autowired
        private DataSource dataSource;

        @Override
        public void run(ApplicationArguments args) throws Exception {
            // 执行数据库连接检查逻辑
            checkDatabaseConnection();
        }

        private void checkDatabaseConnection() {
            try (Connection connection = dataSource.getConnection()) {
                if (connection != null && !connection.isClosed()) {
                    System.out.println("✅ MySQL 数据库连接成功！");
                    // ... 其他输出信息
                }
            } catch (SQLException e) {
                // ... 错误处理
            }
        }
    }

}
