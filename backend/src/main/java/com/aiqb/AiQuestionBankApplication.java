package com.aiqb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * AI 智能题库系统 - 启动类
 */
@SpringBootApplication
@MapperScan("com.aiqb.mapper")
public class AiQuestionBankApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiQuestionBankApplication.class, args);
        System.out.println("""

                ====================================================
                  AI 智能题库系统 启动成功！
                  API 文档: http://localhost:8080/api/doc.html
                  Swagger:  http://localhost:8080/api/swagger-ui.html
                ====================================================
                """);
    }
}
