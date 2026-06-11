package com.vetech.serve;


/**
 * @cn-file
 * 文件：serve/src/main/java/com/vetech/serve/ServeApplication.java
 * 说明：后端模块：Java 服务端实现
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 后端服务启动类。
 */
@SpringBootApplication
public class ServeApplication {

    /**
     * Spring Boot 服务启动入口。
     *
     * @param args 启动参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ServeApplication.class, args);
    }

}
